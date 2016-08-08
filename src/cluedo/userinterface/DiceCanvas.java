package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel to display dice roll
 * for the player to know how many
 * moves they have
 */
public class DiceCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private int WIDTH = (int)(BoardFrame.WIDTH * 0.3); // FIXME why 0.3?
	private int HEIGHT =  BoardFrame.HEIGHT - BoardCanvas.HEIGHT; // TODO is this value not stored elsewhere? Seems like it should be in the bottom panel frame
	
	private int IMAGE_SIZE = 60;
	
	private JLabel dice1;
	private JLabel dice2;
	
	public DiceCanvas(Image leftDie, Image rightDie) // FIXME Is my assumption on left and right correct? Was img1 img2 
	{
		Image d1 = leftDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		Image d2 = rightDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		
		dice1 = new JLabel(new ImageIcon(d1));
		dice2  = new JLabel(new ImageIcon(d2));
		
		this.add(dice1);
		this.add(dice2);
		
		this.setLayout(new GridLayout(0, 2, 10, 0));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
	}
	/**
	 * Changes the dice displayed when the player
	 * starts their turn.
	 * @param leftDie
	 * @param rightDie
	 */
	public void changeDice(Image leftDie, Image rightDie) // TODO same as constructor FIX ME
	{
		Image d1 = leftDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		Image d2 = rightDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		
		dice1.setIcon(new ImageIcon(d1));
		dice2.setIcon(new ImageIcon(d2));
	}
	
}
