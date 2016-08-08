package cluedo.userinterface;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * A generic frame for displaying images.
 * Does NOT take focus, so users can open other windows or dialogs.
 */
public class ImageFrame extends JFrame
{
	private JPanel panel;
	
	private final String affirmativeButtonLabel = "Ok";
	private final String missingImageText = "The image could not be loaded.";
	
	private final int minimumFrameWidth = 400;
	private final int minimumFrameHeight = 700; 

	public ImageFrame(Frame owner, String title)
	{
		super(title);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanupDialog();
			}
		});

		panel = new JPanel();

		this.getContentPane().add(panel);
		this.setMinimumSize(new Dimension(minimumFrameWidth, minimumFrameHeight));
	}
	
	/**
	 * Create the window, and display it to the user.
	 * @param imagePath The path of the image to be displayed.
	 * Catches IO exception and displays text in place of the image
	 * informing the user of the failing read.
	 */
	public void display(String imagePath)
	{
		panel.setLayout(new GridLayout(0, 1, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		try
		{
			BufferedImage image = ImageIO.read(new File(imagePath));
			JLabel imageLabel = new JLabel(new ImageIcon(image));
			panel.add(imageLabel);
		}
		catch (IOException e)
		{
			JLabel substituteText = new JLabel(missingImageText);
			panel.add(substituteText);
		}
		
		JButton affirmativeButton = new JButton(affirmativeButtonLabel);
		affirmativeButton.addActionListener((a) -> {
			cleanupDialog();
		});

		
		panel.add(affirmativeButton);
		
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