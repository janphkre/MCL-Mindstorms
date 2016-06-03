package bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import gui.ErrorLog;
import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.robotics.RangeReadings;
import lejos.robotics.navigation.Move;
import localization.NXTMove;
import robotics.concrete.datatypes.Angle;
import robotics.concrete.datatypes.RangeReading;
import robotics.generic.IMclRobot;

public class Connector implements IMclRobot<Angle,NXTMove,RangeReading>, Runnable {
	
	private static final double MAX_RELIABLE_RANGE_READING = 180.0d;//cm
	private static final double MAX_RANGE_READING = 255.0d;//cm
	private static final double RANGE_SENSOR_NOISE = 2.0d;//cm
	private static enum Message {GET_RANGES,GET_MOVE, RANGES, MOVE, MOVE_END};
	private static final Angle[] RANGE_ANGLES = {
			new Angle(-90d),
			new Angle(-45d),
			new Angle(  0d),
			new Angle( 45d),
			new Angle( 90d)};
	
	private boolean connected = false;
	private NXTConnector connection;
	private DataInputStream in; //only used in the second thread. No synchronization!
	private DataOutputStream out; //synchronized, just to be sure between GUI and MCL/Core!
	private Thread connectionThread;
	private SynchronousQueue<RangeReading[]> rangeQueue;
	private SynchronousQueue<NXTMove> moveQueue;
	private Random rand;
	
	public Connector() {
		this.rangeQueue = new SynchronousQueue<RangeReading[]>();
		this.moveQueue = new SynchronousQueue<NXTMove>();
		this.rand = new Random();
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void close() {
		if(connected) {
			try {
				connection.close();
			} catch (IOException e) { }
		}
		connection = null;
		connected=false;
	}
	
	private void ioException() {
		close();
		ErrorLog.log("IOException! Did the Bot turn off?");
	}
	
	public void connect(String name, String program) {
		connection = new NXTConnector();
		
		connection = new NXTConnector();
		if(!connection.connectTo(name, null, NXTCommFactory.BLUETOOTH, NXTComm.LCP)) {
			ErrorLog.log("Failed to connect to the NXT.");
			connected = false;
			return;
		}
		NXTCommand command = new NXTCommand(connection.getNXTComm());
		try {
			command.startProgram(program);
		} catch (IOException e) {
			ErrorLog.log("Failed to start the program.");
			try {
				command.disconnect();
				connection.close();
			} catch (IOException f) { }
			connected = false;
			return;
		}
		try {
			command.disconnect();
			connection.close();
		} catch (IOException e) { }
		try {
			Thread.sleep(2000); // Wait 2 seconds for program to start 
		} catch (InterruptedException e) { }
		
		//CONNECT NORMAL:
		connection = new NXTConnector();
		if(!connection.connectTo(name, null, NXTCommFactory.BLUETOOTH, NXTComm.PACKET)) {
			connected = false;
			return;
		}
		
		in = new DataInputStream(connection.getInputStream());
		out = new DataOutputStream(connection.getOutputStream());
		connected = true;
		
		
		
		if(connectionThread != null) {
			connectionThread.interrupt();
		}
    	connectionThread = new Thread(this);
    	connectionThread.setDaemon(true);
    	connectionThread.start();
	}
	
	@Override
	public RangeReading[] getRangeReadings() {
		if(!connected) return null;
		synchronized(out) {
			try {
				out.write(Message.GET_RANGES.ordinal());
				out.flush();
			} catch (IOException e) {
				ioException();
				return null;
			}
		}
		try {
			return rangeQueue.take();
		} catch (InterruptedException e) {
			// TODO ignore???
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public float calculateRangeNoise(RangeReading rangeReading, RangeReading rangeMap) {
		if((rangeReading.getValue() < 0 || rangeReading.getValue() > MAX_RELIABLE_RANGE_READING) && rangeMap.getValue() > MAX_RELIABLE_RANGE_READING) return 1;
		if(rangeReading.getValue() < 0) return 0;
		final double adaptedRangeReading = rangeReading.getValue() + RANGE_SENSOR_NOISE * rand.nextDouble() - RANGE_SENSOR_NOISE / 2;
		final double delta = Math.abs(adaptedRangeReading - rangeMap.getValue());
		return (float) (delta < MAX_RELIABLE_RANGE_READING ? 1/delta: 1/delta);//TODO: any better ideas instead of 1/delta?
	}

	
	@Override
	public NXTMove performMove() {
		if(!connected) return null;
		synchronized(out) {
			try {
				out.write(Message.GET_MOVE.ordinal());
				out.flush();
			} catch (IOException e) {
				ioException();
				return null;
			}
		}
		try {
			return moveQueue.take();
		} catch (InterruptedException e) {
			// TODO ignore???
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void run() {
		NXTMove currentMove = new NXTMove();
		while(connected) {
			try {
				Message message = Message.values()[in.read()];
				switch(message) {
				case RANGES:
					RangeReadings rangeReadings = new RangeReadings(RANGE_ANGLES.length);
					rangeReadings.loadObject(in);
					RangeReading[] ranges = new RangeReading[RANGE_ANGLES.length];
					for(int i=0;i < RANGE_ANGLES.length;i++) {
						ranges[i] = new RangeReading(rangeReadings.getRange(i));
					}
					rangeQueue.put(ranges);
					break;
				case MOVE://Move is posted after each segment of a Move has stopped.
					Move move = new Move(false, 0, 0);
					move.loadObject(in);
					currentMove.add(move);
					System.out.println(move.getMoveType());//TODO: TEST!
					break;
				case MOVE_END://As we want to update the particles only once after the complete move has come to an end, we need some other message too.
					moveQueue.put(currentMove);
					currentMove = new NXTMove();
					break;
				default:
				}
			} catch (IOException e) {
				ioException();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public Angle[] getRangeVectors() {
		return RANGE_ANGLES;
	}
	
	public double getMaxSensorRange() {
		return MAX_RANGE_READING;
	}
}
