package cluedo.board;

import java.util.HashMap;
import java.util.Map;
import cluedo.model.*;
import cluedo.utility.Heading.Direction;

public class Board {
	private final int WIDTH = 24;
	private final int HEIGHT = 25;

	private Map<Cell, Piece> cellHasPiece;
	private Map<Piece, Cell> pieceOnCell;
	private Cell[][] cells = new Cell[WIDTH][HEIGHT];

	// TODO Board constructor
	public Board() { // Need Cells and Pieces
						// Put all cells into HashMap?
		cellHasPiece = new HashMap<Cell, Piece>();
		// Put all pieces into the map with null cell?
		pieceOnCell = new HashMap<Piece, Cell>();
	}
	
	//TODO finish board class
	/**
	 * Move the player at a specified direction
	 * 
	 * @param piece
	 * @param dir - The direction the piece is moving to
	 * @return Cell - The new cell position of the piece
	 */
	public Cell move(Piece piece, Direction dir)
	{
		if(!pieceOnCell.containsKey(piece)){
			/*throw new IllegalMethodCallException();*/
		}
		Cell onPiece = pieceOnCell.get(piece);
		return null; //TODO return new cell position
	}
	
	/**
	 * Get the cell position of a particular piece
	 * @param piece
	 * @return Cell - The cell the piece is on
	 */
	public Cell getPosition(Piece piece)
	{
		if(!pieceOnCell.containsKey(piece)){
			/*throw new IllegalMethodCallException();*/
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
	/*private Cell getCell(Direction dir)
	{
		
		return null;
	}*/
}
