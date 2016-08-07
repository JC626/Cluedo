package cluedo.userinterface;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class ConfirmationDialog extends JDialog
{
	/**
	 * Wrapper for dialog boxes with some useful defaults.
	 * Presents Yes and No as options.
	 * @param title The title of the window.
	 * @param question The question to ask the user.
	 * @return True if the user selected "Yes", false otherwise.
	 */
	public static boolean yesNo(String title, String question)
	{
		return dialogBox(title, question, JOptionPane.YES_NO_OPTION);
	}
	
	/**
	 * Wrapper for dialog boxes with some useful defaults.
	 * Presents Ok and Cancel as options.
	 * @param title The title of the window.
	 * @param question The question to ask the user.
	 * @return True if the user selected "Ok", false otherwise.
	 */
	public static boolean okCancel(String title, String question)
	{
		return dialogBox(title, question, JOptionPane.OK_CANCEL_OPTION);
	}
	
	/**
	 * Wrapper for JOptionPane.showConfirmDialog method.
	 * @param title The title of the window.
	 * @param question The question to ask the user.
	 * @param option The buttons to display. Use JOptionPane.??? for the values.
	 * @return True if the user selected the affirmative option, false otherwise.
	 */
	private static boolean dialogBox(String title, String question, int option)
	{
		return JOptionPane.showConfirmDialog(null, new JLabel(question), title,
				option, JOptionPane.INFORMATION_MESSAGE) == 0;
	}
}
