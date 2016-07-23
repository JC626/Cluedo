package cluedo.board;

import java.util.HashMap;
import java.util.Map;
import cluedo.model.*;
import cluedo.utility.Heading.Direction;

/**
 * The Board for the Cluedo game
 *
 */
public class Board {
	private final int WIDTH = 24;
	private final int HEIGHT = 25;
	
	/**
	 * A map of only the cells that contains a Piece on it.
	 */
	private Map<Cell, Piece> cellHasPiece;
	/**
	 * A map of all Pieces that are on the board
	 * Every Piece must be on a Cell (Cell cannot be null)
	 */
	private Map<Piece, Cell> pieceOnCell;
	
	/**
	 * Representation of the Board
	 */
	private Cell[][] cells = new Cell[WIDTH][HEIGHT];

	// TODO Board constructor
	public Board() { // Need Cells and Pieces
						// Put all cells into HashMap?
		cellHasPiece = new HashMap<Cell, Piece>();
		// Put all pieces into the map with null cell?
		pieceOnCell = new HashMap<Piece, Cell>();
	}
	
	//TODO Exceptions in Board class (commented out)
	
	/**
	 * Move the player at a specified direction
	 * 
	 * @param piece
	 * @param direction - The direction the piece is moving to
	 * @return Cell - The new cell position of the piece
	 */
	public Cell move(Piece piece, Direction direction)
	{
		if(!pieceOnCell.containsKey(piece)){
			/*throw new IllegalMethodCallException();*/
		}
		Cell onPiece = pieceOnCell.get(piece);
		if(onPiece.hasWall(direction))
		{
			//throw new IllegalMethodCallException();
		}
		Cell newPos = getCell(onPiece,direction);
		this.setPosition(piece, newPos);
		return newPos;
	}
	
	/**
	 * Get the cell position of a particular piece
	 * @param piece
	 * @return Cell - The cell the piece is on
	 */
	public Cell getPosition(Piece piece)
	{
		if(!pieceOnCell.containsKey(piece)){
			//throw new IllegalMethodCallException();
		}
		return pieceOnCell.get(piece);
	}
	/**
	 * Sets the piece to the specified cell position
	 * 
	 * Used for setting:
	 * Starting position for pieces
     * Secret passage usage
     * Suggestions to move weapons and Pieces (if needed)
	 * player’s are handled by the game, as the rules change
     * Entering/exiting 
	 *
	 * @param piece
	 * @param cell - 
	 */
	public void setPosition(Piece piece, Cell cell)
	{
		cellHasPiece.put(cell,piece);
		pieceOnCell.put(piece,cell);
	}
	/**
	 * Get the neighbouring cell in the specified direction
	 * from a given cell.
	 * @param cell - the cell we are moving from
	 * @param direction - the direction the neighbouring cell is
	 * @return the new cell position
	 */
	private Cell getCell(Cell cell, Direction direction)
	{
		int x = cell.getX();
		int y = cell.getY();
		switch(direction)
		{
		case North:
			y--;
			break;
		case South:
			y++;
			break;
		case East:
			x++;
			break;
		case West:
			x--;
			break;
		default:
			throw new IllegalArgumentException("Direction not recognised - internal error");
		}
		if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
		{
			throw new IllegalArgumentException("Movement outside the board boundaries - internal error");
		}
		return cells[x][y];
	}
}
