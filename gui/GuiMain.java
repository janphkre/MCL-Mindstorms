package gui;

import java.util.Iterator;
import java.util.LinkedList;

import localization.Core;
import localization.MoveNXT;
import localization.PositionNXT;
import robotics.Particle;

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
	
	public void displayRangeReadings(double[] rangeReadings) {
		// TODO
	}
	
	public void displayMove(MoveNXT move) {
		// TODO
	}
	
	public void displayParticles(Iterator<Particle> iterator) {
		// TODO
	}

	public void displayResult(LinkedList<PositionNXT> result) {
		// TODO
		
	}
	
	public void algorithmFinished() {
		// TODO: : enable grayed-out button(s) again
	}
}
