package gui;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.util.math.geom.SVGGroupParser;
import aima.gui.applications.robotics.GenericMonteCarloLocalization2DApp;
import aima.gui.applications.robotics.MonteCarloLocalizationApp;
import aima.gui.applications.robotics.components.AnglePanel;
import bot.Connector;
import localization.NXTMove;
import localization.NXTPosition;
import localization.NXTPositionFactory;
import localization.NXTRangeReading;
import localization.NXTRangeReadingFactory;

public class NXTApp extends MonteCarloLocalizationApp  {

	public NXTApp() {
		super();
	}
	
	public NXTApp(String[] args) {
		super(args);
	}

	public static void main(String[] args) {
		NXTApp app = new NXTApp(args);
		app.constructApplicationFrame();
		app.show();
	}
	
	@Override
	protected void initialize() {
		NXTSettingsListener settingsListener = new NXTSettingsListener(settingsGui);
		settingsListener.createSettings();
		
		AnglePanel angles = new AnglePanel();
		settingsGui.registerSpecialSetting(RANGE_READING_ANGLES_KEY, angles);
		
		MclCartesianPlot2D<NXTPosition, NXTMove, RangeReading> map = new MclCartesianPlot2D<NXTPosition,NXTMove,RangeReading>(new SVGGroupParser(),new SVGGroupParser(),new NXTPositionFactory(),new NXTRangeReadingFactory());
		Connector robot = new Connector(angles.getSetting());
		MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl = new MonteCarloLocalization<NXTPosition, Angle, NXTMove, RangeReading>(map, robot);
		app = new GenericMonteCarloLocalization2DApp<NXTPosition,NXTMove,NXTRangeReading>(mcl, map, robot, new NXTRobotGui(robot), settingsGui);
		
		angles.setChangeListener(robot);
		settingsListener.setMap(map);
		settingsListener.setMcl(mcl);
		settingsListener.setRobot(robot);
		settingsGui.notifyAllListeners();
		app.show();
	}
}
