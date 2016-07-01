package gui;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.robotics.impl.simple.SimpleMove;
import aima.core.robotics.impl.simple.SimpleRangeReading;
import aima.core.robotics.impl.simple.VirtualRobot;
import aima.gui.applications.robotics.components.Settings;
import bot.Connector;
import localization.NXTMove;
import localization.NXTRangeReading;

public class NXTSettingsListener implements Settings.ISettingsListener {

	public static final String SENSOR_RANGE_KEY = "SENSOR_RANGE";
	public static final String RANGE_READING_ANGLES_KEY = "RANGE_READING_ANGLES";
	public static final String MIN_MOVE_DISTANCE_KEY = "MIN_MOVE_DISTANCE";
	public static final String MAX_MOVE_DISTANCE_KEY = "MAX_MOVE_DISTANCE";
	public static final String MIN_WEIGHT_KEY = "MIN_WEIGHT";
	public static final String MAX_DISTANCE_KEY = "MAX_DISTANCE";
	public static final String MOVE_ROTATION_NOISE_KEY = "MOVE_ROTATION_NOISE";
	public static final String MOVE_DISTANCE_NOISE_KEY = "MOVE_DISTANCE_NOISE";
	public static final String RANGE_READING_NOISE_KEY = "RANGE_READING_NOISE";
	public static final String BAD_DELTA_KEY = "BAD_DELTA";
	
	private Settings settingsGui;
	private MclCartesianPlot2D<?,?,?> map;
	private MonteCarloLocalization<?,?,?,?> mcl;
	private Connector connector;
	
	public NXTSettingsListener(Settings settingsGui) {
		this.settingsGui = settingsGui;
	}
	
	public void setMap(MclCartesianPlot2D<?,?,?> map) {
		this.map = map;
	}
	
	public void setMcl(MonteCarloLocalization<?,?,?,?> mcl) {
		this.mcl = mcl;
	}
	
	public void setRobot(Connector connector) {
		this.connector = connector;
	}
	
	public void createSettings() {
		settingsGui.registerSetting(SENSOR_RANGE_KEY, "Max. sensor range", "400.0");
		settingsGui.registerSetting(MIN_WEIGHT_KEY, "Min. particle weight", "0.1");
		settingsGui.registerSetting(MAX_DISTANCE_KEY, "Max. particle distance", "5.0");
		settingsGui.registerSetting(MOVE_ROTATION_NOISE_KEY, "Move rotation noise (rad)", "0.03490658504");
		settingsGui.registerSetting(MOVE_DISTANCE_NOISE_KEY, "Move distance noise", "3.0");
		settingsGui.registerSetting(RANGE_READING_NOISE_KEY, "Range reading noise", "0.0001");
		settingsGui.registerSetting(MIN_MOVE_DISTANCE_KEY, "Min. move distance", "10.0");
		settingsGui.registerSetting(MAX_MOVE_DISTANCE_KEY, "Max. move distance", "30.0");
		//settingsGui.registerSetting(BAD_DELTA_KEY, "Bad range delta", "300.0");
		
		settingsGui.registerListener(SENSOR_RANGE_KEY, this);
		settingsGui.registerListener(MIN_WEIGHT_KEY, this);
		settingsGui.registerListener(MAX_DISTANCE_KEY, this);
		settingsGui.registerListener(MOVE_ROTATION_NOISE_KEY, this);
		settingsGui.registerListener(MOVE_DISTANCE_NOISE_KEY, this);
		settingsGui.registerListener(RANGE_READING_NOISE_KEY, this);
		settingsGui.registerListener(MIN_MOVE_DISTANCE_KEY, this);
		settingsGui.registerListener(MAX_MOVE_DISTANCE_KEY, this);
		//settingsGui.registerListener(BAD_DELTA_KEY, this);
	}
	
	@Override
	public void notify(String key, String value) {
		final double valueNumber = Double.parseDouble(value);
		if(key.equals(SENSOR_RANGE_KEY)) {
			map.setSensorRange(valueNumber);
		} else if(key.equals(MIN_WEIGHT_KEY)) {
			mcl.setWeightCutOff(valueNumber);
		} else if(key.equals(MAX_DISTANCE_KEY)) {
			mcl.setMaxDistance(valueNumber);
		} else if(key.equals(MOVE_ROTATION_NOISE_KEY)) {
			NXTMove.setRotationNoise(valueNumber);
		} else if(key.equals(MOVE_DISTANCE_NOISE_KEY)) {
			NXTMove.setMovementNoise(valueNumber);
		} else if(key.equals(RANGE_READING_NOISE_KEY)) {
			NXTRangeReading.setRangeNoise(valueNumber);
		} else if(key.equals(MIN_MOVE_DISTANCE_KEY)) {
			connector.setMinMoveDistance(valueNumber);
		} else if(key.equals(MAX_MOVE_DISTANCE_KEY)) {
			connector.setMaxMoveDistance(valueNumber);
		}/* else if(key.equals(BAD_DELTA_KEY)) {
			connector.setBadDelta(valueNumber);
		}*/
	}
	
}
