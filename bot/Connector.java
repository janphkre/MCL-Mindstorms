package bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.robotics.RangeReadings;
import lejos.robotics.navigation.Move;
import localization.NXTMove;
import localization.NXTRangeReading;
import aima.core.robotics.IMclRobot;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.gui.applications.robotics.components.AnglePanel.ChangeListener;
import aima.gui.applications.robotics.util.GuiBase;

/**
 * This class establishes and manages the connection to the NXT robot.<br/>
 * Thus it implements {@link IMclRobot} to let the Monte-Carlo-Localization control the robot.
 * Furthermore {@link ChangeListener} is implemented to inform the robot when it should measure the range in different angles.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class Connector implements ChangeListener, IMclRobot<Angle,NXTMove,AbstractRangeReading>, Runnable {
	/**
	 * This is the distance that the ultrasonic sensor of the NXT can reliable measure. Any range reading above this value should be treated as infinity. 
	 */
	public static final double MAX_RELIABLE_RANGE_READING = 180.0d;//cm
	/**
	 * This is the distance that the ultrasonic sensor will return as maximum
	 */
	public static final double MAX_RANGE_READING = 255.0d;//cm
	
	private static enum Message {SET_ANGLES, SET_MIN_DISTANCE, SET_MAX_DISTANCE, GET_RANGES, GET_LINE_MOVE, GET_RANDOM_MOVE, RANGES, MOVE, MOVE_END};
	
	private Message moveType = Message.GET_RANDOM_MOVE;
	private Angle[] rangeReadingAngles;
	private boolean connected = false;
	private NXTConnector connection;
	private DataInputStream in; //only used in the second thread. No synchronization!
	private DataOutputStream out; //synchronized, just to be sure between GUI and MCL/Core!
	private Thread connectionThread;
	private SynchronousQueue<AbstractRangeReading[]> rangeQueue;
	private SynchronousQueue<NXTMove> moveQueue;
	private float minDistance;
	private float maxDistance;
	private double badDelta;
	
	/**
	 * @param rangeReadingAngles the initial angles in which the ranges are read.
	 */
	public Connector(Angle[] rangeReadingAngles) {
		this.rangeReadingAngles = rangeReadingAngles;
		this.rangeQueue = new SynchronousQueue<AbstractRangeReading[]>();
		this.moveQueue = new SynchronousQueue<NXTMove>();
	}
	
	/**
	 * Returns the connection status with the robot.
	 * @return true if a connection is established with the robot.
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Closes the connection with the robot.
	 */
	public void close() {
		if(connected) {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection = null;
			connected=false;
		}
		
	}
	
	/**
	 * Handles an {@code IOException}.
	 */
	private void ioException() {
		close();
		GuiBase.showMessageBox("IOException! Did the Bot turn off?");
	}
	
	/**
	 * Tries to connect to a robot with the given name and start the specified program (which is a {@code MClDaemon} on the NXT.
	 * @param name the name of the NXT robot.
	 * @param program the name of the MCLDaemon program on the NXT.
	 */
	public void connect(String name, String program) {
		connection = new NXTConnector();
		if(!connection.connectTo(name, null, NXTCommFactory.BLUETOOTH, NXTComm.LCP)) {
			GuiBase.showMessageBox("Failed to connect to the NXT.");
			connected = false;
			return;
		}
		NXTCommand command = new NXTCommand(connection.getNXTComm());
		try {
			command.startProgram(program);
		} catch (IOException e) {
			GuiBase.showMessageBox("Failed to start the program.");
			try {
				command.disconnect();
				connection.close();
			} catch (IOException f) {
				//ignore the exception
			}
			connected = false;
			return;
		}
		try {
			command.disconnect();
			connection.close();
		} catch (IOException e) {
			//ignore the exception
		}
		try {
			Thread.sleep(2000); // Wait 2 seconds for program to start 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//CONNECT NORMAL:
		connection = new NXTConnector();
		if(!connection.connectTo(name, null, NXTCommFactory.BLUETOOTH, NXTComm.PACKET)) {
			connected = false;
			return;
		}
		
		in = new DataInputStream(connection.getInputStream());
		out = new DataOutputStream(connection.getOutputStream());
		connected = true;
		
		try{
			sendAngles();
			out.write(Message.SET_MIN_DISTANCE.ordinal());
			out.writeFloat(minDistance);
			out.flush();
			out.write(Message.SET_MAX_DISTANCE.ordinal());
			out.writeFloat(maxDistance);
			out.flush();
		} catch (IOException e) {
			ioException();
		}
		
		if(connectionThread != null) {
			connectionThread.interrupt();
		}
    	connectionThread = new Thread(this);
    	connectionThread.setDaemon(true);
    	connectionThread.start();
	}
	
	private void sendAngles() throws IOException {
		out.write(Message.SET_ANGLES.ordinal());
		out.writeInt(rangeReadingAngles.length);
		for(Angle angle: rangeReadingAngles) {
			out.writeFloat((float) Math.toDegrees(angle.getValue()));
		}
		out.flush();
	}
	
	/**
	 * Sets the minimum move distance and sends it to the robot if one is connected.
	 * @param distance the distance to be set.
	 */
	public void setMinMoveDistance(double distance) {
		minDistance = (float) distance;
		if(connected) {
			try {
				out.write(Message.SET_MIN_DISTANCE.ordinal());
				out.writeFloat((float) distance);
				out.flush();
			} catch (IOException e) {
				ioException();
			}
		}
	}
	
	/**
	 * Sets the maximum move distance and sends it to the robot if one is connected.
	 * @param distance the distance to be set.
	 */
	public void setMaxMoveDistance(double distance) {
		maxDistance = (float) distance;
		if(connected) {
			try {
				out.write(Message.SET_MAX_DISTANCE.ordinal());
				out.writeFloat((float) distance);
				out.flush();
			} catch (IOException e) {
				ioException();
			}
		}
	}
	
	/**
	 * Sets the bad delta for the calculation of the weight.
	 * @param delta the delta to be set.
	 */
	public void setBadDelta(double delta) {
		badDelta = delta;
	}
	
	@Override
	public void notify(Angle[] angles) {
		rangeReadingAngles = angles;
		if(connected) {
			try {
				sendAngles();
			} catch (IOException e) {
				ioException();
			}
		}
	}
	
	@Override
	public AbstractRangeReading[] getRangeReadings() {
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
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public float calculateWeight(AbstractRangeReading robotRange, AbstractRangeReading mapRange) {
		if((robotRange.getValue() < 0 || Double.isInfinite(robotRange.getValue())) && (mapRange.getValue() < 0  || Double.isInfinite(mapRange.getValue()))) return 1;
		final double robotValue;
		if(Double.isInfinite(robotRange.getValue()) || robotRange.getValue() > MAX_RELIABLE_RANGE_READING) robotValue = MAX_RELIABLE_RANGE_READING;
		else robotValue = robotRange.getValue();
		final double mapValue;
		if(Double.isInfinite(mapRange.getValue()) || mapRange.getValue() > MAX_RELIABLE_RANGE_READING) mapValue = MAX_RELIABLE_RANGE_READING;
		else mapValue = mapRange.getValue();
		final double delta = Math.abs(robotValue - mapValue);
		if(delta > badDelta) return 0.0f;
		return (float) (1.0d - delta / badDelta);
	}

	
	@Override
	public NXTMove performMove() {
		if(!connected) return null;
		synchronized(out) {
			try {
				out.write(moveType.ordinal());
				out.flush();
			} catch (IOException e) {
				ioException();
				return null;
			}
		}
		try {
			return moveQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void run() {
		NXTMove currentMove = new NXTMove();
		while(connected) {
			try {
				final int input = in.read();
				if(input < 0) break;
				if(input >= Message.values().length) continue;
				Message message = Message.values()[input];
				switch(message) {
				case RANGES:
					RangeReadings rangeReadings = new RangeReadings(rangeReadingAngles.length);
					rangeReadings.loadObject(in);
					AbstractRangeReading[] ranges = new AbstractRangeReading[rangeReadingAngles.length];
					for(int i=0;i < rangeReadingAngles.length;i++) {
						ranges[i] = new NXTRangeReading(rangeReadings.getRange(i),rangeReadingAngles[i]);
					}
					rangeQueue.put(ranges);
					break;
				case MOVE:
					//Move is posted after each segment of a move has stopped.
					Move move = new Move(false, 0, 0);
					move.loadObject(in);
					currentMove.add(move);
					break;
				case MOVE_END:
					//As the particles shall only be updated once after the complete move has come to an end, this message is needed too.
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
}
