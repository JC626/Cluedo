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
public class DiceCanvas extends JPanel {
	private static final long serialVersionUID = 1L;
	private int WIDTH = (int)(BoardFrame.WIDTH*0.3);
	private int HEIGHT =  BoardFrame.HEIGHT - BoardCanvas.HEIGHT;
	private int IMAGE_SIZE = 60;
	private JLabel dice1;
	private JLabel dice2;
	
	public DiceCanvas(Image img1, Image img2) {
		super();
		this.setLayout(new GridLayout(0, 2,10,0));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		Image d1 = img1.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		Image d2 = img2.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		dice1 = new JLabel(new ImageIcon(d1));
		dice2  = new JLabel(new ImageIcon(d2));
		this.add(dice1);
		this.add(dice2);
	}
	/**
	 * Changes the dice displayed when the player
	 * starts their turn
	 * @param img1
	 * @param img2
	 */
	public void changeDice(Image img1, Image img2)
	{
		Image d1 = img1.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		Image d2 = img2.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_DEFAULT);
		dice1.setIcon(new ImageIcon(d1));
		dice2.setIcon(new ImageIcon(d2));
	}
	
}
