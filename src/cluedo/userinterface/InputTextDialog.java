package cluedo.userinterface;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A generic dialog box for text input.
 * Takes focus from the application, so users must enter something, or cancel.
 */
public class InputTextDialog extends JDialog
{
	private JPanel panel;
	private Optional<String> userInput;
	
	private static final int LAYOUT_ROWS = 0;
	private static final int LAYOUT_COLS = 2;
	private static final int LAYOUT_HORIZONTAL_GAP = 30;
	private static final int LAYOUT_VERTICAL_GAP = 30;

	private static final int BORDER_TOP = 30;
	private static final int BORDER_LEFT = 30;
	private static final int BORDER_BOTTOM = 30;
	private static final int BORDER_RIGHT = 30;
	
	private static final String AFFIRMATIVE_BUTTON_LABEL = "Ok";
	private static final String NEGATIVE_BUTTON_LABEL = "Cancel";
	
	private static final int WINDOW_MINIMUM_WIDTH = 600;
	private static final int WINDOW_MINIMUM_HEIGHT = 250;
	
	private static final int BUTTON_FONT_SIZE = 26;

	public InputTextDialog(Frame owner, String title)
	{
		super(owner, title, true);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelAction();
			}
		});
	
		panel = new JPanel();
		
		this.getContentPane().add(panel);
		this.setMinimumSize(new Dimension(WINDOW_MINIMUM_WIDTH, WINDOW_MINIMUM_HEIGHT));
	}
	
	/**
	 * Get the user's input with the specified question.
	 * @param question The question to be displayed to the user.
	 * @return Optional.of(answer) if the user gives an answer to the question,
	 * Optional.empty() if they cancel.
	 */
	public Optional<String> getUserInput(String question)
	{
		panel.setLayout(new GridLayout(LAYOUT_ROWS, LAYOUT_COLS, LAYOUT_HORIZONTAL_GAP, LAYOUT_VERTICAL_GAP));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));
		
		JLabel questionLabel = new JLabel(question);
		JTextField input = new JTextField();

		JButton affirmativeButton = new JButton(AFFIRMATIVE_BUTTON_LABEL);
		affirmativeButton.addActionListener((a) -> {
			userInput = Optional.of(input.getText());
			cleanupDialog();
		});
		
		JButton negativeButton = new JButton(NEGATIVE_BUTTON_LABEL);
		negativeButton.addActionListener((a) -> {
			cancelAction();
		});
		
		GraphicalUserInterface.setFontSize(questionLabel, BUTTON_FONT_SIZE);
		GraphicalUserInterface.setFontSize(input, BUTTON_FONT_SIZE);
		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);
		GraphicalUserInterface.setFontSize(negativeButton, BUTTON_FONT_SIZE);

		panel.add(questionLabel);
		panel.add(input);
		panel.add(affirmativeButton);
		panel.add(negativeButton);
		
		pack();	
		this.setVisible(true);
		
		return userInput;
	}
	
	/**
	 * Set our return value for canceling, and cleanup.
	 */
	private void cancelAction()
	{
		// If the user cancels, then we shouldn't return what they selected,
		// but a placeholder value instead.
		userInput = Optional.empty();
		cleanupDialog();
	}
	
	/**
	 * Dispose of the window, and remove it from view.
	 */
	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}
}
