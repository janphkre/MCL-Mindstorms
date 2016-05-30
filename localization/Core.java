package localization;

import java.util.LinkedList;

import bot.Connector;
import gui.GuiMain;
import robotics.MonteCarloLocalization;
import robotics.concrete.Map2D;

public class Core implements Runnable  {
	
	private static final int PARTICLE_COUNT = 200;
	private static final double WEIGHT_MIN = 0.001d;
	private static final double WEIGHT_MAX = 0.998d;
	private static final double[] RANGE_ANGLES = {-90d,-45d,0d,45d,90d};
	
	private int pause; //milliseconds
	
	private GuiMain gui;
	private Map2D<NXTPosition> map;
	private Connector connector;
	private MonteCarloLocalization<NXTPosition> mcl;
	
	public Core(GuiMain gui) {
		this.pause = 1000;
		this.gui = gui;
		this.map = new Map2D<NXTPosition>(new NXTPositionFactory());
		this.connector = new Connector(RANGE_ANGLES);
		this.mcl = new MonteCarloLocalization<NXTPosition>(PARTICLE_COUNT, WEIGHT_MIN, WEIGHT_MAX, map, connector, RANGE_ANGLES);
	}
	
	public void move() {
		NXTMove move = connector.performMove();
		gui.displayMove(move);
		mcl.applyMove(move);
		gui.displayParticles(mcl.getParticleIterator());
	}
	
	public void rangeReading() {
		double[] rangeReadings = connector.getRangeReadings();
		gui.displayRangeReadings(rangeReadings);
		mcl.weightParticles(rangeReadings);
		gui.displayParticles(mcl.getParticleIterator());
	}
	
	public LinkedList<NXTPosition> reselect() {
		mcl.reselectParticles();
		gui.displayParticles(mcl.getParticleIterator());
		LinkedList<NXTPosition> result = mcl.getPosition();
		if(!result.isEmpty()) return result;
		return null;
	}
	
	@Override
	public void run() {
		gui.displayParticles(mcl.getParticleIterator());
		pause();
		LinkedList<NXTPosition> result = null;
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
