package localization;

import bot.Connector;
import gui.GuiMain;
import robotics.MonteCarloLocalization;
import robotics.concrete.Map2D;
import robotics.concrete.datatypes.Angle;
import robotics.concrete.datatypes.RangeReading;

public class Core implements Runnable  {
	
	private static final int PARTICLE_COUNT = 200;
	private static final double MIN_WEIGHT = 0.001d;
	private static final double MAX_DISTANCE = 10d;
	
	private int pause; //milliseconds
	
	private GuiMain gui;
	private Map2D<NXTPosition,NXTMove> map;
	private Connector connector;
	private MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading> mcl;
	
	public Core(GuiMain gui) {
		this.pause = 1000;
		this.gui = gui;
		this.connector = new Connector();
		this.map = new Map2D<NXTPosition,NXTMove>(new NXTPositionFactory(),connector.getMaxSensorRange());
		this.mcl = new MonteCarloLocalization<NXTPosition,Angle,NXTMove,RangeReading>(PARTICLE_COUNT, MIN_WEIGHT, MAX_DISTANCE, map, connector);
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
	
	public NXTPosition reselect() {
		mcl.reselectParticles();
		gui.displayParticles(mcl.getParticleIterator());
		return mcl.getPosition();
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
			result = reselect();
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
}
