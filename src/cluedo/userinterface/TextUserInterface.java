package cluedo.userinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cluedo.board.Board;
import cluedo.game.Game;

import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.utility.Heading.Direction;

/**
 * A user interface, for human players, that consists of input and output from the command line.
 */
public class TextUserInterface
{
	private static final String BANNER = " ____    ___                       __            \n" +
			"/\\  _`\\ /\\_ \\                     /\\ \\           \n" +
			"\\ \\ \\/\\_\\//\\ \\    __  __     __   \\_\\ \\    ___   \n" +
			" \\ \\ \\/_/_\\ \\ \\  /\\ \\/\\ \\  /'__`\\ /'_` \\  / __`\\ \n" +
			"  \\ \\ \\_\\ \\\\_\\ \\_\\ \\ \\_\\ \\/\\  __//\\ \\_\\ \\/\\ \\_\\ \\\n" +
			"   \\ \\____//\\____\\\\ \\____/\\ \\____\\ \\___,_\\ \\____/\n" +
			"    \\/___/ \\/____/ \\/___/  \\/____/\\/__,_ /\\/___/ \n" +
			"                                                 \n";
	private static final String menuFormat = "    [%d] %s";
	private static final String userPrompt = "> ";
	private static final String shortcutDisplayCommand = "shortcuts";

	private static final Character horizontalLine = 'W';//'\u2550';
	private static final Character verticalLine = 'W';//'\u2551';

	private static final Character topLeftCorner = 'W';//'\u2554';
	private static final Character topRightCorner = 'W';//'\u2557';

	private static final Character bottomLeftCorner = 'W';//'\u255A';
	private static final Character bottomRightCorner = 'W';//'\u255D';

	// Displayable characters:
	private final String emptyCell = "-";
	private final String cellWall = "W";

	private final GameOptions gameOptions = new GameOptions();
	private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	private final Map<Cell, Piece> tokens = new HashMap<Cell, Piece>();

	// The size of the buffer is the number of Cells, multiplied by the size of each cell (3 by 3).
	private final Character[][] drawingBuffer = new Character[3 * Board.WIDTH][3 * Board.HEIGHT]; // The drawing buffer, which will be modified in layers based in game state.

	private Game game;


















	private void newGame()
	{
		int numberOfPlayers = promptMenuNumber("Select players: ", Game.MIN_HUMAN_PLAYERS, Game.MAX_PLAYERS, " players");

		List<Piece> weaponTokens = createWeaponTokens();
		List<Piece> playerTokens = createPlayerTokens();

		List<Displayable> suspectCards = createSuspectCards();
		List<Displayable> weaponCards = createWeaponCards();
		List<Displayable> roomCards = createRoomCards();

		game = new Game(numberOfPlayers, playerTokens, weaponTokens, suspectCards, weaponCards, roomCards);

		runGame();
	}

	private void runGame()
	{
		printCanonBackground();

		while (!game.isGameOver())
		{
			Player currentPlayer = game.nextTurn();

			int moves = game.getRemainingMoves();

			while (game.getRemainingMoves() >= 0) 
			{

				List<String> options = new ArrayList<String>();
				List<String> regex = new ArrayList<String>();

				if (game.isInRoom()) 
				{
					/*
					 * If the current player is in a room, then they can do the following:
					 * 
					 * Make a suggestion
					 * Exit the room (if they haven't just entered)
					 */

					options.add("Exit the " + game.getRoom(game.getPosition(currentPlayer.getPiece())).getName());
					regex.add("exit|leave|go");

					if (game.canMakeSuggestion())
					{
						options.add("Make a suggestion");
						regex.add("suggest|"); // The default action is to suggest
					}
					else // The user cannot make a suggestion, so make leaving the default.
					{
						regex.add(regex.remove(0) + "|"); 
					}
				}
				else
				{
					/*
					 * The player can:
					 * 
					 * Move
					 */
					options.add("Move");
					regex.add("(n|s|e|w)+");
				}

				if (gameOptions.printBoardAtStartTurn)
				{
					printBoard();
				}

				executeDefaultMenu(currentPlayer.getName(), options, regex);

				continuePromptMenu();
			}
		}
	}

