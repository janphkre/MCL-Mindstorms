package de.thkoeln.gm.mcl_mindstorms.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import aima.core.robotics.datatypes.RobotException;
import aima.gui.swing.demo.robotics.GenericMonteCarloLocalization2DApp;
import aima.gui.swing.demo.robotics.components.AbstractSettingsListener;
import aima.gui.swing.demo.robotics.components.ButtonPanel;
import aima.gui.swing.demo.robotics.components.IRobotGui;
import aima.gui.swing.demo.robotics.components.Settings;
import de.thkoeln.gm.mcl_mindstorms.localization.NXTMove;
import de.thkoeln.gm.mcl_mindstorms.localization.NXTRangeReading;
import de.thkoeln.gm.mcl_mindstorms.robot.Connector;

/**
 * This settings listener is used for the NXT environment.
 * It applies all setting changes in the settings GUI on the corresponding parameters except the particle count which is managed internally in the {@link GenericMonteCarloLocalization2DApp}.<br/>
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
@SuppressWarnings("javadoc")
public final class NXTSettingsListener extends AbstractSettingsListener {
	
	public static final String BAD_DELTA_KEY = "BAD_DELTA";
	public static final String MIN_MOVE_DISTANCE_KEY = "MIN_MOVE_DISTANCE";
	public static final String MAX_MOVE_DISTANCE_KEY = "MAX_MOVE_DISTANCE";
	public static final String MOVE_ROTATION_NOISE_KEY = "MOVE_ROTATION_NOISE";
	public static final String MOVE_DISTANCE_NOISE_KEY = "MOVE_DISTANCE_NOISE";
	public static final String RANGE_READING_NOISE_KEY = "RANGE_READING_NOISE";
	public static final String ROTATE_SPEED_KEY = "ROTATE_SPEED";
	public static final String TRAVEL_SPEED_KEY = "TRAVEL_SPEED";
	public static final String CLEARANCE_KEY = "CLEARANCE";
	public static final String COLOR_CUTOFF_KEY = "COLOR_CUTOFF";
	public static final String LIGHT_CUTOFF_KEY = "LIGHT_CUTOFF";
	public static final String ROTATION_START_ANGLE_KEY = "ROTATION_START_ANGLE";
	public static final String VERBOSE_ROBOT_KEY = "VERBOSE_ROBOT";
	public static final String CUTOFF_BUTTON_KEY = "CUTOFF_BUTTON";
	private Connector connector;
	private IRobotGui robotGui;
	private GenericMonteCarloLocalization2DApp<?, ?, ?> mainApp;
	
	/**
	 * @param settingsGui the {@link Settings} on which this class should register itself.
	 */
	public NXTSettingsListener(Settings settingsGui) {
		super(settingsGui);
	}
	
	/**
	 * Sets the {@link Connector} on which the settings will be updated.
	 * @param connector the robot to be kept up to date with the correct parameters.
	 * @param robotGui the GUI associated with the connector.
	 */
	public void setRobot(Connector connector, IRobotGui robotGui) {
		this.connector = connector;
		this.robotGui = robotGui;
	}
	
	/**
	 * Sets the {@link GenericMonteCarloLocalization2DApp} on which the settings schedules a runnable to be run in background.
	 * @param mainApp the main application on which a backgroundThread exists.
	 */
	public void setMainApp(GenericMonteCarloLocalization2DApp<?, ?, ?> mainApp) {
		this.mainApp = mainApp;
	}
	
	@Override
	public void createSettings() {
		Runnable autoTune = new Runnable() {
			@Override
			public void run() {
				try {
					final int[] values = connector.findCutoffs();
					if(values == null) robotGui.notifyInitialize();
					else {
						settingsGui.setSetting(COLOR_CUTOFF_KEY, String.valueOf(values[0]));
						settingsGui.setSetting(LIGHT_CUTOFF_KEY, String.valueOf(values[1]));
						settingsGui.updateGuiSetting(COLOR_CUTOFF_KEY);
						settingsGui.updateGuiSetting(LIGHT_CUTOFF_KEY);
					}
				} catch (RobotException e) {
					/*A RobotException may be thrown if the robot disconnected for some reason.*/
				}
				settingsGui.enableGuiButtons();
			}
		};
		ButtonPanel buttonPanel = new ButtonPanel("Autotune cutoffs", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				settingsGui.disableGuiButtons();
				mainApp.runInBackground(autoTune);
			}
		});
		
		settingsGui.registerSetting(PARTICLE_COUNT_KEY, "Particle count", "2000");
		settingsGui.registerSetting(REMEMBER_FACTOR_KEY, "Remember factor", "0.8");
		settingsGui.registerSetting(SENSOR_RANGE_KEY, "Max. sensor range", "400.0");
		settingsGui.registerSetting(MIN_WEIGHT_KEY, "Min. particle weight", "0.0");
		
		settingsGui.registerSetting(BAD_DELTA_KEY, "Bad range delta", "100.0");
		settingsGui.registerSetting(MAX_DISTANCE_KEY, "Max. particle distance", "15.0");
		settingsGui.registerSetting(MOVE_ROTATION_NOISE_KEY, "Move rotation noise (deg)", "20");
		settingsGui.registerSetting(MOVE_DISTANCE_NOISE_KEY, "Move distance noise", "20.7188");
		settingsGui.registerSetting(RANGE_READING_NOISE_KEY, "Range reading noise", "0.4486");
		settingsGui.registerSetting(MIN_MOVE_DISTANCE_KEY, "Min. move distance", "10.0");
		settingsGui.registerSetting(MAX_MOVE_DISTANCE_KEY, "Max. move distance", "40.6");
		settingsGui.registerSetting(ROTATE_SPEED_KEY, "Rotation speed", "100.0");
		settingsGui.registerSetting(TRAVEL_SPEED_KEY, "Travel speed", "50.0");
		settingsGui.registerSetting(CLEARANCE_KEY, "Object clearance", "25.0");
		settingsGui.registerSetting(COLOR_CUTOFF_KEY, "Color cutoff", "5");
		settingsGui.registerSetting(LIGHT_CUTOFF_KEY, "Light cutoff", "40");
		settingsGui.registerSpecialSetting(CUTOFF_BUTTON_KEY, buttonPanel);
		settingsGui.registerSetting(ROTATION_START_ANGLE_KEY, "Search start angle", "5");
		settingsGui.registerSetting(VERBOSE_ROBOT_KEY, "Verbose NXT", "false");

		registerAbstractListener();
		registerNXTListener();
	}
	
	/**
	 * Registers this object as a listener for its settings.
	 */
	protected void registerNXTListener() {
		settingsGui.registerListener(MOVE_ROTATION_NOISE_KEY, this);
		settingsGui.registerListener(MOVE_DISTANCE_NOISE_KEY, this);
		settingsGui.registerListener(RANGE_READING_NOISE_KEY, this);
		settingsGui.registerListener(MIN_MOVE_DISTANCE_KEY, this);
		settingsGui.registerListener(MAX_MOVE_DISTANCE_KEY, this);
		settingsGui.registerListener(BAD_DELTA_KEY, this);
		settingsGui.registerListener(ROTATE_SPEED_KEY, this);
		settingsGui.registerListener(TRAVEL_SPEED_KEY, this);
		settingsGui.registerListener(CLEARANCE_KEY, this);
		settingsGui.registerListener(COLOR_CUTOFF_KEY, this);
		settingsGui.registerListener(LIGHT_CUTOFF_KEY, this);
		settingsGui.registerListener(ROTATION_START_ANGLE_KEY, this);
		settingsGui.registerListener(VERBOSE_ROBOT_KEY, this);
		
	}
	
	/**
	 * Notifier for all elements that use a {@code double} value.
	 * @param key the key to which the value belongs.
	 * @param value the value that (may have) changed.
	 * @return true if the key was found.
	 */
	private boolean notifyDouble(String key, String value) {
		try {
			final double valueNumber = Double.parseDouble(value);
			super.notifySetting(key, value);
			if(key.equals(MOVE_ROTATION_NOISE_KEY)) {
				NXTMove.setRotationNoise(valueNumber);
			} else if(key.equals(MOVE_DISTANCE_NOISE_KEY)) {
				NXTMove.setMovementNoise(valueNumber);
			} else if(key.equals(RANGE_READING_NOISE_KEY)) {
				NXTRangeReading.setRangeNoise(valueNumber);
			} else if(key.equals(MIN_MOVE_DISTANCE_KEY)) {
				connector.setMinMoveDistance((float) valueNumber);
			} else if(key.equals(MAX_MOVE_DISTANCE_KEY)) {
				connector.setMaxMoveDistance((float) valueNumber);
			} else if(key.equals(BAD_DELTA_KEY)) {
				connector.setBadDelta(valueNumber);
			} else if(key.equals(ROTATE_SPEED_KEY)) {
				connector.setRotateSpeed(valueNumber);
			} else if(key.equals(TRAVEL_SPEED_KEY)) {
				connector.setTravelSpeed(valueNumber);
			} else if(key.equals(CLEARANCE_KEY)) {
				connector.setClearance((float) valueNumber);
			} else if(key.equals(COLOR_CUTOFF_KEY)) {
				connector.setColorCutoff((int) valueNumber);
			} else if(key.equals(LIGHT_CUTOFF_KEY)) {
				connector.setLightCutoff((int) valueNumber);
			} else if(key.equals(ROTATION_START_ANGLE_KEY)) {
				connector.setRotationStartAngle((int) valueNumber);
			} else {
				return false;
			}
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Notifier for all elements that use a {@code boolean} value.
	 * @param key the key to which the value belongs.
	 * @param value the value that (may have) changed.
	 * @return true if the key was found.
	 */
	private boolean notifyBoolean(String key, String value) {
		final boolean valueBoolean = Boolean.parseBoolean(value);
		if(key.equals(VERBOSE_ROBOT_KEY)) {
			connector.setVerbose(valueBoolean);
		} else {
			return false;
		}
		return true;
	}
	
	@Override
	public void notifySetting(String key, String value) {
		if(notifyDouble(key, value)) return;
		notifyBoolean(key, value);
	}
}
