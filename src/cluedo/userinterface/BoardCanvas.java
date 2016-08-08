package cluedo.userinterface;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;
import cluedo.board.Board;

public class BoardCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	static final int HEIGHT = 800; // FIXME, This is the height of the Board. Height and width different for different panels why is this here? Should it be with BoardFrame.WIDTH, or should BoardFrame.WIDTH be here?
	
	public static final int cellWidth = BoardFrame.WIDTH / Board.WIDTH;
	public static final int cellHeight = HEIGHT / Board.HEIGHT;

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
				g.drawImage(image, x * cellWidth, y * cellHeight, this);
			}

		}
	}
}
