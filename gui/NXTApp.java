package gui;

import java.io.File;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.util.math.geom.SVGGroupParser;
import aima.gui.applications.robotics.GenericMonteCarloLocalization2DApp;
import aima.gui.applications.robotics.MonteCarloLocalizationApp;
import aima.gui.applications.robotics.components.AnglePanel;
import bot.Connector;
import localization.NXTMove;
import localization.NXTPose;
import localization.NXTPositionFactory;
import localization.NXTRangeReading;
import localization.NXTRangeReadingFactory;

/**
 * Extends {@link MonteCarloLocalizationApp} to be able to use the {@link GenericMonteCarloLocalization2DApp} for the NXT environment. 
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class NXTApp extends MonteCarloLocalizationApp  {

	/**
	 * Starts the application.
	 * @param args a path to the file containing the settings may be passed as the first argument. Otherwise the default settings file {@code mcl_settings.cache} in the working directory is used.
	 */
	public static void main(String[] args) {
		File settingsFile = args.length > 0 ? new File(args[0]) : DEFAULT_SETTINGS_FILE;
		NXTApp app = new NXTApp(settingsFile);
		app.constructApplicationFrame();
		app.show();
	}
	
	/**
	 * @param settingsFile the file containing the settings for this Monte-Carlo-Localization.
	 */
	public NXTApp(File settingsFile) {
		super(settingsFile);
	}
	
	@Override
	protected void initialize() {
		NXTSettingsListener settingsListener = new NXTSettingsListener(settingsGui);
		settingsListener.createSettings();
		
		AnglePanel angles = new AnglePanel();
		settingsGui.registerSpecialSetting(RANGE_READING_ANGLES_KEY, angles);
		
		MclCartesianPlot2D<NXTPose, NXTMove, AbstractRangeReading> map = new MclCartesianPlot2D<NXTPose,NXTMove,AbstractRangeReading>(new SVGGroupParser(),new SVGGroupParser(),new NXTPositionFactory(),new NXTRangeReadingFactory());
		Connector robot = new Connector(angles.getAngles());
		robotGui = new NXTRobotGui(robot);
		MonteCarloLocalization<NXTPose,Angle,NXTMove,AbstractRangeReading> mcl = new MonteCarloLocalization<NXTPose, Angle, NXTMove, AbstractRangeReading>(map, robot);
		app = new GenericMonteCarloLocalization2DApp<NXTPose,NXTMove,NXTRangeReading>(mcl, map, robot, robotGui, settingsGui);
		
		angles.setChangeListener(robot);
		settingsListener.setMap(map);
		settingsListener.setMcl(mcl);
		settingsListener.setRobot(robot);
	}
}
