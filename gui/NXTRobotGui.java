package gui;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import aima.gui.applications.robotics.components.IRobotGui;
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

	private Connector connector;
	private JTextField robot_Name;
	private JTextField program;
	private String robotDataFrameTitle = "Robot";
	
	/**
	 * @param connector the NXT robot to be managed.
	 */
	public NXTRobotGui(Connector connector) {
		this.connector = connector;
		connector.registerGui(this);
	}
	
	/**
	 * Tries to activate the system default look and feel.
	 */
	private void activateSystemStyle() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
	}
	
	/**
	 * Displays a message box without waiting for the message box to close.
	 * @param message the message to be shown.
	 */
	public void showError(final String message) {
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
               JOptionPane.showMessageDialog(null,message);
            }
        });
        thread.start();
	}
	
	@Override
	public boolean initializeRobot() {
		activateSystemStyle();
		
		UIManager.put("OptionPane.cancelButtonText", "Abort");
		UIManager.put("OptionPane.okButtonText", "Connect");
		  
		robot_Name = new JTextField();
		program = new JTextField();
		Object[] robotData = {"Robot Name:", robot_Name,"Program", program};
	      
		JOptionPane pane = new JOptionPane( robotData, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	    pane.createDialog(null, robotDataFrameTitle).setVisible(true);
	   
	    if(pane.getValue() instanceof Integer) {
	            if(((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION) {
	            	connector.connect(robot_Name.getText(), program.getText());
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
	
}
