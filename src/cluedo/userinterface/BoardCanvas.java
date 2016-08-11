package cluedo.userinterface;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import cluedo.board.Board;
import cluedo.model.Cell;

public class BoardCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	public static final int MIN_HEIGHT = 800; 
	public static final int CELL_WIDTH = BoardFrame.MIN_WIDTH / Board.WIDTH;
	public static final int CELL_HEIGHT = MIN_HEIGHT / Board.HEIGHT;

	private static final int PIECE_SHIFT = 10;
	private Image[][] cellImages;
	private Image boardImage;
	/**
	 * Stores the original image to their scaled image.
	 * Scaling an image creates a new image therefore cannot
	 * check that a scaled image is the same as the original
	 */
	private Map<Image,Image> scaledImages = new HashMap<Image,Image>(); 
	private Map<Image, Cell> scaledPieces = new HashMap<Image,Cell>();
	
	public BoardCanvas(Image[][] boardImages, Map<Image,Cell> pieceImages)
	{
		if (boardImages == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}
		this.cellImages = boardImages;
		scalePieces(pieceImages);
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
		drawPieces(g);
	}
	
	/**
	 * Scale the images of the pieces on the board
	 * so that it fits a cell
	 * @param pieces 
	 */
	private void scalePieces(Map<Image,Cell> pieces)
	{
		for(Map.Entry<Image, Cell> piece: pieces.entrySet())
		{
			Image image = piece.getKey();
			Image scaled = image.getScaledInstance(CELL_WIDTH-PIECE_SHIFT, CELL_HEIGHT-PIECE_SHIFT, Image.SCALE_DEFAULT);
			scaledImages.put(image, scaled);
			scaledPieces.put(scaled, piece.getValue());
		}
	}
	
	/**
	 * Draw the pieces on the cells on the board
	 * @param g
	 */
	private void drawPieces(Graphics g)
	{
		for(Map.Entry<Image, Cell> piece: scaledPieces.entrySet())
		{
			Cell location = piece.getValue();
			int x = location.getX()*CELL_WIDTH + PIECE_SHIFT/2;
			int y = location.getY()*CELL_HEIGHT + PIECE_SHIFT/2;
			g.drawImage(piece.getKey(), x, y,this);
		}
	}
	public void changePieceLocation(Image piece, Cell location)
	{
		if(!scaledImages.containsKey(piece))
		{
			throw new IllegalArgumentException("The piece image does not exist on the board");
		}
		Image scaled = scaledImages.get(piece);
		scaledPieces.put(scaled, location);
		repaint();
	}
}