	private void printBoard()
	{
		generateBoard();

		for (int y = 0; y < drawingBuffer[0].length; y++)
		{
			for (int x = 0; x < drawingBuffer.length; x++)
			{
				print(drawingBuffer[x][y].toString());
			}
			println();
		}
	}

	private void generateBoard()
	{
		Cell[][] board = game.getCells();

		for (int y = 0; y < board.length; y++)
		{
			for (int x = 0; x < board[y].length; x++)
			{
				addCellLayerDrawingBuffer(board[y][x]);
			}
		}

		addPlayerLayerDrawingBuffer();
		//addWeaponLayerDrawingBuffer();
	}

	private void addPlayerLayerDrawingBuffer()
	{
		Cell[][] board = game.getCells();
		List<Player> players = game.getAllPlayers();

		for (int y = 0; y < board.length; y++)
		{
			for (int x = 0; x < board[y].length; x++)
			{
				for (Player p : players)
				{
					Cell playerLocation = game.getPosition(p.getPiece());
					
					if (board[x][y].equals(playerLocation)) // Draw the Piece in this Cell
					{
						drawingBuffer[x + 1][y + 1] = getPlayerDisplayable(p);
					}
				}

			}
		}
	}

	/**
	 * As GameBuilder is non public and doesn't provide
	 * the names in a public and fixed manner we need to
	 * hard code the names here. If the names or order changes
	 * then this method will need to change too.
	 * @param p The player whose character we need to represent.
	 * @return The character representing p.
	 */
	private Character getPlayerDisplayable(Player p)
	{
		Character playerDisplayable = ' ';
		
		switch (p.getName())
		{
			case "Miss Scarlett":
				playerDisplayable = 'S';
				break;
			case "Colonel Mustard":
				playerDisplayable = 'C';
				break;
			case "Mrs. White":
				playerDisplayable = 'w';
				break;
			case "Reverend Green":
				playerDisplayable = 'G';
				break;
			case "Mrs. Peacock":
				playerDisplayable = 'p';
				break;
			case "Professor Plum":
				playerDisplayable = 'P';
				break;
			default:
				// Player not recognised, don't draw them
		}
		
		return playerDisplayable;
	}

	private void addCellLayerDrawingBuffer(Cell cell)
	{
		// Each cell is 3*3, so we multiply the Cell location by 3 to avoid overwriting other Cell's walls.
		int x = 3 * cell.getX();
		int y = 3 * cell.getY();

		boolean north = cell.hasWall(Direction.North);
		boolean south = cell.hasWall(Direction.South);
		boolean east = cell.hasWall(Direction.East);
		boolean west = cell.hasWall(Direction.West);

		// Top row
		drawingBuffer[x][y] = cellTopLeft(north, west);
		drawingBuffer[x + 1][y] = cellTopCentre(north);
		drawingBuffer[x + 2][y] = cellTopRight(north, east);

		// Middle row
		drawingBuffer[x][y + 1] = cellMiddleLeft(west);
		drawingBuffer[x + 1][y + 1] = cellMiddleCentre();
		drawingBuffer[x + 2][y + 1] = cellMiddleRight(east);

		// Bottom row
		drawingBuffer[x][y + 2] = cellBottomLeft(south, west);
		drawingBuffer[x + 1][y + 2] = cellBottomCentre(south);
		drawingBuffer[x + 2][y + 2] = cellBottomRight(east, south);
	}

	private Character cellBottomRight(boolean east, boolean south)
	{
		Character result = ' ';
		if (south)
		{
			if (east) // Corner piece
			{
				result = bottomLeftCorner;
			}
			else // Horizontal piece 
			{
				result = horizontalLine;
			}
		}
		else if (east) // Vertical piece
		{
			result = verticalLine;
		} // Else an empty space.

		return result;
	}

	private Character cellBottomCentre(boolean south)
	{
		return (south) ? horizontalLine : ' ';
	}

