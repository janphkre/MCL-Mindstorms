import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorHTSensor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeReadings;
import lejos.robotics.RangeScanner;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RotatingRangeScanner;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;

public class MclDaemon implements Runnable, ButtonListener, MoveListener {
	
	private static enum Message {SET_ANGLES, SET_MIN_DISTANCE, SET_MAX_DISTANCE, GET_RANGES, GET_MOVE, RANGES, MOVE, MOVE_END};
	private static final RegulatedMotor HEAD_MOTOR = Motor.C;
	private static final RegulatedMotor LEFT_MOTOR = Motor.A;
	private static final RegulatedMotor RIGHT_MOTOR = Motor.B;
	private static final SensorPort ULTRASONIC_PORT = SensorPort.S4;
	private static final SensorPort COLOR_PORT = SensorPort.S3;
	private static final SensorPort LIGHT_PORT = SensorPort.S2;
	
	private static final int HEAD_GEAR_RATIO = 1;
	private static final double WHEEL_DIAMETER= 3.4d;
	private static final double TRACK_WIDTH= 16.1d;
	private static final double ROTATE_SPEED = 100d;
	private static final double TRAVEL_SPEED = 50d;
	
	
	private Random rand = new Random();
	private boolean running = false;
	private DifferentialPilot pilot;
	private RangeScanner scanner;
	private ColorHTSensor color;
	private LightSensor light;
	
	private DataInputStream in;
	private DataOutputStream out;//the output has to be synchronized, as it is used by the MoveListener too.
	
	private int moveStartCounter = 0;
	private int moveStopCounter = 0;
	
	private float minDistance;
	private float maxDistance;
	//private float[] rangeAngles = {-90f,-45f,0f,45f,90f};
	
	public static void main(String[] args) {
		(new MclDaemon()).run();
	}

	public MclDaemon() {
		pilot = new DifferentialPilot(WHEEL_DIAMETER,TRACK_WIDTH,LEFT_MOTOR,RIGHT_MOTOR,false);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setTravelSpeed(TRAVEL_SPEED);
		RangeFinder sonic = new UltrasonicSensor(ULTRASONIC_PORT);
		scanner = new RotatingRangeScanner(HEAD_MOTOR, sonic, HEAD_GEAR_RATIO);
		//scanner.setAngles(rangeAngles);
		color = new ColorHTSensor(COLOR_PORT);
		light = new LightSensor(LIGHT_PORT);
		
		NXTCommConnector connector = Bluetooth.getConnector();
		NXTConnection conn = connector.waitForConnection(0, NXTConnection.PACKET);
		in = conn.openDataInputStream();
		out = conn.openDataOutputStream();
		System.out.println("Connected");
		
		pilot.addMoveListener(this);
	}
	
	@Override
	public void run() {
		running = true;
    	Button.ESCAPE.addButtonListener(this);
    	while(running) {
			try {
				final Message message = Message.values()[in.readByte()];
				switch(message) {
				case GET_MOVE:
					System.out.println("MOVE");
					performMove();
					break;	
				case GET_RANGES:
					System.out.println("RANGES");
					final RangeReadings ranges = scanner.getRangeValues();
					synchronized(out) {
						out.writeByte(Message.RANGES.ordinal());
						ranges.dumpObject(out);
						out.flush();
					}
					break;
				case SET_ANGLES:
					final int count = in.readShort();
					float[] rangeAngles = new float[count];
					for(int i=0;i<count;i++) {
						rangeAngles[i] = in.readFloat();
					}
					scanner.setAngles(rangeAngles);
					break;
				case SET_MIN_DISTANCE:
					minDistance = in.readFloat();
					break;
				case SET_MAX_DISTANCE:
					maxDistance = in.readFloat();
					break;
				default:
					System.out.println("MESSAGE:"+message.ordinal()+"?");
				}
			} catch (IOException e) {
				exception();
			}
			System.gc();
		}
    	
	}
	
	private void performMove() {
		float targetdist = (float) (minDistance + rand.nextGaussian() * (maxDistance - minDistance));
		float delta = 0f;
		pilot.forward();
        while(delta + pilot.getMovement().getDistanceTraveled() < targetdist && running) {
			if(color.getColorID() <= 5 || light.readValue() <= 40) {
        		int i = 5;
				while ((color.getColorID() <= 5 || light.readValue() <= 40) && running) {
					delta += pilot.getMovement().getDistanceTraveled();
					pilot.stop();
					pilot.rotate(i,true);
					while ((color.getColorID() <= 5 || light.readValue() <= 40) && pilot.isMoving() && running) Thread.yield();
					pilot.stop();
					i *= -2;
					i = i % 360;
				}
				pilot.forward();
        	}
        }
        pilot.stop();
        while(moveStartCounter != moveStopCounter) Thread.yield(); //Make sure, that all MOVES have been sent before this message! TODO: Any better ideas/implementation of a "semaphore" for synchronization?
        synchronized(out) {
			try {
				out.writeByte(Message.MOVE_END.ordinal());
				out.flush();
			} catch (IOException e) {
				exception();
			}
		}
	}
	
	@Override
	public void moveStarted(Move event, MoveProvider mp) {
		moveStartCounter++;
	}

	@Override
	public void moveStopped(Move event, MoveProvider mp) {
		synchronized(out) {
			try {
				out.writeByte(Message.MOVE.ordinal());
				event.dumpObject(out);
				out.flush();
			} catch (IOException e) {
				exception();
			}
		}
		moveStopCounter++;
	}

	@Override
	public void buttonPressed(Button b) {}

	@Override
	public void buttonReleased(Button b) {
		System.out.println("Exit");
		running = false;
		pilot.stop();
		System.exit(0);
	}
	
	private void exception() {
		System.out.println("IO Exception");
		running = false;
		pilot.stop();
		System.exit(0);
	}
	
}
