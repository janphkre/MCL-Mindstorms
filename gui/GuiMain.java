package gui;

import java.io.File;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.util.math.geom.SVGGroupParser;
import aima.gui.applications.robotics.MonteCarloLocalizationApp;
import aima.gui.applications.robotics.components.AnglePanel;
import aima.gui.applications.robotics.components.Settings;
import aima.gui.applications.robotics.util.SimpleSettingsListener;
import bot.Connector;
import localization.NXTMove;
import localization.NXTPosition;
import localization.NXTPositionFactory;
import localization.NXTRangeReading;
import localization.NXTRangeReadingFactory;

public class GuiMain {
	
	private static final File DEFAULT_SETTINGS_FILE = new File("");//TODO
	
	public static void main(String[] args) {
		Settings settingsGui = MonteCarloLocalizationApp.buildSettings(args.length > 0 ? new File(args[0]) : DEFAULT_SETTINGS_FILE);
		NXTSettingsListener settingsListener = new NXTSettingsListener(settingsGui);
		settingsListener.createSettings();
		settingsGui.registerSetting(SimpleSettingsListener.PARTICLE_COUNT_KEY, "Particle count", "1000");
		
		final double sensorRange = Double.parseDouble(settingsGui.getSetting(NXTSettingsListener.SENSOR_RANGE_KEY));
		final int particleCount = Integer.parseInt(settingsGui.getSetting(SimpleSettingsListener.PARTICLE_COUNT_KEY));
		final double minWeight = Double.parseDouble(settingsGui.getSetting(NXTSettingsListener.MIN_WEIGHT_KEY));
		final double maxDistance = Double.parseDouble(settingsGui.getSetting(NXTSettingsListener.MAX_DISTANCE_KEY));
		AnglePanel angles = new AnglePanel();
		settingsGui.registerSpecialSetting(NXTSettingsListener.RANGE_READING_ANGLES_KEY, angles);
		
		MclCartesianPlot2D<NXTPosition, NXTMove, RangeReading> map = new MclCartesianPlot2D<NXTPosition,NXTMove,RangeReading>(new SVGGroupParser(),new SVGGroupParser(),new NXTPositionFactory(),new NXTRangeReadingFactory(),sensorRange);
		Connector robot = new Connector(angles.getSetting());
		MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl = new MonteCarloLocalization<NXTPosition, Angle, NXTMove, RangeReading>(map, robot, particleCount, minWeight, maxDistance);
		MonteCarloLocalizationApp<NXTPosition, NXTMove, NXTRangeReading> app = new MonteCarloLocalizationApp<NXTPosition,NXTMove,NXTRangeReading>(mcl, map, robot, new NXTRobotGui(robot), settingsGui);
		
		angles.setChangeListener(robot);
		settingsListener.setMap(map);
		settingsListener.setMcl(mcl);
		settingsListener.setRobot(robot);
		settingsGui.notifyAllListeners();
		app.show();
	}
}
