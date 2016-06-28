package gui;

import javax.swing.JOptionPane;

import aima.gui.applications.robotics.components.IRobotGui;
import bot.Connector;

public class NXTRobotGui implements IRobotGui {

	private Connector connector;
	
	public NXTRobotGui(Connector connector) {
		this.connector = connector;
		connector.registerGui(this);
	}
	
	@Override
	public boolean initializeRobot() {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public void destructRobot() {
		connector.close();
	}
	
	/**
	 * Displays a Message Box in a new Thread
	 * @param message to be shown
	 */
	public void showError(final String message){
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
               JOptionPane.showMessageDialog(null,message);
            }
        });
        thread.start();
	}
	
}
