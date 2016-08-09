package cluedo.userinterface;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import cluedo.board.Board;

public class BoardCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	public static final int HEIGHT = 800; 
	
	public static final int CELL_WIDTH = BoardFrame.MIN_WIDTH / Board.WIDTH;
	public static final int CELL_HEIGHT = HEIGHT / Board.HEIGHT;

	private Image[][] boardImages;
		
	public BoardCanvas(Image[][] boardImages)
	{
		if (boardImages == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}
		this.boardImages = boardImages;
	}

	/**
	 * Draw boardImages as provided in the constructor onto Graphics g.
	 * If boardImages contains null images, they are not drawn.
	 */
	protected void paintComponent(Graphics g)
	{
		for (int x = 0; x < Board.WIDTH; x++)
		{
			for (int y = 0; y < Board.HEIGHT; y++)
			{
					Image image = boardImages[x][y];
					g.drawImage(image, x*CELL_WIDTH, y*CELL_HEIGHT, this);
			}

		}
	}
}
