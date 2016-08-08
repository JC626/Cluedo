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
public class TextDialog extends JDialog
{
	private JPanel panel;
	private Optional<String> userInput;

	public TextDialog(Frame owner, String title)
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
		this.setMinimumSize(new Dimension(300,150));
	}
	
	/**
	 * Get the user's input with the specified question.
	 * @param question The question to be displayed to the user.
	 * @return Optional.of(answer) if the user gives an answer to the question,
	 * Optional.empty() if they cancel.
	 */
	public Optional<String> getUserInput(String question)
	{
		panel.setLayout(new GridLayout(0, 2, 10,10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));//20, 0, 60, 75));
		
		JLabel questionLabel = new JLabel(question);
		
		JTextField input = new JTextField();

		JButton okButton = new JButton("Ok");
		okButton.addActionListener((a) -> {
			userInput = Optional.of(input.getText());
			cleanupDialog();
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((a) -> {
			cancelAction();
		});
		
		panel.add(questionLabel);
		panel.add(input);
		panel.add(okButton);
		panel.add(cancelButton);
		
		pack();	
		this.setVisible(true);
		
		return userInput;
	}
	
	/**
	 * Set our return value for canceling, and cleanup.
	 */
	private void cancelAction()
	{
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
