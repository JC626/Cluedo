package cluedo.board;

import java.util.HashMap;
import java.util.Map;

import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.model.*;
import cluedo.utility.Heading;
import cluedo.utility.Heading.Direction;

/**
 * The Board for the Cluedo game
 * Keeps track of the location of Pieces in the game
 *
 */
public class Board 
{
	public static final int WIDTH = 24;
	public static final int HEIGHT = 25;
	
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
	private final Cell[][] cells;
	
	public Board(Cell[][] cells) 
	{ 
		this.cells = cells;
		cellHasPiece = new HashMap<Cell, Piece>();
		pieceOnCell = new HashMap<Piece, Cell>();
	}
	
	/**
	 * Move the player at a specified direction
	 * 
	 * @param piece - Player's piece on the board to move
	 * @param direction - The direction the piece is moving to
	 * @return Cell - The new cell position of the piece
	 * @throws InvalidMoveException 
	 * If the current player cannot move as there is another player in the
	 * way or a wall is blocking the way
	 * @throws IllegalMethodCallException 
	 * If the piece does not exist
	 * @throws IllegalArgumentException 
	 * If the arguments are null
	 */
	public Cell move(Piece piece, Direction direction) throws InvalidMoveException
	{
		if(piece == null || direction == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if(!pieceOnCell.containsKey(piece))
		{
			throw new IllegalMethodCallException("Cannot move the piece as it does not exist");
		}
		Cell onPiece = pieceOnCell.get(piece);
		/*
		 * Walls are not defined in both cells 
		 * (i.e. cell may not have a South wall
		 * but neighbouring cell will have North wall)
		 *  so have to check both cells if there is a wall
		 */
		if(onPiece.hasWall(direction))
		{
			throw new InvalidMoveException("Cannot move in that direction as a wall is blocking the way");
		}
		Cell newPos = getNeighbouringCell(onPiece,direction);
		if(newPos.hasWall(Heading.opposite(direction)))
		{
			throw new InvalidMoveException("Cannot move in that direction as a wall is blocking the way");
		}
		if(containsPiece(newPos))
		{
			throw new InvalidMoveException("Cannot move to a cell with another player on it");
		}
		this.setPosition(piece, newPos);
		return newPos;
	}
	
	public boolean containsPiece(Cell cell)
	{
		return cellHasPiece.get(cell) != null;
	}
	
	/**
	 * Get the cell position of a particular piece
	 * @param piece
	 * @return Cell - The cell the piece is on
	 * @throws IllegalArgumentException 
	 * If the piece does not exist or is null
	 */
	public Cell getPosition(Piece piece)
	{
		if(piece == null)
		{
			throw new IllegalArgumentException("Argument is null");
		}
		if(!pieceOnCell.containsKey(piece)){
			throw new IllegalArgumentException("Not a valid piece: " + piece);
		}
		return pieceOnCell.get(piece);
	}
	/**
	 * Sets the piece to the specified cell position
	 * 
	 * Used for setting:
     * Secret passage usage
     * Suggestions to move weapons and Pieces (if needed)
     * Entering/exiting rooms
	 *
	 * @param piece
	 * @param cell  
	 * @throws IllegalArgumentException
	 * If the arguments are null
	 */
	public void setPosition(Piece piece, Cell cell)
	{
		if(piece == null || cell == null)
		{
			throw new IllegalArgumentException("Arguments are null");
		}
		//Check if the cell is in the array;
		int x = cell.getX();
		int y = cell.getY();
		if(cells[x][y] != cell)
		{
			throw new IllegalArgumentException("Cell is not a cell on the board");
		}
		//Remove previous position
		Cell previous = pieceOnCell.get(piece);
		cellHasPiece.remove(previous);
		//Put new position
		cellHasPiece.put(cell,piece);
		pieceOnCell.put(piece,cell);
	}
	/**
	 * Sets the piece to the specified cell position
	 * Gets the cell based on the x and y position
	 * Used for setting:
	 * Starting position of pieces
	 *
	 * @param piece
	 * @param x
	 * @param y
	 * @throws IllegalArgumentException
	 * If the arguments are null or x and y is out of the board's bounds
	 */
	public void setPosition(Piece piece, int x, int y )
	{
		if(piece == null)
		{
			throw new IllegalArgumentException("Arguments are null");
		}
		if(x >= Board.WIDTH || x < 0 || y >= Board.HEIGHT || y < 0)
		{
			throw new IllegalArgumentException("Coordinates out of the board's boundaries");
		}
		//Remove previous position
		Cell previous = pieceOnCell.get(piece);
		cellHasPiece.remove(previous);
		//Put new position
		Cell cell = cells[x][y];
		cellHasPiece.put(cell,piece);
		pieceOnCell.put(piece,cell);
	}
	
	/**
	 * @return The board representation (a 2D array of Cells)
	 */
	public Cell[][] getCells() 
	{
		return cells;
	}
	/**
	 * Get the neighbouring cell in the specified direction
	 * from a given cell.
	 * @param cell - the cell we are moving from
	 * @param direction - the direction the neighbouring cell is
	 * @return The new cell position
	 * @throws IllegalArgumentException
	 * If the direction is invalid or the neighbouring cell 
	 * does not exist as it is outside the board's boundaries.
	 * Also if the parameters are null
	 */
	public Cell getNeighbouringCell(Cell cell, Direction direction)
	{
		if(cell == null || direction == null)
		{
			throw new IllegalArgumentException("Arguments are null");
		}
		int x = cell.getX();
		int y = cell.getY();
		if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
		{
			throw new IllegalArgumentException("Cell is outside the board's boundaries");
		}
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
