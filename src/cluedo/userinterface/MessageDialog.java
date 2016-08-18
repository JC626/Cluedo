package cluedo.userinterface;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MessageDialog extends JDialog
{

	/**
	 * Presents the dialog as an Information message
	 */
	public static void information(String title, String text)
	{
		dialogBox(title, text, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Presents the dialog as an Error message
	 */
	public static void error(String title, String text)
	{
		dialogBox(title, text, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Wrapper for JOptionPane.showMessageDialog method.
	 * @param title The title of the window.
	 * @param text The information to show the user
	 */
	private static void dialogBox(String title, String text, int messageType)
	{
		JOptionPane.showMessageDialog(null, new JLabel(text), title, messageType);
	}
	
}
