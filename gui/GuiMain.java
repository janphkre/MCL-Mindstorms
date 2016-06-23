package gui;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.MclCartesianPlot2D;
import aima.core.util.math.geom.SVGGroupParser;
import aima.gui.applications.robotics.MonteCarloLocalizationApp;
import aima.gui.applications.robotics.IRobotGui;
import bot.Connector;
import localization.NXTMove;
import localization.NXTPosition;
import localization.NXTPositionFactory;
import localization.NXTRangeReading;
import localization.NXTRangeReadingFactory;

public class GuiMain {
	
	private static final Angle[] RANGE_READING_ANGLES = {
			new Angle(-Math.PI/2),
			new Angle(-Math.PI/4),
			new Angle(0d),
			new Angle(Math.PI/4),
			new Angle(Math.PI/2)};
	private static final int PARTICLE_COUNT = 200;
	private static final double MIN_WEIGHT = Math.pow(0.1,RANGE_READING_ANGLES.length);
	private static final double MAX_DISTANCE = 2;//cm
	
	public static void main(String[] args) {
		Connector robot = new Connector(RANGE_READING_ANGLES);
		MclCartesianPlot2D<NXTPosition, NXTMove, RangeReading> map = new MclCartesianPlot2D<NXTPosition,NXTMove,RangeReading>(new SVGGroupParser(),new SVGGroupParser(),new NXTPositionFactory(),new NXTRangeReadingFactory(),Connector.MAX_RELIABLE_RANGE_READING);
		MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl = new MonteCarloLocalization<NXTPosition, Angle, NXTMove, RangeReading>(map, robot, PARTICLE_COUNT, MIN_WEIGHT, MAX_DISTANCE);
		new MonteCarloLocalizationApp<NXTPosition,NXTMove,NXTRangeReading>(mcl, map, robot, new NXTRobotGui(robot));
	}
}
