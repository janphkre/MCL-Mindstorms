package de.thkoeln.gm.mcl_mindstorms.robot;

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
import aima.core.robotics.IMclRobot;
import aima.core.robotics.datatypes.RobotException;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.gui.swing.demo.robotics.components.AnglePanel.ChangeListener;
import aima.gui.swing.demo.robotics.util.GuiBase;
import de.thkoeln.gm.mcl_mindstorms.localization.NXTMove;
import de.thkoeln.gm.mcl_mindstorms.localization.NXTRangeReading;

/**
 * This class establishes and manages the connection to the NXT robot.<br/>
 * Thus it implements {@link IMclRobot} to let the Monte-Carlo-Localization control the robot.
 * Furthermore {@link ChangeListener} is implemented to inform the robot when it should measure the range at different angles.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class Connector implements ChangeListener, IMclRobot<Angle,NXTMove,AbstractRangeReading>, Runnable {
	
	/**
	 * The distance that the ultrasonic sensor of the NXT can reliable measure in cm. Any range reading above this value should be treated as infinity. 
	 */
	public static final double MAX_RELIABLE_RANGE_READING = 180.0d;
	/**
	 * The distance that the ultrasonic sensor will return as a maximum in cm.
	 */
	public static final double MAX_RANGE_READING = 255.0d;
	
	private static enum Message {SET_VERBOSE, SET_ANGLES, SET_MIN_DISTANCE, SET_MAX_DISTANCE, SET_ROTATE_SPEED, SET_TRAVEL_SPEED, SET_SAFE_SPACE, SET_COLOR_CUTOFF, SET_LIGHT_CUTOFF, SET_ROTATION_START_ANGLE, GET_RANGES, GET_LINE_MOVE, GET_RANDOM_MOVE, RANGES, MOVE, MOVE_END, FIND_CUTOFFS, CUTOFFS};
	
	private Message moveType = Message.GET_LINE_MOVE;
	private Angle[] rangeReadingAngles;
	private boolean connected = false;
	private NXTConnector connection;
	private DataInputStream in; //only used in the second thread. No synchronization!
	private DataOutputStream out; //synchronized, just to be sure between GUI and MCL/Core!
	private Thread connectionThread;
	private SynchronousQueue<Object> resultQueue;
	private float minDistance;
	private float maxDistance;
	private double rotateSpeed;
	private double travelSpeed;
	private float safeSpace;
	private int colorCutoff;
	private int lightCutoff;
	private int rotationStartAngle;
	private boolean verbose;
	private double badDelta;
	
	/**
	 * @param rangeReadingAngles the initial angles at which the ranges are read.
	 */
	public Connector(Angle[] rangeReadingAngles) {
		this.rangeReadingAngles = rangeReadingAngles;
		this.resultQueue = new SynchronousQueue<Object>();
	}
	
	/**
	 * Returns the status of the connection with the robot.
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
		GuiBase.showMessageBox("IOException! Did the de.thkoeln.gm.mcl_mindstorms.robot die?");
	}
	
	/**
	 * Tries to connect to a robot with the given name and starts the specified program (which is a {@code MClDaemon} on the NXT.
	 * @param name the name of the NXT robot.
	 * @param program the full name of the MCLDaemon program on the NXT.
	 */
	public void connect(String name, String program) {
		GuiBase.showMessageBox("Connecting...",false);
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
		
		sendSettings();
		
		if(connectionThread != null) {
			connectionThread.interrupt();
		}
    	connectionThread = new Thread(this);
    	connectionThread.setDaemon(true);
    	connectionThread.start();
    	GuiBase.hideMessageBox();
	}
	
	/**
	 * Sends all settings to the NXT.
	 */
	private void sendSettings() {
			sendSetting(Message.SET_VERBOSE.ordinal(), verbose);
			sendAngles();
			sendSetting(Message.SET_MIN_DISTANCE.ordinal(), minDistance);
			sendSetting(Message.SET_MAX_DISTANCE.ordinal(), maxDistance);
			sendSetting(Message.SET_ROTATE_SPEED.ordinal(), rotateSpeed);
			sendSetting(Message.SET_TRAVEL_SPEED.ordinal(), travelSpeed);
			sendSetting(Message.SET_SAFE_SPACE.ordinal(), safeSpace);
			sendSetting(Message.SET_COLOR_CUTOFF.ordinal(), colorCutoff);
			sendSetting(Message.SET_LIGHT_CUTOFF.ordinal(), lightCutoff);
			sendSetting(Message.SET_ROTATION_START_ANGLE.ordinal(), rotationStartAngle);
	}
	
	/**
	 * Sends a setting to the NXT.
	 * @param message the message by which the robot recognizes the setting.
	 * @param setting the value of the setting.
	 */
	private void sendSetting(int message, float setting) {
		try {
			out.write(message);
			out.writeFloat(setting);
			out.flush();
		} catch (IOException e) {
			ioException();
		}
	}
	
	/**
	 * Sends a setting to the NXT.
	 * @param message the message by which the robot recognizes the setting.
	 * @param setting the value of the setting.
	 */
	private void sendSetting(int message, double setting) {
		try {
			out.write(message);
			out.writeDouble(setting);
			out.flush();
		} catch (IOException e) {
			ioException();
		}
	}
	
	/**
	 * Sends a setting to the NXT.
	 * @param message the message by which the robot recognizes the setting.
	 * @param setting the value of the setting.
	 */
	private void sendSetting(int message, int setting) {
		try {
			out.write(message);
			out.writeInt(setting);
			out.flush();
		} catch (IOException e) {
			ioException();
		}
	}
	
	/**
	 * Sends a setting to the NXT.
	 * @param message the message by which the robot recognizes the setting.
	 * @param setting the value of the setting.
	 */
	private void sendSetting(int message, boolean setting) {
		try {
			out.write(message);
			out.writeBoolean(setting);
			out.flush();
		} catch (IOException e) {
			ioException();
		}
	}
	
	/**
	 * Sends the angles of the range readings to the robot.
	 */
	private void sendAngles() {
		try {
			out.write(Message.SET_ANGLES.ordinal());
			out.writeInt(rangeReadingAngles.length);
			for(Angle angle: rangeReadingAngles) {
				out.writeFloat((float) Math.toDegrees(angle.getValue()));
			}
			out.flush();
		} catch (IOException e) {
			ioException();
		}
	}
	
	/**
	 * Sets the robot to verbose mode.
	 * @param verbose {@code true} if the robot is to be verbose. 
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
		if(connected) sendSetting(Message.SET_VERBOSE.ordinal(), verbose);
	}
	
	/**
	 * Sets the bad delta for the calculation of the weight.
	 * @param delta the delta to be set.
	 */
	public void setBadDelta(double delta) {
		badDelta = delta;
	}
	
	/**
	 * Sets the minimum move distance and sends it to the robot if one is connected.
	 * @param distance the distance to be set.
	 */
	public void setMinMoveDistance(float distance) {
		maxDistance = distance;
		if(connected) sendSetting(Message.SET_MIN_DISTANCE.ordinal(), distance);
	}
	
	/**
	 * Sets the maximum move distance and sends it to the robot if one is connected.
	 * @param distance the distance to be set.
	 */
	public void setMaxMoveDistance(float distance) {
		maxDistance = distance;
		if(connected) sendSetting(Message.SET_MAX_DISTANCE.ordinal(), distance);
	}
	
	/**
	 * Sets the rotate speed and sends it to the robot if one is connected.
	 * @param speed the rotate speed to be set.
	 */
	public void setRotateSpeed(double speed) {
		rotateSpeed = speed;
		if(connected) sendSetting(Message.SET_ROTATE_SPEED.ordinal(), speed);
	}
	
	/**
	 * Sets the travel speed and sends it to the robot if one is connected.
	 * @param speed the travel speed to be set.
	 */
	public void setTravelSpeed(double speed) {
		travelSpeed = speed;
		if(connected) sendSetting(Message.SET_TRAVEL_SPEED.ordinal(), speed);
	}
	
	/**
	 * Sets the clearance to objects while moving forward and sends it to the robot if one is connected.
	 * @param distance the clearance to be set.
	 */
	public void setClearance(float distance) {
		safeSpace = distance;
		if(connected) sendSetting(Message.SET_SAFE_SPACE.ordinal(), distance);
	}
	
	/**
	 * Sets the cutoff value for the color sensor and sends it to the robot if one is connected.
	 * @param cutoff the cutoff value to be set.
	 */
	public void setColorCutoff(int cutoff) {
		colorCutoff = cutoff;
		if(connected) sendSetting(Message.SET_COLOR_CUTOFF.ordinal(), cutoff);
	}
	
	/**
	 * Sets the cutoff value for the light sensor and sends it to the robot if one is connected.
	 * @param cutoff the cutoff value to be set.
	 */
	public void setLightCutoff(int cutoff) {
		lightCutoff = cutoff;
		if(connected) sendSetting(Message.SET_LIGHT_CUTOFF.ordinal(), cutoff);
	}
	
	/**
	 * Sets the angle at which the robot initially rotates to find the line and sends it to the robot if one is connected.
	 * @param angle the angle to be set.
	 */
	public void setRotationStartAngle(int angle) {
		rotationStartAngle = angle;
		if(connected) sendSetting(Message.SET_ROTATION_START_ANGLE.ordinal(), angle);
	}
	
	/**
	 * Makes the robot find cut off values for the color and light sensors.
	 * @return returns the found cutoff values. {@code null} will be returned if the robot is not connected.
	 * @throws RobotException thrown if an error occurred.
	 */
	public int[] findCutoffs() throws RobotException {
		if(!connected) return null;
		try {
			out.write(Message.FIND_CUTOFFS.ordinal());
			out.flush();
		} catch(IOException e) {
			ioException();
		}
		try {
			Object result = resultQueue.take();
			if(result instanceof int[]) return (int[]) result;
			else throw new RobotException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RobotException();
		}
	}
	
	@Override
	public void notify(Angle[] angles) {
		rangeReadingAngles = angles;
		if(connected)sendAngles();
	}
	
	@Override
	public AbstractRangeReading[] getRangeReadings() throws RobotException {
		if(!connected) return null;
		synchronized(out) {
			try {
				out.write(Message.GET_RANGES.ordinal());
				out.flush();
			} catch (IOException e) {
				ioException();
				throw new RobotException();
			}
		}
		try {
			Object result = resultQueue.take();
			if(result instanceof AbstractRangeReading[]) return (AbstractRangeReading[]) result;
			else throw new RobotException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RobotException();
		}
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
	public NXTMove performMove() throws RobotException {
		if(!connected) return null;
		synchronized(out) {
			try {
				out.write(moveType.ordinal());
				out.flush();
			} catch (IOException e) {
				ioException();
				throw new RobotException();
			}
		}
		try {
			Object result = resultQueue.take();
			if(result instanceof NXTMove) return (NXTMove) result;
			else throw new RobotException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RobotException();
		}
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
					resultQueue.put(ranges);
					break;
				case MOVE:
					//Sent after each segment of a move has stopped.
					Move move = new Move(false, 0, 0);
					move.loadObject(in);
					currentMove.add(move);
					break;
				case MOVE_END:
					//As the particles shall only be updated once after the complete move has come to an end, this message is needed too.
					resultQueue.put(currentMove);
					currentMove = new NXTMove();
					break;
				case CUTOFFS:
					//The values that have been found for the sensors as cutoff values.
					int[] values = new int[2];
					values[0] = in.readInt();
					values[1] = in.readInt();
					resultQueue.put(values);
					break;
				default:
				}
			} catch (IOException e) {
				ioException();
				resultQueue.offer(new Object());
				break;
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
