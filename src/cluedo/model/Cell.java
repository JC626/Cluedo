package cluedo.model;

import java.util.Set;
import java.util.TreeSet;

import cluedo.utility.Heading.Direction;

/**
 * The basic unit within a Board.
 * Contains a Displayable aspect that is displayed when no other Displayable is in this Cell.
 * Each Cell has final x and y position, which can be used to distinguish one Cell from another.
 */
public class Cell
{
	/*
	 * x and y are represent the position of this Cell.
	 * Used by the Board for it's internal representation, as well as the UI
	 * to display the board.
	 * 
	 *  May not be negative.
	 */
	
	private final int x;
	private final int y;
	
	/**
	 * The UI method of displaying this Cell. Typically this will only be used if another object is not in the same place as the Cell.
	 */
	private final Displayable background;
	
	/**
	 * The walls that this Cell has.
	 * A wall indicates that any Piece in this Cell can't move in that direction.
	 * 
	 * A Cell as 0 .. 4 walls, which are determined on construction.
	 */
	private final Set<Direction> walls = new TreeSet<Direction>();

	/**
	 * The basic unit within a Board.
	 * @param x The x position of the Cell on the board. May not be negative.
	 * @param y The y position of the Cell on the board. May not be negative.
	 * @param background The Displayable aspect of this cell. May not be null.
	 * @param walls The walls that this cell has. May not be null or, contain null or duplicate items.
	 * @throws IllegalArgumentException if any of the above conditions are not met.
	 */
	public Cell(int x, int y, Displayable background, Direction ... walls)
	{
		if (x < 0 || y < 0)
		{
			throw new IllegalArgumentException("x and y may not be negative");
		}
		
		if (background == null || walls == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}

		// Not an error per se, but it indicates that the caller is using the constructor incorrectly.
		if (walls.length > Direction.values().length)
		{
			throw new IllegalArgumentException("walls may not contain duplicate items");
		}
		
		for (Direction d : walls)
		{
			if (d == null)
			{
				throw new IllegalArgumentException("walls may not contain null items");
			}
			
			if (this.walls.contains(d)) // We've come across this Direction before, this suggests the caller is using the constructor incorrectly.
			{
				throw new IllegalArgumentException("walls may not contain duplicate items");
			}
			
			this.walls.add(d);
		}
		
		this.background = background;
		this.x = x;
		this.y = y;
	}

	/**
	 * Determine the location of walls.
	 * Note that even if this method returns false it doesn't guarantee the adjacent cell is safe to move to
	 * as there may be a Piece on that Cell (stored by the Game).
	 * @param d The direction in which to test if there is a wall there.
	 * @return true if there is a wall in Direction d, false otherwise.
	 */
	public boolean hasWall(Direction d)
	{
		return walls.contains(d);
	}
	
	/**
	 * The background representation for this Cell.
	 * @return A Displayble aspect, that should only be used if there is not another Displayable in this Cell.
	 */
	public Displayable getBackground()
	{
		return background;
	}
	
	/**
	 * The x position of this Cell.
	 * @return a non negative int (may be 0).
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * The y position of this Cell.
	 * @return a non negative int (may be 0).
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 * Two Cells are considered equal iff their x and y values are equal.
	 */
	public boolean equals(Object o)
	{
		boolean isEqual = false;
		
		if (o instanceof Cell)
		{
			Cell c = (Cell) o;
			isEqual = this.getX() == c.getX()
					&& this.getY() == c.getY();
		}
		
		return isEqual;
	}
}
