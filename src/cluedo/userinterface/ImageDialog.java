package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A generic dialog for displaying images.
 */
public class ImageDialog extends JDialog
{
	private JPanel panel;
	private JPanel buttonPanel;
	
	private final String AFFIRMATIVE_BUTTON_LABEL = "Ok";

	private static final int PANEL_BORDER_TOP = 20;
	private static final int PANEL_BORDER_LEFT = 20;
	private static final int PANEL_BORDER_BOTTOM = 0;
	private static final int PANEL_BORDER_RIGHT = 20;
	
	private static final int BUTTON_BORDER_TOP = 0;
	private static final int BUTTON_BORDER_LEFT = 0;
	private static final int BUTTON_BORDER_BOTTOM = 10;
	private static final int BUTTON_BORDER_RIGHT = 0;
	
	// These should be calculated from the maximum image size,
	// but all of our images are the same size.
	private final int MINIMUM_FRAME_WIDTH = 300;
	private final int MINIMUM_FRAME_HEIGHT = 400;
	
	private static final int BUTTON_FONT_SIZE = 26;

	public ImageDialog(Frame owner, String title)
	{
		super(owner, title, true);

		panel = new JPanel();
		buttonPanel = new JPanel();
		
		this.add(panel);
		this.add(buttonPanel);
		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.setMinimumSize(new Dimension(MINIMUM_FRAME_WIDTH, MINIMUM_FRAME_HEIGHT));
	}
	
	/**
	 * Create the window, and display it to the user.
	 * @param imagePath The path of the image to be displayed.
	 * Catches IO exception and displays text in place of the image
	 * informing the user of the failing read.
	 */
	public void display(ImageIcon image)
	{
		panel.setBorder(BorderFactory.createEmptyBorder(PANEL_BORDER_TOP, PANEL_BORDER_LEFT, PANEL_BORDER_BOTTOM, PANEL_BORDER_RIGHT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_TOP, BUTTON_BORDER_LEFT, BUTTON_BORDER_BOTTOM, BUTTON_BORDER_RIGHT));
		
		JLabel imageLabel = new JLabel(image);
		panel.add(imageLabel);
		
		JButton affirmativeButton = new JButton(AFFIRMATIVE_BUTTON_LABEL);
		affirmativeButton.addActionListener((a) -> {
			cleanupDialog();
		});
		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);
		
		buttonPanel.add(affirmativeButton);
		
		
		pack();	
		this.setVisible(true);
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