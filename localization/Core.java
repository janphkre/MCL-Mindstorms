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
	
	private int pause;//ms
	
	private GuiMain gui;
	private Map2D<Position2D> map;
	private Connector connector;
	private MonteCarloLocalization<Move2D,Position2D> mcl;
	
	public Core(GuiMain gui) {
		this.pause = 1000;
		this.gui = gui;
		this.map = new Map2D<Position2D>();
		this.connector = new Connector(RANGE_ANGLES);
		this.mcl = new MonteCarloLocalization<Move2D,Position2D>(PARTICLE_COUNT, WEIGHT_MIN, WEIGHT_MAX, map, connector, RANGE_ANGLES);
	}
	
	public void move() {
		Move2D move = connector.performMove();
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
	
	public LinkedList<Position2D> reselect() {
		mcl.reselectParticles();
		gui.displayParticles(mcl.getParticleIterator());
		LinkedList<Position2D> result = mcl.getPosition();
		if(!result.isEmpty()) return result;
		return null;
	}
	
	@Override
	public void run() {
		gui.displayParticles(mcl.getParticleIterator());
		pause();
		LinkedList<Position2D> result = null;
		rangeReading();
		while(result == null) {
			//1. Move the Robot:
			move();
			//2. WeightParticles through the Ranges:
			rangeReading();
			pause();
			//3. Reselection:
			result = reselect();
		}
		gui.displayResult(result);
		gui.algorithmFinished();
	}
	
	private void pause() {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
