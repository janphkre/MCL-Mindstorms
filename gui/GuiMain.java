package gui;

import java.util.Iterator;

import aima.core.robotics.impl.Particle;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import localization.Core;
import localization.NXTMove;
import localization.NXTPosition;

public class GuiMain {
	
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
		// TODO: : enable grayed-out button(s) again
	}
}
