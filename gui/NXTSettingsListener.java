package gui;

import aima.core.robotics.impl.simple.SimpleMove;
import aima.core.robotics.impl.simple.SimpleRangeReading;
import aima.gui.applications.robotics.components.Settings;
import aima.gui.applications.robotics.util.DefaultSettingsListener;
import bot.Connector;

public class NXTSettingsListener extends DefaultSettingsListener {

	public NXTSettingsListener(Settings settingsGui) {
		super(settingsGui);
		settingsGui.registerListener(Settings.MOVE_ROTATION_NOISE_KEY, this);
		settingsGui.registerListener(Settings.MOVE_DISTANCE_NOISE_KEY, this);
		settingsGui.registerListener(Settings.RANGE_READING_NOISE_KEY, this);
	}

	@Override
	public void createSettings() {
		settingsGui.registerSetting(Settings.SENSOR_RANGE_KEY, "Max. sensor range", String.valueOf(Connector.MAX_RELIABLE_RANGE_READING));
		super.createSettings();
		settingsGui.registerSetting(Settings.MOVE_ROTATION_NOISE_KEY, "Move rotation noise", "PI / 90");
		settingsGui.registerSetting(Settings.MOVE_DISTANCE_NOISE_KEY, "Move distance noise", "50.0");
		settingsGui.registerSetting(Settings.RANGE_READING_NOISE_KEY, "Range reading noise", "5.0");
	}
	
	@Override
	public void notify(String key, String value) {
		super.notify(key, value);
		final double valueNumber = Double.parseDouble(value);
		if(key.equals(Settings.MOVE_ROTATION_NOISE_KEY)) {
			SimpleMove.setRotationNoise(valueNumber);
		} else if(key.equals(Settings.MOVE_DISTANCE_NOISE_KEY)) {
			SimpleMove.setMovementNoise(valueNumber);
		} else if(key.equals(Settings.RANGE_READING_NOISE_KEY)) {
			SimpleRangeReading.setRangeNoise(valueNumber);
		}
	}
	
}
