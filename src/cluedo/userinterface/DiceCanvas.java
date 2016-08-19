package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel to display the dice roll
 * for the player to know how many
 * moves they have rolled
 */
public class DiceCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private static final int ROWS = 0;
	private static final int COLS = 2;
	private static final int HORIZONTAL_GAP = 10;
	private static final int VERTICAL_GAP = 0;
		
	private int IMAGE_SIZE = 60;
	private JLabel leftDieLabel;
	private JLabel rightDieLabel;
	
	/**
	 * Area in which we display the dice rolls.
	 * @param width The minimum size of the canvas.
	 * Should be at least the size of the largest dice image. 
	 * @param height The minimum size of the canvas.
	 * Should be at least the size of the largest dice image.
	 */
	public DiceCanvas(int width, int height)  
	{
		leftDieLabel = new JLabel();
		rightDieLabel  = new JLabel();
		this.add(leftDieLabel);
		this.add(rightDieLabel);
		
		this.setLayout(new GridLayout(ROWS, COLS, HORIZONTAL_GAP, VERTICAL_GAP));
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