	private Character cellBottomLeft(boolean south, boolean west)
	{
		Character result = ' ';
		if (south)
		{
			if (west) // Corner piece
			{
				result = bottomLeftCorner;
			}
			else // Horizontal piece 
			{
				result = horizontalLine;
			}
		}
		else if (west) // Vertical piece
		{
			result = verticalLine;
		} // Else an empty space.

		return result;
	}

	private Character cellMiddleRight(boolean east)
	{
		return (east) ? verticalLine : ' ';
	}

	private Character cellMiddleCentre()
	{
		return ' '; // This will be overridden if there's a weapon or player there.
	}

	private Character cellMiddleLeft(boolean west)
	{
		return (west) ? verticalLine : ' ';
	}

	private Character cellTopRight(boolean north, boolean east)
	{
		Character result = ' ';
		if (north)
		{
			if (east) // Corner piece
			{
				result = topRightCorner;
			}
			else // Horizontal piece 
			{
				result = horizontalLine;
			}
		}
		else if (east) // Vertical piece
		{
			result = verticalLine;
		} // Else an empty space.

		return result;
	}

	private Character cellTopCentre(boolean north)
	{
		return (north) ? horizontalLine : ' ';
	}

	private Character cellTopLeft(boolean north, boolean west)
	{
		Character result = ' ';
		if (north)
		{
			if (west) // Corner piece
			{
				result = topLeftCorner;
			}
			else // Horizontal piece 
			{
				result = horizontalLine;
			}
		}
		else if (west) // Vertical piece
		{
			result = verticalLine;
		} // Else an empty space.

		return result;
	}

	/**
	 * Print out a menu, and return the index + 1 of the option selected from menuOptions.
	 * For creating menus, one should usually use executeDefaultMenu as it handles some default
	 * cases automatically.
	 * We return index + 1 because that's the option the user is shown, and is a clearer way of thinking about
	 * the menus (case 1, case 2, case 3... case n).
	 * @param menuTitle
	 * @param menuOptions
	 * @param regexMatches
	 * @return
	 */
	private int executeMenu(String menuTitle, List<String> menuOptions, List<String> regexMatches)
	{
		final int userSelectionSentinel = -1;
		int userSelection = userSelectionSentinel;

		updateMenuOptions(menuOptions, regexMatches);

		// Print out the options available to the user
		println(menuTitle);
		println();
		printMenu(menuOptions);

		// Get the user input, and loop until we have something valid.
		do
		{
			try
			{

				print(userPrompt);
				String answer = input.readLine().toLowerCase();

				if (answer.equals(shortcutDisplayCommand))
				{
					// Display shortcuts for this set of commands.
					printShortcuts(menuOptions, regexMatches);
				}
				else
				{
					for (int i = 0; i < regexMatches.size(); i++)
					{
						// Entering the number is always acceptable (1 for the first option, 2 for the second etc).
						// Users expect the first option is number 1, so the user can press 1 for the first option.
						String thisIndexRegex = i + 1 + "|";

						if (Pattern.matches(thisIndexRegex + regexMatches.get(i).toLowerCase(), answer))
						{
							userSelection = i + 1; // We add 1 here to match the shown/accepted number instead of the index because we usually deal with the user selection in a switch.
							break;
						}
					}
				}
			}
			catch (IOException e)
			{
				if (gameOptions.verboseErrors)
				{
					e.printStackTrace();
				}
				println();
				print("Sorry, I couldn't hear you. Could you please repeat that?");
			}
		} while (userSelection == userSelectionSentinel);

		return userSelection;
	}

	private void printShortcuts(List<String> menuOptions, List<String> regexMatches)
	{
		// We make a new list, so we don't change either of the parameters
		// If we were to change (e.g. the menuOptions) then if this method is called multiple times
		// the list of shortcuts would grow - we don't want that.
		List<String> menuWithShortcuts = new ArrayList<String>();

		for (int i = 0; i < menuOptions.size(); i++)
		{
			menuWithShortcuts.add(menuOptions.get(i) + humanReadableFromRegex(regexMatches.get(i)));
		}

		printMenu(menuWithShortcuts);
	}

