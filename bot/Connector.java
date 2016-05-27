package bot;

import robotics.generic.IMclRobot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.*;
import lejos.robotics.RangeReadings;
import lejos.robotics.navigation.Move;
import localization.Move2D;

public class Connector implements IMclRobot<Move2D>, Runnable {
	
	private static final double MAX_RELIABLE_RANGE_READING = 180.0d;//cm
	private static final double RANGE_SENSOR_NOISE = 2.0d;//cm
	private static enum Message {GET_RANGES,GET_MOVE, RANGES, MOVE, MOVE_END};
	private final double[] rangeAngles;
	
	private boolean connected = false;
	private NXTConnector connection;
	private DataInputStream in; //only used in the second thread. No synchronization!
	private DataOutputStream out; //synchronized, just to be sure between Gui and MCL/Core!
	private Thread connectionThread;
	private SynchronousQueue<double[]> rangeQueue;
	private SynchronousQueue<Move2D> moveQueue;
	private Random rand;
	
	public Connector(final double[] rangeAngles) {
		this.rangeAngles = rangeAngles;
		this.rangeQueue = new SynchronousQueue<double[]>();
		this.moveQueue = new SynchronousQueue<Move2D>();
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
		try {
			connection.close();
		} catch (Exception e) { }
		//TODO: MESSAGEBOX / connectionPanel!
	}
	
	public void connect(String name, String program) {
		connection = new NXTConnector();
		
		connection = new NXTConnector();
		if(!connection.connectTo(name, null, NXTCommFactory.BLUETOOTH, NXTComm.LCP)) {
			System.out.println("Failed to connect to the NXT.");
			connected = false;
			return;
		}
		NXTCommand command = new NXTCommand(connection.getNXTComm());
		try {
			command.startProgram(program);
		} catch (IOException e) {
			System.out.println("Failed to start the program.");
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
	public double[] getRangeReadings() {
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
	public double calculateRangeNoise(double rangeReading, double rangeMap) {
		if((rangeReading < 0 || rangeReading > MAX_RELIABLE_RANGE_READING) && rangeMap > MAX_RELIABLE_RANGE_READING) return 1;
		if(rangeReading < 0) return 0;
		final double realRangeReading = rangeReading + RANGE_SENSOR_NOISE * rand.nextDouble() - RANGE_SENSOR_NOISE / 2;
		final double delta = realRangeReading > rangeMap ? realRangeReading - rangeMap : rangeMap - realRangeReading;
		return delta < MAX_RELIABLE_RANGE_READING ? delta / MAX_RELIABLE_RANGE_READING: 0/*TODO: Zero? What if the sensor failed to fetch a correct result?*/;
	}

	
	@Override
	public Move2D performMove() {
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
		Move2D currentMove = new Move2D();
		while(connected) {
			try {
				Message message = Message.values()[in.read()];
				switch(message) {
				case RANGES:
					RangeReadings rangeReadings = new RangeReadings(rangeAngles.length);
					rangeReadings.loadObject(in);
					double[] ranges = new double[rangeAngles.length];
					for(int i=0;i < rangeAngles.length;i++) {
						ranges[i] = rangeReadings.getRange(i);
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
					currentMove = new Move2D();
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
}
