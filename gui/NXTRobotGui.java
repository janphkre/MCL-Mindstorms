package gui;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import aima.gui.applications.robotics.components.IRobotGui;
import aima.gui.applications.robotics.components.Settings;
import bot.Connector;

/**
 * Manages a {@link Connector} by implementing {@link IRobotGui}.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public class NXTRobotGui implements IRobotGui {

	private static final String ROBOT_NAME_KEY = "ROBOT_NAME";
	private static final String ROBOT_PROGRAM_KEY = "ROBOT_PROGRAM";
	private static final String ROBOT_FRAME_TITLE = "NXT Connector";
	private static final Object[] BUTTONS = {"Connect", "Abort"};
	
	private String robotName;
	private String program;
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
		robotNameField.setText(robotName);
		programField.setText(program);
		final int result = JOptionPane.showOptionDialog(null, data, ROBOT_FRAME_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, BUTTONS, BUTTONS[0]);
        if(result == 0) {
        	robotName = robotNameField.getText();
        	program = programField.getText();
        	connector.connect(robotName, program);
        	return connector.isConnected();
        }
		return false;
	}
	
	@Override
	public void destructRobot() {
		connector.close();
	}

	@Override
	public String getButtonString() {
		if(connector.isConnected()) return "Reconnect Robot";
		return DEFAULT_BUTTON_STRING;
	}

	@Override
	public void loadSettings(Settings settingsGui) {
		robotName = settingsGui.getSetting(ROBOT_NAME_KEY);
		program = settingsGui.getSetting(ROBOT_PROGRAM_KEY);
	}

	@Override
	public void saveSettings(Settings settingsGui) {
		settingsGui.setSetting(ROBOT_NAME_KEY, robotName);
		settingsGui.setSetting(ROBOT_PROGRAM_KEY, program);
	}
}
