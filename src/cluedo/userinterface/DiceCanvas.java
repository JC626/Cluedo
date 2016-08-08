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
		
	private int IMAGE_SIZE = 60;
	private JLabel leftDieLabel;
	private JLabel rightDieLabel;
	
	public DiceCanvas(Image leftDie, Image rightDie, int width, int height)  
	{
		leftDieLabel = new JLabel();
		rightDieLabel  = new JLabel();
		changeDice(leftDie,rightDie);
		this.add(leftDieLabel);
		this.add(rightDieLabel);
		
		this.setLayout(new GridLayout(0, 2, 10, 0));
		this.setMinimumSize(new Dimension(width, height));
	}
	/**
	 * Changes the dice displayed when the player
	 * starts their turn.
	 * @param leftDie
	 * @param rightDie
	 */
	public void changeDice(Image leftDie, Image rightDie) 
	{
		Image left = leftDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		Image right = rightDie.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		
		leftDieLabel.setIcon(new ImageIcon(left));
		rightDieLabel.setIcon(new ImageIcon(right));
	}
	
}