	private void updateMenuOptions(List<String> options, List<String> regex)
	{
		for (int i = 0; i < options.size(); i++)
		{
			String regexOption = options.get(i).toLowerCase(); // The menu text is a valid option 

			if (i < regex.size()) // There is a regex for this option
			{
				String oldRegex = regex.get(i);
				regex.remove(i);
				regexOption = oldRegex + "|" + regexOption;
			}

			regex.add(i, regexOption);
		}
	}

	private void printMenu(List<String> menuOptions)
	{
		for (int i = 0; i < menuOptions.size(); i++)
		{
			printMenuItem(menuFormat, i + 1, menuOptions.get(i));
		}
	}

	private void printMenuItem(String format, Object ... item)
	{
		println(String.format(format, item));
	}



	/**
	 * A wrapper for makeMenu, providing some default options
	 * @param playerName
	 * @param menuOptions
	 * @param regexMatches
	 * @return The index of the 
	 */
	private int executeDefaultMenu(String playerName, List<String> menuOptions, List<String> regexOptions)
	{
		String menuTitle = String.format("%s, what do you want to do next?", playerName);

		int userSelection = 0; // The compiler claims that userSelection may not have been initialised, we know however that it will be.
		boolean selectedCallerOption = false; // The 

		/*
		 * At any time we want the user to be able to reprint out the board, their remaining moves, view their 
		 * case file or hand, and make an accusation.
		 * We add these options before printing out the menu options because it allows them to remain in a 
		 * consistent place between different menu options.
		 * User testing suggested that the order should be:
		 * 
		 * Reprint remaining moves
		 * Print board
		 * Review evidence (hand, and case file options)
		 * Make an accusation
		 */

		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		while (!selectedCallerOption)
		{
			// Add our default options.
			options.clear(); // Don't double up the options added from the caller.
			options.add("Reprint remaining moves");
			options.add((gameOptions.printBoardAtStartTurn) ? "Reprint board" : "Print board"); // The text for the printing of the board option depends on the settings set by the user.
			options.add("Review evidence"); // Leads on to view hand, and view case file
			options.add("Make an accusation");

			// And their associated regexs.
			// Adding regexs here (even empty ones) is necessary to line up the caller's regexs with their options.
			regex.clear(); // Clear to avoid doubling up automatically added regex.
			regex.add("r|remaining");
			regex.add("b|board");
			regex.add("h|cf|hand|case file|evidence");
			regex.add("a|accuse");

			options.addAll(menuOptions); // Add all of the caller provided menu options.
			regex.addAll(regexOptions); // And their associated regexs.

			userSelection = executeMenu(menuTitle, options, regex);

			/*
			 * We deal with options 1 to 4, so the calling function doesn't need to.
			 */
			switch (userSelection)
			{
				case 1:
					println("You have " + game.getRemainingMoves() + " remaining.");
					break;
				case 2:
					printBoard();
					break;
				case 3:
					printCaseFile();
					break;
				case 4:
					promptMakeAccusation();
					break;
				default: // One of the caller's options was selected, pass it back to them.
					selectedCallerOption = true; 
					userSelection = userSelection - 4; // Give the caller the index from what they gave. 4 is the number of items we have added.
			}
		}

		return userSelection;
	}

	private void promptMakeAccusation()
	{
		// TODO Auto-generated method stub

	}

	private void printCaseFile()
	{
		/*
		 * The case file is provided to us by Suspect, Room, and Weapon cards.
		 * We print out the cards that are in the global set but have not been
		 * marked off the player's case file. 
		 */
		//TODO

	}

	/**
	 * executeMenu expects a list of regexs. This is one method of providing a valid list.
	 * @param size The size of the list of regexs. If size is 0 or negative, an empty list is returned.
	 * @return An list of empty regexs, the size of num.
	 */
	private List<String> makeEmptyRegex(int size)
	{
		List<String> regex = new ArrayList<String>();

		for (int i = 0; i < size; i++)
		{
			regex.add("");
		}

		return regex;
	}

