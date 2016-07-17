package gui;

import aima.gui.applications.robotics.GenericMonteCarloLocalization2DApp;
import aima.gui.applications.robotics.components.AbstractSettingsListener;
import aima.gui.applications.robotics.components.Settings;
import bot.Connector;
import localization.NXTMove;
import localization.NXTRangeReading;

/**
 * This settings listener is used for the NXT environment.
 * It applies all setting changes in the settings GUI on the corresponding parameters except the particle count which is managed internally in the {@link GenericMonteCarloLocalization2DApp}.<br/>
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public class NXTSettingsListener extends AbstractSettingsListener {

	public static final String MIN_MOVE_DISTANCE_KEY = "MIN_MOVE_DISTANCE";
	public static final String MAX_MOVE_DISTANCE_KEY = "MAX_MOVE_DISTANCE";
	public static final String MOVE_ROTATION_NOISE_KEY = "MOVE_ROTATION_NOISE";
	public static final String MOVE_DISTANCE_NOISE_KEY = "MOVE_DISTANCE_NOISE";
	public static final String RANGE_READING_NOISE_KEY = "RANGE_READING_NOISE";
	public static final String BAD_DELTA_KEY = "BAD_DELTA";

	private Connector connector;
	
	/**
	 * @param settingsGui the {@link Settings} on which this class should register itself and its settings that will be used.
	 */
	public NXTSettingsListener(Settings settingsGui) {
		super(settingsGui);
	}
	
	/**
	 * Sets the {@link Connector} on which the settings will be updated.
	 * @param connector the robot to be kept up to date with the correct parameters.
	 */
	public void setRobot(Connector connector) {
		this.connector = connector;
	}
	
	public void createSettings() {
		settingsGui.registerSetting(PARTICLE_COUNT_KEY, "Particle count", "2000");
		settingsGui.registerSetting(REMEMBER_FACTOR_KEY, "Remember factor", "0.8");
		settingsGui.registerSetting(SENSOR_RANGE_KEY, "Max. sensor range", "400.0");
		settingsGui.registerSetting(MIN_WEIGHT_KEY, "Min. particle weight", "0.0");
		
		settingsGui.registerSetting(MAX_DISTANCE_KEY, "Max. particle distance", "15.0");
		settingsGui.registerSetting(MOVE_ROTATION_NOISE_KEY, "Move rotation noise (rad)", "0.3647");
		settingsGui.registerSetting(MOVE_DISTANCE_NOISE_KEY, "Move distance noise", "20.7188");
		settingsGui.registerSetting(RANGE_READING_NOISE_KEY, "Range reading noise", "0.4486");
		settingsGui.registerSetting(MIN_MOVE_DISTANCE_KEY, "Min. move distance", "10.0");
		settingsGui.registerSetting(MAX_MOVE_DISTANCE_KEY, "Max. move distance", "40.6");
		settingsGui.registerSetting(BAD_DELTA_KEY, "Bad range delta", "100.0");
		
		registerAbstractListener();
		registerNXTListener();
	}
	
	protected void registerNXTListener() {
		settingsGui.registerListener(MOVE_ROTATION_NOISE_KEY, this);
		settingsGui.registerListener(MOVE_DISTANCE_NOISE_KEY, this);
		settingsGui.registerListener(RANGE_READING_NOISE_KEY, this);
		settingsGui.registerListener(MIN_MOVE_DISTANCE_KEY, this);
		settingsGui.registerListener(MAX_MOVE_DISTANCE_KEY, this);
		settingsGui.registerListener(BAD_DELTA_KEY, this);
	}
	
	@Override
	public void notify(String key, String value) {
		super.notify(key, value);
		final double valueNumber = Double.parseDouble(value);
		if(key.equals(MOVE_ROTATION_NOISE_KEY)) {
			NXTMove.setRotationNoise(valueNumber);
		} else if(key.equals(MOVE_DISTANCE_NOISE_KEY)) {
			NXTMove.setMovementNoise(valueNumber);
		} else if(key.equals(RANGE_READING_NOISE_KEY)) {
			NXTRangeReading.setRangeNoise(valueNumber);
		} else if(key.equals(MIN_MOVE_DISTANCE_KEY)) {
			connector.setMinMoveDistance(valueNumber);
		} else if(key.equals(MAX_MOVE_DISTANCE_KEY)) {
			connector.setMaxMoveDistance(valueNumber);
		} else if(key.equals(BAD_DELTA_KEY)) {
			connector.setBadDelta(valueNumber);
		}
	}
	
}
