package cluedo.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cluedo.model.Cell;
import cluedo.utility.Heading.Direction;

/**
 * Creates the Cells for building the Board.
 * 
 * The Board is defined by a 2D array of Cells.
 * 
 * Walls are defined inside the cell that has them (cells inside rooms own walls, not the cell inside the hallway).
 * Walls inside the mansion are owned by the cells inside the mansion rather than out.
 * This means that the outside cells are all 0000 or starting positions.

 * This allows for the UI to draw each wall exactly once - but means the Game needs to check for walls on
 * both this Cell and the Cell being moved to.
 */
class CellBuilder
{
	private final Cell[][] cells;
	
	private final String[][] map =
		{
				{ "B000", "B000", "B000", "B000", "B000", "B000", "B000", "B000", "B000", "NWEA", "B000", "B000", "B000", "B000", "NWEA", "B000", "B000", "B000", "B000", "B000", "B000", "B000", "B000", "B000" },
				{ "NW00", "N000", "N000", "N000", "N000", "NE00", "0000", "NW00", "N000", "0000", "NW00", "N000", "N000", "NE00", "0000", "N000", "NE00", "0000", "NW00", "N000", "N000", "N000", "N000", "NE00" },
				{ "W000", "0000", "0000", "0000", "0000", "E000", "N000", "0000", "NW00", "N000", "0000", "0000", "0000", "0000", "N000", "NE00", "0000", "N000", "W000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "WD00", "0000", "0000", "0000", "0000", "ES00" },
				{ "WS00", "0000", "0000", "0000", "0000", "E000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "D000", "WS00", "S000", "S000", "ESX0", "B000" },
				{ "B000", "WS00", "S000", "S000", "D000", "ES00", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "NESA" },
				{ "NSW0", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "SW00", "D000", "S000", "S000", "S000", "S000", "D000", "ES00", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "B000" },
				{ "B000", "W000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "NW00", "N000", "N000", "N000", "N000", "NE00" },
				{ "NW00", "N000", "N000", "N000", "NE00", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "N000", "N000", "NE00", "0000", "0000", "NWCC", "NCCC", "NCCC", "NCCC", "NECC", "0000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "WCCC", "CCCC", "CCCC", "CCCC", "ECCC", "0000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "WCCC", "CCCC", "CCCC", "CCCC", "ECCC", "0000", "0000", "0000", "WS00", "S000", "S000", "S000", "D000", "SE00" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "WCCC", "CCCC", "CCCC", "CCCC", "ECCC", "0000", "0000", "0000", "0000", "0000", "D000", "0000", "DE00", "B000" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "WCCC", "CCCC", "CCCC", "CCCC", "ECCC", "0000", "0000", "0000", "NW00", "N000", "D000", "N000", "NE00", "B000" },
				{ "WS00", "S000", "S000", "S000", "S000", "S000", "D000", "SE00", "0000", "0000", "WCCC", "CCCC", "CCCC", "CCCC", "ECCC", "0000", "0000", "NW00", "0000", "0000", "0000", "0000", "0000", "NE00" },
				{ "B000", "W000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "0000", "SWCC", "SCCC", "SCCC", "SCCC", "SECC", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "0000", "E000" },
				{ "NSWA", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "0000", "0000", "0000", "SW00", "0000", "0000", "0000", "0000", "0000", "SE00" },
				{ "B000", "W000", "0000", "0000", "0000", "0000", "D000", "0000", "0000", "NW00", "N000", "D000", "D000", "N000", "NE00", "0000", "0000", "0000", "SW00", "S000", "S000", "S000", "SE00", "B000" },
				{ "NWX0", "N000", "N000", "N000", "N000", "N000", "DE00", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "NESA" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "D000", "D000", "0000", "D000", "0000", "0000", "0000", "0000", "E000", "B000" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "DW00", "N000", "N000", "N000", "N000", "N000", "NEX0" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "E000", "0000", "0000", "W000", "0000", "0000", "0000", "0000", "0000", "E000" },
				{ "W000", "0000", "0000", "0000", "0000", "0000", "SE00", "0000", "S000", "SW00", "0000", "0000", "0000", "0000", "SE00", "S000", "0000", "SW00", "0000", "0000", "0000", "0000", "0000", "E000" },
				{ "SW00", "S000", "S000", "S000", "S000", "SE00", "B000", "ESWA", "B000", "B000", "SW00", "S000", "S000", "SE00", "B000", "B000", "ESW0", "B000", "SW00", "S000", "S000", "S000", "S000", "SE00" },
		};

	CellBuilder()
	{
		/*
		 * Markers for the map:
		 * North, South, East, West are NSEW.
		 * 
		 * Place holder is 0 to make the map appear as square as possible.
		 * Secret passages are X.
		 * Starting positions are A.
		 * Each side of the doors are D.
		 * Cells that are out of bounds are B.
		 * Note: The bottom of the hall has a wall (is not a rectangle). The wall on the actual board is half way between a cell.
		 */ 

		cells = new Cell[map[0].length][map.length];

		for (int x = 0; x < cells.length; x++)
		{
			for (int y = 0; y < cells[0].length; y++)
			{
				String s = map[y][x].toUpperCase();
				
				cells[x][y] = new Cell(x, y, wallsFromString(s));
			}
		}
	}
	
	public Set<Cell> getSecretPassageCells()
	{
		Set<Cell> secretPassage = new HashSet<Cell>();
		
		for (int x = 0; x < cells.length; x++)
		{
			for (int y = 0; y < cells[0].length; y++)
			{
				String s = map[y][x].toUpperCase();
				if (s.contains("X"))
				{
					secretPassage.add(cells[x][y]);
				}
			}
		}
		
		return secretPassage;
	}
	
	public Set<Cell> getDoorCells()
	{
		Set<Cell> doors = new HashSet<Cell>();
		
		for (int x = 0; x < cells.length; x++)
		{
			for (int y = 0; y < cells[0].length; y++)
			{
				String s = map[y][x].toUpperCase();
				if (s.contains("D"))
				{
					doors.add(cells[x][y]);
				}
			}
		}
		
		return doors;
	}
	
	public Set<Cell> getOutOfBoundsCells()
	{
		Set<Cell> outOfBounds = new HashSet<Cell>();
		
		for (int x = 0; x < cells.length; x++)
		{
			for (int y = 0; y < cells[0].length; y++)
			{
				String s = map[y][x].toUpperCase();
				if (s.contains("B"))
				{
					outOfBounds.add(cells[x][y]);
				}
			}
		}
		
		return outOfBounds;
	}
	
	public Cell[][] getCells()
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
}