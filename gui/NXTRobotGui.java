package gui;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import aima.gui.applications.robotics.components.IRobotGui;
import aima.gui.applications.robotics.components.Settings;
import aima.gui.applications.robotics.util.GuiBase;
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
	
	private String robotName;
	private String program;
	private Connector connector;
	private JTextField robotNameField;
	private JTextField programField;
	
	/**
	 * @param connector the NXT robot to be managed.
	 */
	public NXTRobotGui(Connector connector) {
		this.connector = connector;
	}
	
	@Override
	public boolean initializeRobot() {
		GuiBase.activateSystemStyle();
		
		UIManager.put("OptionPane.cancelButtonText", "Abort");
		UIManager.put("OptionPane.okButtonText", "Connect");
		  
		robotNameField = new JTextField(robotName);
		programField = new JTextField(program);
		Object[] robotData = {"Robot name:", robotNameField,"Program:", programField};
	      
		JOptionPane pane = new JOptionPane( robotData, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	    pane.createDialog(null, ROBOT_FRAME_TITLE).setVisible(true);
	   
	    if(pane.getValue() instanceof Integer) {
            if(((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION) {
            	robotName = robotNameField.getText();
            	program = programField.getText();
            	connector.connect(robotName, program);
            	return connector.isConnected();
            }
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
