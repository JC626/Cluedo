package cluedo.board;

import java.util.ArrayList;
import java.util.List;

import cluedo.model.Cell;
import cluedo.utility.Heading.Direction;

/**
 * Creates the Cells for building the Board.
 * FIXME revise this
 * For reasons explained in the documentation, the UI needs to make all Cells.
 * This, in combination of Cells needing to print the things in them at the time of
 * their own printing and the lack of layering in a CLI leads to this class being
 * a private inner class.
 * 
 * Cells cannot contain game state, but need to access game state, in order to know
 * what they're to draw. This problem also exists with Players, and Weapons. For
 * Players and Weapons an acceptable solution was to have them contain Pieces (Displayable
 * aspects) because the drawing of the Piece doesn't effect anything else.
 * 
 * This is not a suitable solution for Cells because what is drawn, and where, depends 
 * partially on the game state - this means Cells need to access the state. If the Cells
 * contained the state themselves then it could be modified by the UI; the only alternative
 * is to have the Cells access state via the UI.
 * 
 */
class CellBuilder
{
	private final Cell[][] cells;

	CellBuilder()
	{
		/*
		 * North, South, East, West are NSEW.
		 * 
		 * Walls are defined inside the cell that has them (cells inside rooms own walls, not the cell inside the hallway).
		 * Walls inside the mansion are owned by the cells inside the mansion rather than out.
		 * This means that the outside cells are all 0000 or starting positions.
		 * 
		 * Currently undrawn, subject to change later are:
		 * i.e. just draw where the player is now.
		 * C is the center (needed?) It's not treated specially in the game... Used as a marker, for any changed later.
		 * 
		 * Place holder is 0 to make the map appear as square as possible.
		 * Secret passages are X.
		 * Each side of the doors are D.
		 * Note: The bottom of the hall has a wall (is not a rectangle). The wall on the actual board is half way between a cell.
		 */ 

		String[][] map =
			{
					{ "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "NWEA", "0000", "0000", "0000", "0000", "NWEA", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000" },
					{ "NW00", "N000", "N000", "N000", "N000", "NE00", "0000", "NW00", "N000", "0000", "NW00", "N000", "N000", "NE00", "0000", "N000", "NE00", "0000", "NW00", "N000", "N000", "N000", "N000", "NE000" },
					{ "W000", "0000", "0000", "0000", "0000", "000E", "N000", "0000", "NW00", "N000", "0000", "0000", "0000", "0000", "N000", "NE00", "0000", "N000", "W000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "WD00", "0000", "0000", "0000", "0000", "ES00" },
					{ "WS00", "0000", "0000", "0000", "0000", "E000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "D000", "WS00", "S000", "S000", "ESX0", "0000" },
					{ "0000", "WS00", "S000", "S000", "D000", "ES00", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "NESA" },
					{ "NSW0", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "SW00", "D000", "S000", "S000", "S000", "S000", "D000", "ES00", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000" },
					{ "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "NW00", "N000", "N000", "N000", "N000", "NE00" },
					{ "NW00", "N000", "N000", "N000", "NE00", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "N000", "N000", "NE00", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "0000", "WS00", "S000", "S000", "S000", "D000", "SE00" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "0000", "0000", "0000", "D000", "0000", "DE00", "0000" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "0000", "NW00", "N000", "D000", "N000", "NE00", "0000" },
					{ "WS00", "S000", "S000", "S000", "S000", "S000", "D000", "SE00", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "0000", "NW00", "0000", "0000", "0000", "0000", "0000", "NE00" },
					{ "0000", "W000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "CCCC", "CCCC", "CCCC", "CCCC", "CCCC", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "0000", "E000" },
					{ "NSWA", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "SW00", "0000", "0000", "0000", "0000", "0000", "SE00" },
					{ "0000", "W000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "NW00", "N000", "D000", "D000", "N000", "NE00", "0000", "0000", "0000", "SW00", "S000", "S000", "S000", "SE00", "0000" },
					{ "NWX0", "N000", "N000", "N000", "N000", "N000", "DE00", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "NESA" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "D000", "0000", "0000", "0000", "0000", "E000", "0000" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "DW00", "N000", "N000", "N000", "N000", "N000", "NEX0" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "E000" },
					{ "W000", "0000", "0000", "0000", "0000", "0000", "SE00", "0000", "S000", "SW00", "0000", "0000", "0000", "0000", "SE00", "S000", "0000", "SW00", "0000", "0000", "0000", "0000", "0000", "E000" },
					{ "SW00", "S000", "S000", "S000", "S000", "SE00", "0000", "ESWA", "0000", "0000", "SW00", "S000", "S000", "SE00", "0000", "0000", "ESW0", "0000", "SW00", "S000", "S000", "S000", "S000", "SE00" },
			};

		cells = new Cell[map.length][map[0].length];

		for (int row = 0; row < map.length; row++)
		{
			for (int col = 0; col < map[row].length; col++)
			{
				String s = map[row][col].toUpperCase();
				cells[row][col] = new CellImpl(col, row, wallsFromString(s));
			}
		}
	}
	
	Cell[][] getCells()
	{
		return cells;
	}

	private Direction[] wallsFromString(String wallDef)
	{
		List<Direction> walls = new ArrayList<Direction>();

		if (wallDef.contains("N"))
		{
			walls.add(Direction.North);
		}

		if (wallDef.contains("S"))
		{
			walls.add(Direction.South);
		}

		if (wallDef.contains("E"))
		{
			walls.add(Direction.East);
		}

		if (wallDef.contains("W"))
		{
			walls.add(Direction.West);
		}

		return walls.toArray(new Direction[walls.size()]);
	}


	private class CellImpl extends Cell
	{
		public CellImpl(int x, int y, Direction[] walls)
		{
			super(x, y, walls);
		}

		@Override
		public void display()
		{
			// The cell, for the text UI, doesn't display anything itself, but that task is instead handled by the UI.
		}
	}
}