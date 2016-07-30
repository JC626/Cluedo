package cluedo.tests;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.sun.corba.se.impl.util.Utility;

import cluedo.board.Board;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.model.Cell;
import cluedo.model.Piece;
import cluedo.utility.Heading.Direction;

public class BoardTests
{
	private Board board;
	private Piece validPiece = () -> { System.out.println("TEST: A valid piece"); };

	private Cell validCell;


	@Before
	public void setup()
	{
		setupValidCell();
		setupBoard();
	}

	/**
	 * Get a random valid cell, so the tests don't rely on a set value, e.g.
	 * new Cell(1, 4, Direction.West);
	 */
	public void setupValidCell()
	{
		validCell = cluedo.utility.tests.Cell.getRandomCell();
	}

	/**
	 * Create a new board with our valid piece and valid cell.
	 */
	public void setupBoard()
	{
		board = new Board(createBoard());
		board.setPosition(validPiece, validCell);
	}

	private Cell[][] createBoard()
	{
		Cell[][] cells = new Cell[Board.WIDTH][Board.HEIGHT];

		for (int y = 0; y < cells[0].length; y++)
		{
			for (int x = 0; x < cells.length; x++)
			{
				if (x == validCell.getX() && y == validCell.getY())
				{
					cells[x][y] = validCell;					
				}
				else
				{
					cells[x][y] = new Cell(x, y)
					{
						@Override
						public void display()
						{
							// An empty Cell, doesn't do anything.
						}
					};
				}
			}
		}



		return cells;
	}

	/**
	 * Get a random direction to reduce bias in Direction selection.
	 * @return A random direction.
	 */
	public Direction getRandomDirection()
	{
		return cluedo.utility.tests.Direction.getRandomDirection();
	}



	private void generateCell(Direction wall, boolean wallInDir)
	{
		// This could loop forever, but on average we'll get a 
		// valid cell in 4 attempts
		while (validCell.hasWall(wall) != wallInDir)
		{
			setupValidCell();
		}
	}


	@Test (expected = IllegalArgumentException.class)
	public void invalidMoveNullPiece() throws InvalidMoveException
	{
		board.move(null, getRandomDirection());
		fail("Board should throw IllegalArgumentException on null arguments");
	}

	@Test (expected = IllegalArgumentException.class)
	public void invalidMoveNullDirection() throws InvalidMoveException
	{
		board.move(validPiece, null);
		fail("Board should throw IllegalArgumentException on null arguments");
	}

	@Test (expected = IllegalMethodCallException.class)
	public void invalidMoveNonexistentPiece() throws InvalidMoveException 
	{
		board.move(() -> {}, getRandomDirection());
	}

	@Test (expected = InvalidMoveException.class)
	public void invalidMoveWallBlocking() throws InvalidMoveException 
	{
		// As there may not be a blocking wall for the direction
		// we want to go in for the default Cell, we'll iterate 
		// until we find one.
		Direction invalidDirection = getRandomDirection();
		generateCell(invalidDirection, true);

		board.setPosition(validPiece, validCell); // We changed our Cell, so we need to move our Piece
		board.move(validPiece, invalidDirection);
	}

	/*@Test (expected = InvalidMoveException.class)
	public void invalidMovePieceBlocking() throws InvalidMoveException 
	{
		Piece other = () -> {};
		Direction invalidDirection = getRandomDirection();
		generateCell(invalidDirection, false);

		System.out.println(invalidDirection);
		System.out.println(validCell.getX() + ", " + validCell.getY() + ": " + validCell.hasWall(invalidDirection));


		board.setPosition(other, validCell);
		board.move(validPiece, invalidDirection);
	}*/


}
