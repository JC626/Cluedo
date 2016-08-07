package cluedo.userinterface;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageFrame extends JFrame
{
	private JPanel panel;

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
		this.setMinimumSize(new Dimension(400,700));
	}
	
	public void display(String imagePath) throws IOException
	{
		panel.setLayout(new GridLayout(0, 1, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		BufferedImage image = ImageIO.read(new File(imagePath));
		JLabel imageLabel = new JLabel(new ImageIcon(image));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener((a) -> {
			cleanupDialog();
		});

		panel.add(imageLabel);
		panel.add(okButton);
		
		pack();	
		this.setVisible(true);
	}

	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}
}