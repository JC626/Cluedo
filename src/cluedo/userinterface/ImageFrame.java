package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A generic frame for displaying images.
 * Does NOT take focus, so users can open other windows or dialogs.
 */
public class ImageFrame extends JDialog
{
	private JPanel panel;
	private JPanel buttonPanel;
	
	private final String AFFIRMATIVE_BUTTON_LABEL = "Ok";
	
	private final int MINIMUM_FRAME_WIDTH = 300;
	private final int MINIMUM_FRAME_HEIGHT = 400;
	
	private static final int BUTTON_FONT_SIZE = 26;

	public ImageFrame(Frame owner, String title)
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
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
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