	private List<Piece> createPlayerTokens()
	{
		List<Piece> players = new ArrayList<Piece>();

		for (int player = 0; player < Game.MAX_HUMAN_PLAYERS; player++)
		{
			players.add(() -> {});
		}

		return players;
	}

	/**
	 * The values returned here are based on order of the values in Game.
	 */
	private List<Piece> createWeaponTokens()
	{
		List<Piece> weapons = new ArrayList<Piece>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createRoomCards()
	{
		// TODO Auto-generated method stub
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.NUM_ROOMS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createWeaponCards()
	{
		// TODO Auto-generated method stub
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createSuspectCards()
	{
		// TODO Auto-generated method stub
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.MAX_PLAYERS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}


	private void print(String s)
	{
		System.out.print(s);
	}

	private void println(String s)
	{
		print(s + "\n");
	}

	private void println()
	{
		println("");
	}


	public static void main(String[] args)
	{
		TextUserInterface t = new TextUserInterface();
		t.mainMenu();

		System.out.println("Thank you for playing!");
	}


	public void mainMenu()
	{
		int userSelection;
		boolean endGame = false;

		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		options.add("How to play");
		options.add("New game");
		options.add("Options");
		options.add("Quit");

		while (!endGame)
		{
			// If we don't clear and then re-add all of the items then the 
			// options accumulate the menu text (e.g. "Options") on each call
			// of "shortcuts".
			regex.clear();
			regex.add("h|\\?|help|tutorial");
			regex.add("n|new|start");
			regex.add("o|setting(s?)"); // match "setting" or "settings" but not "settingss" or any number of extra 's'
			regex.add("q|exit");


			printGreeting();

			userSelection = executeMenu("Press ? or 1 for a tutorial", options, regex);

			switch (userSelection)
			{
				case 1:
					printInstructions();
					break;
				case 2:
					newGame();
					break;
				case 3:
					setOptions();
					break;
				case 4:
					// Done, exit; note the lack of a break here.
				default:
					endGame = true;
			}

			printBlankLines(7); // FIXME 7 was also chosen arbitrarily
		}
	}

	private void printGreeting()
	{
		// Banner courtesy of http://www.desmoulins.fr/index_us.php?pg=scripts!online!asciiart
		print(BANNER);
		println("A text based detective game -- now with advanced graphics!");
		printBlankLines(3); // Give some space for readability.
	}

	/**
	 * Modifies the global gameOptions with input the user selects.
	 * This method grows almost linearly with the number of options.
	 */
	private void setOptions()
	{
		int userSelection;
		boolean finished = false; // Done changing the options

		String booleanMenuPrompt = "What would you like to set '%s' to?";

		String menuTitle = "Select an option to change:\nBrackets denote the current value.";
		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		regex.add("|back|exit"); // User can press enter to go back
		regex.add("board");
		regex.add("errors");

		do
		{
			// Menu option options need to be added and revised every time to reflect changed settings.
			options.clear();
			options.add("Go back");
			options.add("Print board at the start of every turn [" + gameOptions.printBoardAtStartTurn + "]");
			options.add("Print verbose errors [" + gameOptions.verboseErrors + "]");
			userSelection = executeMenu(menuTitle, options, regex);

			switch (userSelection)
			{
				case 1:
					finished = true;
					break;
				case 2:
					gameOptions.printBoardAtStartTurn = promptMenuBoolean(String.format(booleanMenuPrompt, "Print board at the start of every turn"));
					break;
				case 3:
					gameOptions.verboseErrors = promptMenuBoolean(String.format(booleanMenuPrompt, "Print verbose errors"));
					break;
				default:
					// Do nothing
			}
		} while (!finished);
	}

	/**
	 * Wrapper for the more verbose promptMenuBoolean, with some reasonable defaults.
	 * @param question The question to be asked presented to the user.
	 * @return The user selection, True for an affirmative, false otherwise.
	 */
	private boolean promptMenuBoolean(String question)
	{
		return promptMenuBoolean(question, "True", "False");
	}

	/**
	 * Ask the user a boolean question.
	 * Wrapper for executeMenu with reasonable regex options.
	 * @param question The question to be presented to the user.
	 * @param truePrompt Affirmative answer.
	 * @param falsePrompt Negative answer.
	 * @return The user selection, true for truePrompt, false otherwise.
	 */
	private boolean promptMenuBoolean(String question, String truePrompt, String falsePrompt)
	{
		boolean userAnswer;

		// Setup our menus
		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		options.add(truePrompt);
		options.add(falsePrompt);

		regex.add("t|yes");
		regex.add("f|no");

		userAnswer = (executeMenu(question, options, regex) == 1) ? true : false;

		return userAnswer;
	}

	/**
	 * Returns the user's selected number.
	 * @param min The minimum allowable number.
	 * @param max The maximum allowable number.
	 * @return An integer between min and max inclusive.
	 */
	private int promptMenuNumber(String prompt, int min, int max, String postfix)
	{
		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		for (int i = min; i <= max; i++)
		{
			options.add(Integer.toString(i) + postfix);
		}

		// ExecuteMenu returns the number of the menu item the user selected.
		// The first one starts at 1, so we need to minus 1, and then add min to get
		// the minimum value as presented.
		return min + (executeMenu(prompt, options, regex) - 1);
	}

	private void printInstructions()
	{
		printControls();
		printGameRules();
	}

	private void printControls()
	{
		println("There are a number of ways you can interact with the menus in Cluedo.");

		println("One easy way to select a menu item is to type in the number to the left of the option:");

		// Give the users an example
		println();
		printMenuItem(menuFormat, 4, "Options");
		println(userPrompt + "4");
		println();

		println("Another easy way is to type in one of the many shortcuts available.");
		println("To view the shortcuts for any given set of commands enter \"" + shortcutDisplayCommand + "\"");
		println("For the above example, we could have easily typed " + shortcutDisplayCommand + ":");

		// Continue our example
		println();
		println(userPrompt + shortcutDisplayCommand);
		println();
		printMenuItem(menuFormat, 4, "Options" +  humanReadableFromRegex("o|setting(s?)"));
		println(userPrompt + "settings");
		println();

		println("As a final tip, when you're in a hallway (you'll learn more about those below) instead of selecting Move you can just enter the direction you want to go.");

		// Movement example
		println();
		printMenuItem(menuFormat, 4, "Move" +  humanReadableFromRegex("m|move"));
		println(userPrompt + "n");
		println();

		println("This will move you one step to the north.");
		println("You can also string together multiple moves at once!");

		// Movement example
		println();
		printMenuItem(menuFormat, 4, "Move" +  humanReadableFromRegex("m|move"));
		println(userPrompt + "nnneeews");
		println();

		println("That will move your character north three steps, east three steps, then west and finally south.");
		println("You'll be warned if you don't have enough remaining moves or you've entered an invalid direction.");
		println("Keep in mind, if your path leads you into a wall your movement will stop and you'll be asked for a different set of moves having already made the moves to get to this point.");

		continuePromptMenu();
	}

	private void continuePromptMenu()
	{
		List<String> menu = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		menu.add("Press enter to continue");
		regex.add("");
		executeMenu("", menu, regex);
	}

	private String humanReadableFromRegex(String regex)
	{
		// TODO transform the regex into something readable.
		return " {" + regex + "}";
	}

	private void printGameRules()
	{
		//TODO game rules
	}

	private void printCanonBackground()
	{
		// TODO Auto-generated method stub
	}

	private void printBlankLines(int lines)
	{
		for (int i = 0; i < lines; i++)
		{
			println();
		}
	}


	/**
	 * A small wrapper class for optional functionality in game.
	 * These values can be read or changed at will by any part of this class.
	 * 
	 * The values listed below are the defaults.
	 */
	private class GameOptions
	{
		boolean printBoardAtStartTurn = true;
		boolean verboseErrors = false; // Print out exception stack traces.
	}
}