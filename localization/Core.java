package localization;

import aima.core.robotics.impl.MonteCarloLocalization;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.CartesianPlot2D;
import aima.core.robotics.impl.map.SVGGroupParser;
import bot.Connector;
import gui.GuiMain;

public class Core implements Runnable  {
	
	private static final Angle[] RANGE_READING_ANGLES = {
			new Angle(-Math.PI/2),
			new Angle(-Math.PI/4),
			new Angle(  0d),
			new Angle(Math.PI/4),
			new Angle(Math.PI/2)};
	private static final int PARTICLE_COUNT = 200;
	private static final double MIN_WEIGHT = Math.pow(0.1,RANGE_READING_ANGLES.length);
	private static final double MAX_DISTANCE = 10d;
	
	private int pause; //milliseconds
	
	private GuiMain gui;
	private CartesianPlot2D<NXTPosition,NXTMove,NXTRangeReading> map;
	private Connector connector;
	private MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl;
	
	public Core(GuiMain gui) {
		this.pause = 1000;
		this.gui = gui;
		this.connector = new Connector(RANGE_READING_ANGLES);
		this.map = new CartesianPlot2D<NXTPosition,NXTMove,NXTRangeReading>(new SVGGroupParser(), new NXTPositionFactory(), new NXTRangeReadingFactory(),connector.getMaxSensorRange());
		this.mcl = new MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading>(map, connector, PARTICLE_COUNT, MIN_WEIGHT, MAX_DISTANCE);
	}
	
	public void move() {
		NXTMove move = connector.performMove();
		gui.displayMove(move);
		mcl.applyMove(move);
		gui.displayParticles(mcl.getParticleIterator());
	}
	
	public void rangeReading() {
		RangeReading[] rangeReadings = connector.getRangeReadings();
		gui.displayRangeReadings(rangeReadings);
		mcl.weightParticles(rangeReadings);
		gui.displayParticles(mcl.getParticleIterator());
	}
	
	public NXTPosition resample() {
		mcl.resampleParticles();
		gui.displayParticles(mcl.getParticleIterator());
		return mcl.getPose();
	}
	
	@Override
	public void run() {
		gui.displayParticles(mcl.getParticleIterator());
		pause();
		NXTPosition result = null;
		rangeReading();
		gui.notify();
		while(result == null) {
			//1. Move the Robot:
			move();
			gui.notify();
			//2. WeightParticles through the Ranges:
			rangeReading();
			gui.notify();
			pause();
			//3. Reselection  of Particles:
			result = resample();
			gui.notify();
		}
		gui.displayResult(result);
		gui.algorithmFinished();
		gui.notify();
	}
	
	private void pause() {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main() {
		GuiMain gui = new GuiMain();
		Core core = new Core(gui);
		Thread thread = new Thread(core);
		thread.start();
		try {
			gui.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
