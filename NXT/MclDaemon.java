import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
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

/**
 * A basic daemon that is meant to run on the NXT to be able to control the robot for the Monte-Carlo-Localization.<br/>
 * First it awaits a bluetooth connection via {@code NXTConnection.PACKET} and awaits commands over that connection afterwards.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class MclDaemon implements Runnable, ButtonListener, MoveListener {
	
	private static enum Message {SET_VERBOSE, SET_ANGLES, SET_MIN_DISTANCE, SET_MAX_DISTANCE, SET_ROTATE_SPEED, SET_TRAVEL_SPEED, SET_SAFE_SPACE, SET_COLOR_CUTOFF, SET_LIGHT_CUTOFF, SET_ROTATION_START_ANGLE, GET_RANGES, GET_LINE_MOVE, GET_RANDOM_MOVE, RANGES, MOVE, MOVE_END};
	private static final RegulatedMotor HEAD_MOTOR = Motor.C;
	private static final RegulatedMotor LEFT_MOTOR = Motor.B;
	private static final RegulatedMotor RIGHT_MOTOR = Motor.A;
	private static final SensorPort ULTRASONIC_PORT = SensorPort.S1;
	private static final SensorPort COLOR_PORT = SensorPort.S3;
	private static final SensorPort LIGHT_PORT = SensorPort.S4;
	private static final int HEAD_GEAR_RATIO = -1;
	private static final double WHEEL_DIAMETER= 3.4d;
	private static final double TRACK_WIDTH= 18.1d;
	
	private static Random RAND = new Random();
	
	private boolean running = false;
	private DifferentialPilot pilot;
	private RangeFinder sonic;
	private RangeScanner scanner;
	private ColorHTSensor color;
	private LightSensor light;
	private NXTConnection conn;
	private DataInputStream in;
	/**
	 * The output should be synchronized as it is used by the MoveListener too. Use:<br/>
	 * <code>synchronized(out) {...}</code>
	 */
	private DataOutputStream out;
	private float minDistance;
	private float maxDistance;
	private double rotateSpeed;
	private double travelSpeed;
	private float safeSpace;
	private int colorCutoff;
	private int lightCutoff;
	private int rotationStartAngle;
	private boolean verbose = false;
	
	/**
	 * Creates a new instance of the MclDaemon and runs it.
	 * @param args unused.
	 */
	public static void main(String[] args) {
		(new MclDaemon()).run();
	}

	/**
	 * Initializes the daemon and awaits a bluetooth connection via {@code NXTConnection.PACKET}.
	 */
	public MclDaemon() {
		pilot = new DifferentialPilot(WHEEL_DIAMETER,TRACK_WIDTH,LEFT_MOTOR,RIGHT_MOTOR,true);
		pilot.setRotateSpeed(rotateSpeed);
		pilot.setTravelSpeed(travelSpeed);
		sonic = new UltrasonicSensor(ULTRASONIC_PORT);
		scanner = new RotatingRangeScanner(HEAD_MOTOR, sonic, HEAD_GEAR_RATIO);
		color = new ColorHTSensor(COLOR_PORT);
		light = new LightSensor(LIGHT_PORT);
		
		NXTCommConnector connector = Bluetooth.getConnector();
		conn = connector.waitForConnection(0, NXTConnection.PACKET);
		in = conn.openDataInputStream();
		out = conn.openDataOutputStream();
		System.out.println("Connected");
		Sound.playTone(600, 100);
		pilot.addMoveListener(this);
	}
	
	/**
	 * 
	 */
	private void close() {
		running = false;
		pilot.stop();
		conn.close();
		Sound.beepSequence();
		System.getRuntime().halt(0);
	}
	
	private void exception() {
		System.out.println("IO Exception");
		close();
	}
	
	/**
	 * As a move the robot follows a line.
	 * @throws IOException
	 */
	private void performLineMove() throws IOException {
		final float targetdist = (float) (minDistance + RAND.nextFloat() * (maxDistance - minDistance));
		float delta = 0f;
		pilot.forward();
        while(delta + pilot.getMovement().getDistanceTraveled() < targetdist && running) {
			if(color.getColorID() <= colorCutoff || light.readValue() <= lightCutoff) {
        		int i = rotationStartAngle;
				while ((color.getColorID() <= colorCutoff || light.readValue() <= lightCutoff) && running) {
					delta += pilot.getMovement().getDistanceTraveled();
					pilot.stop();
					pilot.rotate(i,true);
					while ((color.getColorID() <= colorCutoff || light.readValue() <= lightCutoff) && pilot.isMoving() && running) Thread.yield();
					pilot.stop();
					i *= -2;
					i = i % 360;
				}
				pilot.forward();
        	}
        }
        pilot.stop();
        while(pilot.isMoving()) Thread.yield(); //Make sure, that all MOVES have been sent before the MOVE_END message!
        synchronized(out) {
			out.write(Message.MOVE_END.ordinal());
			out.flush();
		}
	}
	
	/**
	 * As a move the robot rotates a random angle and goes forward a random distance.
	 * @throws IOException
	 */
	private void performRandomMove() throws IOException {
		final float targetdist = (float) (minDistance + RAND.nextDouble() * (maxDistance - minDistance));
		final float randomAngle = (float) (360.0d * RAND.nextDouble() - 180.0d);
		pilot.rotate(randomAngle);
		for(int i=0; i < 10; i++) {
			pilot.stop();
			while(pilot.isMoving()) Thread.yield();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			float forwardRange;
		    try {
		    	forwardRange = sonic.getRange();
		    } catch (Exception e) {
		    	forwardRange = 0;
		    }
		    if (forwardRange < 0 || targetdist + safeSpace < forwardRange) {
		    	pilot.travel(targetdist);
		    	break;
		    } else if(safeSpace < forwardRange) {
		    	pilot.travel(forwardRange - safeSpace);
		    	break;
		    } else {
		    	pilot.rotate(rotationStartAngle);
		    }
		}
		pilot.stop();
		while(pilot.isMoving()) Thread.yield(); //Make sure, that all MOVES have been sent before the MOVE_END message!
        synchronized(out) {
			out.write(Message.MOVE_END.ordinal());
			out.flush();
		}
        
	}
	
	private void readRanges() throws IOException {
		final RangeReadings ranges = scanner.getRangeValues();
		synchronized(out) {
			out.write(Message.RANGES.ordinal());
			ranges.dumpObject(out);
			out.flush();
		}
	}
	
	private void setAngles() throws IOException {
		final int count = in.readInt();
		float[] rangeAngles = new float[count];
		for(int i=0;i<count;i++) {
			rangeAngles[i] = in.readFloat();
		}
		scanner.setAngles(rangeAngles);
	}
	
	@Override
	public void run() {
		running = true;
    	Button.ESCAPE.addButtonListener(this);
    	while(running) {
			try {
				final int input = in.read();
				if(input < 0) break;
				if(input >= Message.values().length) continue;
				final Message message = Message.values()[input];
				switch(message) {
				case GET_RANDOM_MOVE:
					if(verbose) System.out.println("RANDOM");
					performRandomMove();
					break;
				case GET_LINE_MOVE:
					if(verbose) System.out.println("LINE");
					performLineMove();
					break;
				case GET_RANGES:
					if(verbose) System.out.println("RANGES");
					readRanges();
					break;
				case SET_ANGLES:
					if(verbose) System.out.println("ANGLES");
					setAngles();
					break;
				case SET_MIN_DISTANCE:
					if(verbose) System.out.println("MIN_DISTANCE");
					minDistance = in.readFloat();
					break;
				case SET_MAX_DISTANCE:
					if(verbose) System.out.println("MAX_DISTANCE");
					maxDistance = in.readFloat();
					break;
				case SET_COLOR_CUTOFF:
					if(verbose) System.out.println("COLOR_CUTOFF");
					colorCutoff = in.readInt();
					break;
				case SET_LIGHT_CUTOFF:
					if(verbose) System.out.println("LIGHT_CUTOFF");
					lightCutoff = in.readInt();
					break;
				case SET_ROTATE_SPEED:
					if(verbose) System.out.println("ROTATE_SPEED");
					rotateSpeed = in.readDouble();
					break;
				case SET_ROTATION_START_ANGLE:
					if(verbose) System.out.println("ROTATION_ANGLE");
					rotationStartAngle = in.readInt();
					break;
				case SET_SAFE_SPACE:
					if(verbose) System.out.println("SAFE_SPACE");
					safeSpace = in.readFloat();
					break;
				case SET_TRAVEL_SPEED:
					if(verbose) System.out.println("TRAVEL_SPEED");
					travelSpeed = in.readDouble();
					break;
				case SET_VERBOSE:
					verbose = in.readBoolean();
				default:
					System.out.println("MESSAGE:"+message.ordinal()+"?");
				}
			} catch (IOException e) {
				exception();
    		}
			System.gc();
		}
	}
	
	@Override
	public void moveStarted(Move event, MoveProvider mp) { }

	@Override
	public void moveStopped(Move event, MoveProvider mp) {
		synchronized(out) {
			try {
				out.write(Message.MOVE.ordinal());
				event.dumpObject(out);
				out.flush();
			} catch (IOException e) {
				exception();
			}
		}
	}

	@Override
	public void buttonPressed(Button b) {
		System.out.println("Exit");
		close();
	}

	@Override
	public void buttonReleased(Button b) { }
}
