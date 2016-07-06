package gui;

import java.io.File;

import aima.core.robotics.IMclRobot;
import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.util.math.geom.SVGGroupParser;
import aima.gui.applications.robotics.MonteCarloLocalizationApp;
import aima.gui.applications.robotics.components.AnglePanel;
import aima.gui.applications.robotics.components.IRobotGui;
import aima.gui.applications.robotics.components.Settings;
import bot.Connector;
import localization.NXTMove;
import localization.NXTPosition;
import localization.NXTPositionFactory;
import localization.NXTRangeReading;
import localization.NXTRangeReadingFactory;

public class GuiMain extends MonteCarloLocalizationApp<NXTPosition, NXTMove, NXTRangeReading> {
	
	public GuiMain(MonteCarloLocalization<NXTPosition, Angle, NXTMove, RangeReading> mcl, MclCartesianPlot2D<NXTPosition, NXTMove, RangeReading> map, IMclRobot<Angle, NXTMove, RangeReading> robot, IRobotGui robotGui, Settings settingsGui) {
		super(mcl, map, robot, robotGui, settingsGui);
	}

	public static void main(String[] args) {
		Settings settingsGui = MonteCarloLocalizationApp.buildSettings(args.length > 0 ? new File(args[0]) : DEFAULT_SETTINGS_FILE);
		NXTSettingsListener settingsListener = new NXTSettingsListener(settingsGui);
		settingsListener.createSettings();
		
		AnglePanel angles = new AnglePanel();
		settingsGui.registerSpecialSetting(RANGE_READING_ANGLES_KEY, angles);
		
		MclCartesianPlot2D<NXTPosition, NXTMove, RangeReading> map = new MclCartesianPlot2D<NXTPosition,NXTMove,RangeReading>(new SVGGroupParser(),new SVGGroupParser(),new NXTPositionFactory(),new NXTRangeReadingFactory());
		Connector robot = new Connector(angles.getSetting());
		MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl = new MonteCarloLocalization<NXTPosition, Angle, NXTMove, RangeReading>(map, robot);
		GuiMain app = new GuiMain(mcl, map, robot, new NXTRobotGui(robot), settingsGui);
		
		angles.setChangeListener(robot);
		settingsListener.setMap(map);
		settingsListener.setMcl(mcl);
		settingsListener.setRobot(robot);
		settingsGui.notifyAllListeners();
		app.show();
	}
}
