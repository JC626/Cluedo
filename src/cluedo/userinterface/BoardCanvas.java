package cluedo.userinterface;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import cluedo.board.Board;

public class BoardCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	public static final int MIN_HEIGHT = 800; 
	
	public static final int CELL_WIDTH = BoardFrame.MIN_WIDTH / Board.WIDTH;
	public static final int CELL_HEIGHT = MIN_HEIGHT / Board.HEIGHT;

	private Image[][] cellImages;
	private Image boardImage;
		
	public BoardCanvas(Image[][] boardImages)
	{
		if (boardImages == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}
		this.cellImages = boardImages;
		createBoard();
	}
	/**
	 * Create the board image using the images provided from the constructor
	 */
	private void createBoard()
	{
		boardImage = new BufferedImage(BoardFrame.MIN_WIDTH, MIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics grph = boardImage.getGraphics();
		for (int x = 0; x < Board.WIDTH; x++)
		{
			for (int y = 0; y < Board.HEIGHT; y++)
			{
				Image image = cellImages[x][y];
				grph.drawImage(image, x*CELL_WIDTH, y*CELL_HEIGHT, null);
			}
		}
	}
	/**
	 * Draw boardImages as provided in the constructor onto Graphics g.
	 * If boardImages contains null images, they are not drawn.
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(boardImage, 0, 0, this);
	}
}
