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
import javax.swing.JPanel;

/**
 * Creates a dialog that contains a number of provided buttons.
 * The actions for the buttons are also provided.
 * Takes focus of the application.
 */
public class ButtonDialog extends JDialog
{
	private JPanel panel;
	private static final int buttonFontSize = 25;
	private static final int spaceBetweenButtons = 10;

	/**
	 * A dialog box that contains buttons, as defined by display.
	 * @param owner The owner of this window.
	 * @param title The title of the window.
	 */
	public ButtonDialog(Frame owner, String title)
	{
		super(owner, title, true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanupDialog();
			}
		});

		panel = new JPanel();
		
		this.getContentPane().add(panel);
	}

	/**
	 * Create the window and display the buttons.
	 * @param buttons The buttons that are to be displayed.
	 * @param buttonNames The names of each button.
	 */
	public void display(List<String> buttonNames, List<? extends AbstractButton> buttons, List<ActionListener> buttonActions)
	{
		this.setMinimumSize(new Dimension(250, (50 * buttons.size()) + 150)); // These values appear to work well.
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical line format
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 0));

		for (int i = 0; i < buttons.size(); i++)
		{
			AbstractButton thisButton = buttons.get(i);
			String thisButtonName = buttonNames.get(i);
			ActionListener thisButtonAction = buttonActions.get(i);
			
			thisButton.setText(thisButtonName);
			thisButton.addActionListener(thisButtonAction);
			
			GraphicalUserInterface.setFontSize(thisButton, buttonFontSize);
			thisButton.setAlignmentX(Component.CENTER_ALIGNMENT);

			panel.add(thisButton);
			addVerticalSpace(spaceBetweenButtons);
		}
		
		addVerticalSpace(spaceBetweenButtons); // Extra space between ok button and our provided buttons

		JButton myButton = new JButton("Ok");
		myButton.addActionListener((a) -> {
			cleanupDialog();
		});
		
		GraphicalUserInterface.setFontSize(myButton, buttonFontSize);
		myButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(myButton);
		pack();
		this.setVisible(true);
	}
	
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
