package cluedo.userinterface;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Creates a dialog that contains a number of provided buttons.
 * The actions for the buttons are also provided.
 * Takes focus of the application.
 */
public class ButtonDialog extends JDialog
{
	private JPanel panel;
	private JLabel title;
	private static final int BUTTON_FONT_SIZE = 25;
	private static final int SPACE_BETWEEN_BUTTONS = 10;
	
	private static final int BORDER_TOP = 20;
	private static final int BORDER_LEFT = 20;
	private static final int BORDER_BOTTOM = 0;
	private static final int BORDER_RIGHT = 0;
	
	private final String AFFIRMATIVE_BUTTON_LABEL = "Ok";

	/**
	 * A dialog box that contains buttons, as defined by display.
	 * @param owner The owner of this window.
	 * @param windowName The title of the window.
	 */
	public ButtonDialog(Frame owner, String windowName)
	{
		super(owner, windowName, true); // Dialog is modal (retains focus)

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanupDialog();
			}
		});
		this.title = new JLabel(windowName);
		
		panel = new JPanel();
		panel.add(title);
		this.getContentPane().add(panel);
	}

	/**
	 * Create the window and display the buttons.
	 * @param buttons The buttons that are to be displayed.
	 * @param buttonNames The names of each button.
	 */
	public void display(List<String> buttonNames, List<? extends AbstractButton> buttons, List<ActionListener> buttonActions)
	{
		// These values work well with most buttons. The vertical height is 50 per button plus a bit for the OK button at the bottom.
		this.setMinimumSize(new Dimension(250, (50 * buttons.size()) + 150));
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical line format
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));

		// Add our buttons and their associated listeners.
		for (int i = 0; i < buttons.size(); i++)
		{
			AbstractButton thisButton = buttons.get(i);
			String thisButtonName = buttonNames.get(i);
			ActionListener thisButtonAction = buttonActions.get(i);
			
			thisButton.setText(thisButtonName);
			thisButton.addActionListener(thisButtonAction);
			
			GraphicalUserInterface.setFontSize(thisButton, BUTTON_FONT_SIZE);
			thisButton.setAlignmentX(Component.CENTER_ALIGNMENT);

			panel.add(thisButton);
			addVerticalSpace(SPACE_BETWEEN_BUTTONS);
		}
		
		addVerticalSpace(SPACE_BETWEEN_BUTTONS); // Extra space between ok button and our provided buttons

		JButton affirmativeButton = new JButton(AFFIRMATIVE_BUTTON_LABEL);
		affirmativeButton.addActionListener((a) -> {
			cleanupDialog();
		});
		
		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);
		affirmativeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(affirmativeButton);
		setResizable(false);
		pack();
		this.setVisible(true);
	}
	
	/**
	 * Placeholder space between this element and the next.
	 * @param amount Amount in pixels to give. May be negative.
	 */
	private void addVerticalSpace(int amount)
	{
		panel.add(Box.createRigidArea(new Dimension(0, amount)));
	}

	/**
	 * Destroy the window, and set visible to false.
	 */
	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}
}
