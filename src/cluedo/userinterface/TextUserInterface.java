package cluedo.userinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import cluedo.game.Game;
import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.model.Piece;

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

	private final GameOptions gameOptions = new GameOptions();
	private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	private final Map<Cell, Piece> tokens = new HashMap<Cell, Piece>(); // Weapon, or Player tokens TODO do we need this?
	
	private Game game;

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
			int IOExceptionAttempts = 0; // We should tell the user what's going on in a friendly manner even if they have verboseErrors turned off.

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
				IOExceptionAttempts++;
				if (gameOptions.verboseErrors)
				{
					e.printStackTrace();
				}
				println();
				print("Sorry, I couldn't hear you. Could you please repeat that?");

				if (IOExceptionAttempts >= 3) // FIXME 3 was chosen arbitrarily. 
				{
					print(" (IO exception)");
				}

				println();
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
	private int executeDefaultMenu(String playerName, List<String> menuOptions, Optional<List<String>> regexMatches)
	{
		String menuTitle = String.format("%s, what do you want to do next?\n\n", playerName);

		/*
		 * At any time we want the user to be able to reprint out the board, their remaining moves, and make an accusation.
		 * We add these options before printing out the menu options because it allows them to remain in a consistent place
		 * between different calls.
		 * 
		 * Reprint remaining moves
		 * Print board
		 * Review evidence
		 * 
		 * 
		 */

		// TODO user tests suggested order of default options should be:
		// Reprint remaining moves
		// Reprint board
		// Print case file
		// Print hand

		List<String> options = new ArrayList<String>();
		// The text for the printing of the board option depends on the settings set by the user.


		// Add our default options
		options.add("Reprint remaining moves");
		options.add((gameOptions.printBoardAtStartTurn) ? "Reprint board" : "Print board");
		options.add("Review evidence"); // Leads on to view hand, and view case file

		options.addAll(menuOptions); // Add all of the caller provided menu options.

		int userSelection = executeMenu(menuTitle, options, makeEmptyRegex(options.size()));

		return userSelection;
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


	public static void main(String[] args)
	{
		TextUserInterface t = new TextUserInterface();
		t.startGame();

		System.out.println("Thank you for playing!");
	}


	public void startGame()
	{
		int userSelection;
		boolean endGame = false;

		List<String> options = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		options.add("How to play");
		options.add("New game");
		options.add("Load game");
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
			regex.add("l|load|resume");
			regex.add("o|setting(s?)"); // match "setting" or "settings" but not "settingss" or any number of extra 's'
			regex.add("q|exit");


			printBanner();
			printByline();

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
					println("Not yet implemented");
					break;
				case 4:
					setOptions();
					break;
				case 5:
					// Done, exit; note the lack of a break here.
				default:
					endGame = true;
			}

			printBlankLines(7); // FIXME 7 was also chosen arbitrarily
		}
	}

	/**
	 * Modifies the global gameOptions with input the user selects.
	 * This method grows almost linearly with the number of options
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

	private boolean promptMenuBoolean(String question)
	{
		return promptMenuBoolean(question, "True", "False");
	}

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

	private void newGame()
	{
		int numberOfPlayers = promptMenuNumber("Select players: ", Game.MIN_HUMAN_PLAYERS, Game.MAX_PLAYERS, " players");

		List<Piece> weaponTokens = createWeaponTokens();
		List<Piece> playerTokens = createPlayerTokens();
		List<Cell> cells = createCells();

		List<Displayable> suspectCards = createSuspectCards();
		List<Displayable> weaponCards = createWeaponCards();
		List<Displayable> roomCards = createRoomCards();

		game = new Game(numberOfPlayers, playerTokens, weaponTokens, cells, suspectCards, weaponCards, roomCards);
		
		// TODO startGame(); Go over documentation to find other methods to write
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

	private void printBanner()
	{
		// Banner courtesy of http://www.desmoulins.fr/index_us.php?pg=scripts!online!asciiart
		print(BANNER);

		// Give a bit of space below the banner for readability.
		println();
		println();
	}

	private void printByline()
	{
		println("A text based detective game -- now with advanced graphics!");
		println();
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
