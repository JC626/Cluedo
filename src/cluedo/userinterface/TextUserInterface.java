package cluedo.userinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluedo.game.Game;
import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.model.Piece;

/**
 * A user interface, for human players, that consists of input and output from the command line.
 */
public class TextUserInterface
{
	private final Map<Cell, Piece> tokens = new HashMap<Cell, Piece>(); // Weapon, or Player tokens
	private final Game game;

	public TextUserInterface()
	{
		List<Piece> weaponTokens = createWeaponTokens();
		List<Piece> playerTokens = createPlayerTokens();
		List<Cell> cells = createCells();
		
		List<Displayable> suspectCards = createSuspectCards();
		List<Displayable> weaponCards = createWeaponCards();
		List<Displayable> roomCards = createRoomCards();
		
		// Create method for generating menus
		// Get user input for number of players
		// Go over documentation to find other methods to write
		
		game = new Game(0, null, null, null, null, null, null);
	}



	private List<Cell> createCells()
	{
		List<Cell> cells = new ArrayList<Cell>();
		
		//TODO clean Builder
		//TODO integrate Builder
		
		return cells;
	}

	private List<Piece> createPlayerTokens()
	{
		List<Piece> players = new ArrayList<Piece>();
		
		players.add(() -> print("S")); // Miss Scarlett: lower case s is Spanner
		players.add(() -> print("M")); // Colonel Mustard
		players.add(() -> print("W")); // Mrs. White
		players.add(() -> print("G")); // Reverend Green
		players.add(() -> print("P")); // Mrs. Peacock: lower case p is Professor Plum
		players.add(() -> print("p")); // Professor Plum: upper case P is Mrs. Peacock
		
		return players;
	}

	/**
	 * The values returned here are based on order of the values in Game.
	 */
	private List<Piece> createWeaponTokens()
	{
		List<Piece> weapons = new ArrayList<Piece>();
		
		weapons.add(() -> print("D")); // Dagger
		weapons.add(() -> print("C")); // Candle stick
		weapons.add(() -> print("R")); // Revolver: lower case r is rope
		weapons.add(() -> print("r")); // Rope: upper case R is revolver
		weapons.add(() -> print("L")); // Lead Pipe
		weapons.add(() -> print("s")); // Spanner: upper case S is Miss Scarlett
		
		return weapons;
	}
	
	private List<Displayable> createRoomCards()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<Displayable> createWeaponCards()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<Displayable> createSuspectCards()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	
	
	
	private void print(String s)
	{
		System.out.print(s);
	}
	
	private void println(String s)
	{
		print(s + "\n");
	}
	
	private void print()
	{
		print("");
	}
	
	private void println()
	{
		println("");
	}
}
