package gui;

import java.util.Iterator;

import aima.core.robotics.impl.Particle;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import localization.NXTMove;
import localization.NXTPosition;

public class GuiMain implements Runnable {
	
	public void displayRangeReadings(RangeReading[] rangeReadings) {
		// TODO
	}
	
	public void displayMove(NXTMove move) {
		// TODO
	}
	
	public void displayParticles(Iterator<Particle<NXTPosition,Angle,NXTMove>> iterator) {
		// TODO
	}

	public void displayResult(NXTPosition result) {
		// TODO
		
	}
	
	public void algorithmFinished() {
		// TODO: enable grayed-out button(s) again
	}

	@Override
	public void run() {
		Thread.currentThread().setDaemon(true);
		while(true) {
			try {
				this.wait();
			} catch (InterruptedException e) {}
			update();
		}
	}
	
	private void update() {
		//TODO
	}
}
