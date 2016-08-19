package de.thkoeln.gm.mcl_mindstorms.gui;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import aima.gui.swing.demo.robotics.components.IRobotGui;
import aima.gui.swing.demo.robotics.components.Settings;
import aima.gui.swing.framework.util.GuiBase;
import de.thkoeln.gm.mcl_mindstorms.robot.Connector;

/**
 * Manages a {@link Connector} by implementing {@link IRobotGui}.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class NXTRobotGui implements IRobotGui {
	
	private static final String ROBOT_NAME_KEY = "ROBOT_NAME";
	private static final String ROBOT_PROGRAM_KEY = "ROBOT_PROGRAM";
	private static final String ROBOT_FRAME_TITLE = "NXT Connector";
	private static final Object[] BUTTONS = {"Connect", "Abort"};
	
	private Connector connector;
	private JTextField robotNameField = new JTextField();
	private JTextField programField = new JTextField();
	private Object[] data = {"Robot name:", robotNameField,"Program:", programField};
	
	/**
	 * @param connector the NXT robot to be managed.
	 */
	public NXTRobotGui(Connector connector) {
		this.connector = connector;
	}
	
	@Override
	public boolean initializeRobot() {
		final int result = JOptionPane.showOptionDialog(null, data, ROBOT_FRAME_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, BUTTONS, BUTTONS[0]);
        if(result == 0) {
        	connector.connect(robotNameField.getText(), programField.getText());
        	return connector.isConnected();
        }
		return false;
	}
	
	@Override
	public void destructRobot() {
		connector.close();
	}

	@Override
	public void notifyInitialize() {
		GuiBase.showMessageBox("You may want to connect with the robot first.");
	}
	
	@Override
	public String getButtonString() {
		if(connector.isConnected()) return "Reconnect Robot";
		return DEFAULT_BUTTON_STRING;
	}

	@Override
	public void loadSettings(Settings settingsGui) {
		robotNameField.setText(settingsGui.getSetting(ROBOT_NAME_KEY));
		programField.setText(settingsGui.getSetting(ROBOT_PROGRAM_KEY,"MclDaemon.nxj"));
	}

	@Override
	public void saveSettings(Settings settingsGui) {
		settingsGui.setSetting(ROBOT_NAME_KEY, robotNameField.getText());
		settingsGui.setSetting(ROBOT_PROGRAM_KEY, programField.getText());
	}
}
