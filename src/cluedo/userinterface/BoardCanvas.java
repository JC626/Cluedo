package cluedo.userinterface;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * @param boardImages The images to be displayed on the Board. Does not include movable pieces.
	 * May not be null. May contain null.
	 * @param pieceImages Movable pieces to be displayed on the Board.
	 */
	public BoardCanvas(Image[][] boardImages, Map<Image, Cell> pieceImages)
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
	 * Draws the board using boardImages
	 * and the state of the board (the location of pieces)
	 * as provided in the constructor onto Graphics g.
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
	 * so that it fits within a cell
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
	 * @param g Board Graphics
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
	
	/**
	 * Change the location of a player's piece.
	 * Note that this does not affect the game state.
	 * @param piece - The original image of the piece
	 * @param newPos - The new position of the piece
	 */
	public void changePieceLocation(Image piece, Cell newPos)
	{
		if(!scaledImages.containsKey(piece))
		{
			throw new IllegalArgumentException("The piece image does not exist on the board");
		}
		Image scaled = scaledImages.get(piece);
		scaledPieces.put(scaled, newPos);
		repaint();
	}
	
	/**
	 * Animates the movement of a player
	 * when they move by one cell.
	 * Does not animate the player moving into/out of a room
	 * or if a piece is being transferred
	 * @param currentPiece - The player being moved
	 * @param newPos - The new cell position the player moved to
	 */
	public void animatePlayerMove(Image currentPiece, Cell newPos)
	{
		if(!scaledImages.containsKey(currentPiece))
		{
			throw new IllegalArgumentException("The piece image does not exist on the board");
		}
		Image scaled = scaledImages.get(currentPiece);
		Cell oldPos = scaledPieces.get(scaled);
		int xDiff = newPos.getX() - oldPos.getX();
		int yDiff = newPos.getY() - oldPos.getY();
		/*
		 * Piece must only move in one direction to animate
		 * i.e. a change in x by one or a change in y by one
		 */
		if(!(Math.abs(xDiff) == 1 && yDiff == 0) && !(xDiff == 0 && Math.abs(yDiff) == 1))
		{
			//Player moved into a room
			changePieceLocation(currentPiece, newPos);
			return;
		}
		Graphics g = getGraphics();
		int limit = Math.abs(xDiff) == 1 ? CELL_WIDTH : CELL_HEIGHT;
		int i = 0;
		//Change position by some i amount to make animation
		while(i < limit)
		{
			//Use double buffering to remove flickering
			Image doubleBuffer = createImage((int)getSize().getWidth(), (int)getSize().getHeight());
			Graphics offscreen = doubleBuffer.getGraphics();
			offscreen.drawImage(boardImage, 0, 0, this);
			
			for(Map.Entry<Image, Cell> piece: scaledPieces.entrySet())
			{
				Cell location = piece.getValue();
				int x = location.getX()*CELL_WIDTH + PIECE_SHIFT/2;
				int y = location.getY()*CELL_HEIGHT + PIECE_SHIFT/2;
				if(piece.getKey() == scaled)
				{
					if(xDiff == 1)
					{
						x+= i;
					}
					else if(xDiff == -1)
					{
						x-= i;
					}
					else if(yDiff == 1)
					{
						y+= i;					
					}
					else
					{
						y-= i;				
					} 
				}	
				offscreen.drawImage(piece.getKey(), x, y,this);
			}

			try {
				Thread.sleep(20); //Delay 20ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i+= limit/10;
			g.drawImage(doubleBuffer, 0, 0, this);
		}
		changePieceLocation(currentPiece, newPos);
	}
	
	/**
	 * Draws the cells that a player can take 
	 * to exit a room
	 * @param exitCells - The cells that can be taken as exits
	 * @param exitImage - The image of an exit cell
	 */
	public void drawExitCells(List<Cell> exitCells,Image exitImage)
	{
		Graphics g = getGraphics();
		for(Cell exit : exitCells)
		{
			g.drawImage(exitImage, exit.getX()*CELL_WIDTH, exit.getY()*CELL_HEIGHT, this);
		}
	}
}
