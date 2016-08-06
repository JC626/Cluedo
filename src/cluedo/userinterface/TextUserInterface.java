package cluedo.userinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import cluedo.board.Board;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.exceptions.NoAvailableExitException;
import cluedo.game.Game;

import cluedo.model.Cell;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.model.Weapon;
import cluedo.model.cards.Card;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;
import cluedo.utility.Heading;
import cluedo.utility.Heading.Direction;

/**
 * A user interface, for human players, that consists of input and output from the command line.
 * 
 * Future support for colouring of characters, and box drawing characters for walls is expected.
 */
public class TextUserInterface
{
	private static final String BANNER = " ____    ___                       __            \n" + "/\\  _`\\ /\\_ \\                     /\\ \\           \n" + "\\ \\ \\/\\_\\//\\ \\    __  __     __   \\_\\ \\    ___   \n" + " \\ \\ \\/_/_\\ \\ \\  /\\ \\/\\ \\  /'__`\\ /'_` \\  / __`\\ \n" + "  \\ \\ \\_\\ \\\\_\\ \\_\\ \\ \\_\\ \\/\\  __//\\ \\_\\ \\/\\ \\_\\ \\\n" + "   \\ \\____//\\____\\\\ \\____/\\ \\____\\ \\___,_\\ \\____/\n" + "    \\/___/ \\/____/ \\/___/  \\/____/\\/__,_ /\\/___/ \n" + "                                                 \n";
	private static final String menuFormat = "    [%d] %s";
	private static final String userPrompt = "> ";
	private static final String shortcutDisplayCommand = "shortcuts";

	// Portions of cells. Dividing them up into nine parts allows for a lot of flexability. 
	private Character horizontalLine = '=';
	private Character verticalLine = '|';

	private Character topLeftCorner = '+';
	private Character topRightCorner = '+';

	private Character cellEmpty = '.';

	private Character bottomLeftCorner = '+';
	private Character bottomRightCorner = '+';

	private final GameOptions gameOptions = new GameOptions(); // User options for this game. e.g. printing the board every turn, or verbose errors.
	
	private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

	// The size of the buffer is the number of Cells, multiplied by the size of each cell (3 by 3).
	private final Character[][] drawingBuffer = new Character[3 * Board.WIDTH][3 * Board.HEIGHT]; // The drawing buffer, which will be modified in layers based in game state.

	private Game game;

	/**
	 * Start a new game.
	 * Asks for the number of players, and does other setup for a new text game.
	 * Calls runGame() to begin the game once setup has finished.
	 */
	private void newGame()
	{
		int numberOfPlayers = promptMenuNumber("Select players: ", Game.MIN_HUMAN_PLAYERS, Game.MAX_PLAYERS, " players");

		List<Piece> weaponTokens = createEmptyPiece(Game.NUM_WEAPONS);
		List<Piece> playerTokens = createEmptyPiece(Game.MAX_PLAYERS);

		game = new Game(numberOfPlayers);

		try
		{
			runGame();
		}
		catch (IllegalMethodCallException e)
		{
			// If we make an illegal method call the either the game is over, or
			// there's a bug in the UI. In either case, end the game rather than crashing.
		}
		
		// Output the solution for this game.
		println("Game over.\nThe solution was:");
		
		for (Card c : game.getAnswer())
		{
			println(c.getName());
		}
	}

	/**
	 * The main loop of the UI, provides interaction between the players and their characters.
	 */
	private void runGame()
	{
		Map<Integer, Runnable> actions = new HashMap<Integer, Runnable>(); // Actions available to the user.
		int nextAction = 0; // The menu option corresponding to each action.

		// Give the user some information about this game.
		printCanonBackground();
		printExtraCards();
		
		continuePromptMenu(); // Don't interrupt their reading, just in case the board prints out.

		if (gameOptions.printBoardAtStartTurn)
		{
			makeAndDisplayBoard();
		}

		while (!game.isGameOver())
		{
			// The state for this user may have changed, so we need to reevaluate our options
			nextAction = 0;
			actions.clear();
			actions.put(nextAction, () -> { /* Do nothing, the player is either eliminated or the game is over */ }); // 0 is an accusation that has been fulfilled.
			nextAction++;

			int userSelection;
			List<String> options = new ArrayList<String>();
			List<String> regex = new ArrayList<String>();

			if (game.isInRoom())
			{
				// If the current player is in a room, they can make a suggestion or leave.

				if (game.canMakeSuggestion())
				{
					options.add("Make a suggestion");
					regex.add("suggest|"); // The default action is to suggest

					actions.put(nextAction, () -> promptMakeSuggestion());
					nextAction++;
				}
				else if (game.canMove())
				{
					options.add("Exit the " + game.getRoom(game.getPosition(game.getCurrentPlayer())).getName());
					regex.add("exit|leave|go");

					actions.put(nextAction, () -> promptExitRoom());
					nextAction++;
				}
			}
			else if (game.canMove()) // We can only move if we're not in a room
			{
				options.add("Move");
				regex.add("m|"); // Make movement the default

				actions.put(nextAction, () ->
				{
					try
					{
						promptMovement();
					}
					catch (InvalidMoveException e)
					{
						println("You must be mistaken, you can't have walked there!");
					}
				});
				nextAction++;
			}

			if (game.getRemainingMoves() <= 0) // If we have no remaining moves, we may end our turn.
			{
				options.add("End turn");
				regex.add("done|next|wait");

				actions.put(nextAction, () ->
				{
					continuePromptMenu();
					game.nextTurn();
					if (gameOptions.printBoardAtStartTurn)
					{
						makeAndDisplayBoard();
					}
				});
				nextAction++;
			}

			printRemainingMoves();

			// If remaining moves == 0 we're not necesarily stuck, so that check is needed.
			if (!game.canMove() && game.getRemainingMoves() != 0)
			{
				println("You recall being stuck!");
			}
			
			userSelection = executeDefaultMenu(game.getCurrentPlayer().getName(), options, regex);

			actions.get(userSelection).run();
		}
	}

	/*
	 * PRINTING
	 */
	
	/**
	 * Collects and prints the extra cards from the distribution of cards between players.
	 * If there are no extra cards the method returns without printing anything.
	 */
	private void printExtraCards()
	{
		List<Card> extras = game.getExtraCards(); // Extra cards, may be empty.
		List<String> extraNames = new ArrayList<String>();

		if (!extras.isEmpty()) // We have cards
		{
			for (Card c : extras)
			{
				extraNames.add(c.getName());
			}

			// Inform the user of the cards.
			println("Everyone, we've found some evidence. We can conclusively say that the following were not involved in the murder:\n");

			printMenu(extraNames);
		} // Else: exit, there were no extras.
	}

	/**
	 * Print the remaining moves in a player friendly manner, adding s for != 1 moves remaining,
	 * and deals with 0 moves separately. 
	 */
	private void printRemainingMoves()
	{

		int remaining = game.getRemainingMoves();

		if (remaining > 0)
		{
			String remainingMoves = "You can remember taking " + remaining + " more step";

			if (remaining != 1)
			{
				remainingMoves = remainingMoves + "s";
			}
			remainingMoves = remainingMoves + ".";

			println(remainingMoves);
		}
		else // 0 moves, so inform the user is a friendly manner. i.e. not "You can remember taking 0 more steps".
		{
			println("You don't remember going any further...");
		}
	}

	/**
	 * Displays the board to the output defined by print(), based on the drawingBuffer
	 */
	private void printBoard()
	{
		for (int y = 0; y < drawingBuffer[0].length; y++)
		{
			for (int x = 0; x < drawingBuffer.length; x++)
			{
				print(drawingBuffer[x][y].toString());
			}
			println();
		}
	}

	/**
	 * There shouldn't be any reason to call this method.
	 * Print out the shortcuts (as humanReadableRegex) available for this set of options.
	 * Called by executeMenu() when shortcutDisplayCommand is entered.
	 * @param menuOptions The options that need human readable regex added to them.
	 * @param regexMatches The regex to add. Must be of the same or greater length as menuOptions.
	 */
	private void printShortcuts(List<String> menuOptions, List<String> regexMatches)
	{
		printMenu(getShortcuts(menuOptions, regexMatches));
	}

	/**
	 * Output the menu with numbers starting from 1 going up to the length of menuOptions.
	 * @param menuOptions A list containing the options that are to be printed.
	 */
	private void printMenu(List<String> menuOptions)
	{
		for (int i = 0; i < menuOptions.size(); i++)
		{
			printMenuItem(menuFormat, i + 1, menuOptions.get(i));
		}
	}

	/**
	 * Wrapper for String.format() using println()
	 * @param format A format conforming to String.format() specification.
	 * @param item A number of items conforming to String.format() specification.
	 */
	private void printMenuItem(String format, Object... item)
	{
		println(String.format(format, item));
	}
	
	/**
	 * Wrapper for System.out.print.
	 * Allows for easy switching of output method.
	 * @param s The string to be printed.
	 */
	private void print(String s)
	{
		System.out.print(s);
	}

	/**
	 * Wrapper for print.
	 * @param s Same as print.
	 */
	private void println(String s)
	{
		print(s + "\n");
	}

	/**
	 * Print an empty new line.
	 */
	private void println()
	{
		println("");
	}
	
	/**
	 * Print the banner and byline.
	 */
	private void printGreeting()
	{
		// Banner courtesy of http://www.desmoulins.fr/index_us.php?pg=scripts!online!asciiart
		print(BANNER);
		println("A text based detective game -- now with advanced graphics!");
		printBlankLines(3); // Give some space for readability.
	}

	/**
	 * Output the instructions.
	 * More options, such as gameRules should be added here.
	 */
	private void printInstructions()
	{
		printControls();
	}

	/**
	 * Output regular expressions in a reasonable manner.
	 * The regex is wrapped in {} to distinguish it from other text.
	 * @param regex The regex that is to be printed.
	 * @return The reasonable rendering of regex.
	 */
	private String humanReadableFromRegex(String regex)
	{
		return " {" + regex + "}";
	}

	/**
	 * Output the canon background to the game to the user.
	 * This increases immersion, and hopefully user experience.
	 */
	private void printCanonBackground()
	{
		println("It's the morning of Sunday June 6th, 1926; and you're being investigated for murder.\n");
		println("You, along with five other guests, at John Boddy's mansion on Rainbow Road spent the night getting to know many of John's friends.");
		println("John was killed shortly after 8:15pm the previous night, his body found at the bottom of the cellar stairs - although you suspect it had been moved there.");
		println("Although you don't remember much from the night before, you decide to make an effort to retrace your steps...\n");
	}

	/**
	 * Output blank lines.
	 * Saves writing println(); n times.
	 * @param lines The number of new lines to print.
	 */
	private void printBlankLines(int lines)
	{
		for (int i = 0; i < lines; i++)
		{
			println();
		}
	}

	
	/*
	 * MOVEMENT
	 */
	
	/**
	 * Asks the user for the direction they want to go in.
	 * @return A set of at least one of nsew in any order.
	 */
	private String getMovement()
	{
		String movement = null; // This will have a valid value before the return statement.
		boolean validMovement = false;

		println("Where did you go to?");

		while (!validMovement)
		{
			movement = getUserInput(Pattern.compile("(n|s|e|w)+".toLowerCase()));

			if (movement.length() <= game.getRemainingMoves())
			{
				validMovement = true;
			}
			else
			{
				// Inform the user that their input was too long, and by how much.
				println(String.format("You don't have enough moves (%d remaining, attempted to move %d)", game.getRemainingMoves(), movement.length()));
			}
		}

		return movement;
	}
	
	/**
	 * A tutorial for playing the game.
	 * Any additional controls should include examples.
	 */
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
		printMenuItem(menuFormat, 4, "Options" + humanReadableFromRegex("o|setting(s?)"));
		println(userPrompt + "settings");
		println();

		println("As a final tip, when you're in a hallway the default action is to move so you can press enter instead of typing 'move' or 'm'.");

		// Movement example
		println();
		printMenuItem(menuFormat, 4, "Move" + humanReadableFromRegex("m|move"));
		println(userPrompt + "[RET]");
		println("Where did you go to?");
		println(userPrompt + "n");
		
		println("This will move you one step to the north.");
		println("You can also string together multiple moves at once!");

		// Movement example
		println();
		printMenuItem(menuFormat, 4, "Move" + humanReadableFromRegex("m|move"));
		println(userPrompt + "[RET]");
		println("Where did you go to?");
		println(userPrompt + "nnneeews");
		println();

		println("That will move your character north three steps, east three steps, then west and finally south.");
		println("You'll be warned if you don't have enough remaining moves or you've entered an invalid direction.");
		println("Keep in mind, if your path leads you into a wall your movement will stop and you'll be asked for a different set of moves having already made the moves to get to this point.");
		println();
		println("Try using shortcuts here! You'll get the default (name of the menu item), and of course you can press 1, but here you can also press enter.");
		
		continuePromptMenu();
	}
	
	/**
	 * Create and make moves from a given string.
	 * @param movement The string containing movement information.
	 * Must contain at least one of nsew in any order.
	 */
	private void makeMoves(String movement)
	{
		List<Direction> moves = Heading.convertStringToDirection(movement);
		
		for (Direction d : moves)
		{
			if (game.canMove()) // Don't make the move if it's illegal.
			{
				try
				{
					game.move(d);
				}
				catch (InvalidMoveException e)
				{
					// Never occurs, as the move is valid (canMove() is true)
				}
			}
		}
		
		try
		{
			println(String.format("%s, you are now in the %s", game.getCurrentPlayer().getName(), game.getCurrentRoom().getName())); 
		}
		catch (IllegalMethodCallException e) 
		{
			// User is not in a room, so do nothing
		}
		
	}

	
	/*
	 * PROMPTS
	 */
	
	/**
	 * Get user input until it matches a given regex.
	 * @param matching The regular expression
	 * @return The user input matching the regex, in lower case.
	 */
	private String getUserInput(Pattern matching)
	{
		String userInput = null;

		do
		{
			userInput = getUserInput();
		} while (!matching.matcher(userInput).matches());

		return userInput.toLowerCase();
	}
	
	/**
	 * Prompts the user to exit the room, displaying the exits on the board to inform their decision.
	 * 
	 */
	private void promptExitRoom()
	{
		if (game.canMove())
		{
			try
			{
				int userSelection;

				List<Cell> exits = game.getAvailableExits();
				List<String> options = new ArrayList<String>();

				for (int i = 0; i < exits.size(); i++)
				{
					// There are no more than 4 exits for any room, so this is legal:
					String exitNum = Integer.toString(i + 1);
					char num = exitNum.toCharArray()[0];

					options.add(exitNum);

					// Add the numbered exit to the cell on the drawingBuffer.
					addCentreDrawingBuffer(exits.get(i), num);
				}

				// Display our work to the user.
				printBoard();

				userSelection = executeMenu("Select an exit", options, emptyRegex()) - 1; // -1 because lists are indexed from 0 and menus are displayed 1 .. n.

				game.takeExit(exits.get(userSelection));

				printBlankLines(5);

				makeAndDisplayBoard(); // Reset the board (removing our numbers), and show the user their new position. 
			}
			catch (InvalidMoveException | NoAvailableExitException e)
			{
				// Not possible, because we've determined that there is at least 1 path.
			}
		}
	}
	
	/**
	 * Print out a menu, and return the index + 1 of the option selected from menuOptions.
	 * We return index + 1 because that's the option the user is shown, and is a clearer way of thinking about
	 * the menus (case 1, case 2, case 3... case n).
	 * 
	 * This means that to access a point in 0 based data structures, you need to subtract one:
	 * e.g. options.get(executeMenu(title, options, regex) - 1);
	 * will return the option that the user selected.
	 * 
	 * @param menuTitle The title to display above the menu, usually a question.
	 * @param menuOptions The ordered list of options to display to the user.
	 * @param regexMatches An optional list of regular expressions that match with each option given.
	 * That is, the regular expression will be accepted as selecting that menu option.
	 * Every option's text is added automatically, as is their position.
	 * Duplicate regex will result in the first option being selected.
	 * @return index + 1 of the option selected from menuOptions.
	 */
	private int executeMenu(String menuTitle, List<String> menuOptions, List<String> regexMatches)
	{
		final int userSelectionSentinel = -1; // We'll loop until the user enters something valid, at which point we change this value.
		int userSelection = userSelectionSentinel;

		updateMenuOptions(menuOptions, regexMatches);

		// Print out the options available to the user
		println(menuTitle);
		println();
		printMenu(menuOptions);

		// Get the user input, and loop until we have something valid.
		do
		{
			String answer = getUserInput();

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
						// We add 1 here to match the shown/accepted number instead of the index because we usually deal with the user selection in a switch.
						// It's more natural to say case 1, case 2, .. case n because that is what the user sees: option 1, option 2, .. option n.
						userSelection = i + 1;
						break;
					}
				}
			}
			
		} while (userSelection == userSelectionSentinel);

		return userSelection;
	}

	/**
	 * A user friendly prompt for another attempt at input.
	 * @param e The exception thrown.
	 */
	private void handleIOException(Exception e)
	{
		if (gameOptions.verboseErrors)
		{
			e.printStackTrace();
			println();
		}
		
		println("Sorry, I couldn't hear you. Could you please repeat that?");
	}
	
	/**
	 * Generate the shortcuts (as humanReadableRegex) available for this set of options.
	 * @param menuOptions The options that need human readable regex added to them.
	 * @param regexMatches The regex to add. Must be of the same or greater length as menuOptions.
	 * @return The list of options with human readable regex added.
	 */
	private List<String> getShortcuts(List<String> menuOptions, List<String> regexMatches)
	{
		// We make a new list, so we don't change either of the parameters
		// If we were to change (e.g. the menuOptions) then if this method is called multiple times
		// the list of shortcuts would grow - we don't want that.
		List<String> menuWithShortcuts = new ArrayList<String>();

		for (int i = 0; i < menuOptions.size(); i++)
		{
			menuWithShortcuts.add(menuOptions.get(i) + humanReadableFromRegex(regexMatches.get(i)));
		}
		
		return menuWithShortcuts;
	}

	/**
	 * Change the regex to include the text from options.
	 * Modifies regex by reference.
	 * @param options The options for the menu that is displayed to the user.
	 * @param regex The regular expressions that correspond to each menu item. May be shorter than options, including empty. 
	 */
	private void updateMenuOptions(List<String> options, List<String> regex)
	{
		for (int i = 0; i < options.size(); i++)
		{
			String regexOption = options.get(i).toLowerCase(); // The menu text is a valid option 

			if (i < regex.size()) // There is a regex for this option. Note that regex may be shorter than options, including empty. 
			{
				String oldRegex = regex.get(i);
				regex.remove(i);
				regexOption = oldRegex + "|" + regexOption;
			}

			regex.add(i, regexOption);
		}
	}
	
	/**
	 * A wrapper for makeMenu, providing some default options that users
	 * may want to use on every turn.
	 * Provides a default prompt including playerName.
	 * @param playerName Used in the default prompt.
	 * @param menuOptions Same as executeMenu.
	 * @param regexMatches Same as executeMenu.
	 * @return The index + 1 of the option select in menuOptions.
	 * Reasons are explained in executeMenu.
	 * May return 0, which indicates an accusation has been made and processed.
	 */
	private int executeDefaultMenu(String playerName, List<String> menuOptions, List<String> regexOptions)
	{
		String menuTitle = String.format("%s, what did you do next?", playerName);

		int userSelection = 0; // The compiler claims that userSelection may not have been initialised, we know however that it will be.
		boolean selectedCallerOption = false;

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
					printRemainingMoves();
					break;
				case 2:
					makeAndDisplayBoard();
					break;
				case 3:
					promptReviewEvidence();
					break;
				case 4:
					if (promptMakeAccusation())
					{
						// The user made an accusation, either the game is over or they're out.
						// Regardless, their turn is over.
					}
					else
					{
						break;
					}
				default: // One of the caller's options was selected, pass it back to them.
					selectedCallerOption = true;
					userSelection = userSelection - 4; // Give the caller the index from what they gave. 4 is the number of items we have added.
			}
		}

		return userSelection;
	}
	
	/**
	 * Prompt the user for a suggestion, and modify the game state based on their response.
	 * The state is not changed if the user declines to make a suggestion.
	 */
	private void promptMakeSuggestion()
	{
		SuspectCard murderer = (SuspectCard) promptCard("I suggest the crime was committed in the " + game.getCurrentRoom().getName() + " by ...", game.getSuspectCards());
		WeaponCard murderWeapon = (WeaponCard) promptCard("with the ...", game.getWeaponCards());

		String verificationQuestion = String.format("You're suggesting %s committed the crime in the %s with the %s?", murderer.getName(), game.getCurrentRoom().getName(), murderWeapon.getName());

		if (!promptMenuBoolean(verificationQuestion, "That's correct", "Actually, I'll take another look at the evidence"))
		{
			return; // The user decided not to go through with the suggestion.
		}

		Map<Player, Set<Card>> disproved = game.makeSuggestion(murderWeapon, murderer);

		if (!disproved.isEmpty())
		{
			Map<Player, Card> disprover = new HashMap<Player, Card>();
			// Given that disproved is not empty, these two variables will be initialised.
			Player disprovingPlayer = null;
			Set<Card> disprovingHandSet = null;

			List<Card> disprovingHandList = new ArrayList<Card>();

			for (Player p : disproved.keySet())
			{
				disprovingPlayer = p;
				disprovingHandSet = disproved.get(p);
				break;
			}

			assert disprovingHandSet != null;
			assert disprovingPlayer != null;

			for (Card c : disprovingHandSet)
			{
				disprovingHandList.add(c);
			}

			
			println(String.format("%s, you can disprove the suggestion...", disprovingPlayer.getName()));
			continuePromptMenu();

			String question = String.format("%s choose a card to reveal to %s:", disprovingPlayer.getName(), game.getCurrentPlayer().getName());
			
			
			
			disprover.put(disprovingPlayer, promptCard(question, disprovingHandList));
			game.removeCard(disprover);
		}
		else
		{
			println("No one could disprove your suggestion... Maybe you're onto something here.");
		}
		continuePromptMenu(); // So the viewing player doesn't see the rest of the hand.
	}

	/**
	 * Prompt the user for a suggestion, and modify the game state based on their response.
	 * Note that, if the player goes through with the accusation, true is returned whether or not the accusation was correct.
	 * @return True if the player went through with the accusation, false otherwise.
	 */
	private boolean promptMakeAccusation()
	{
		Player accusingPlayer = promptAccusingPlayer();

		// Note that the call order here is important, the prompts depend on the order:
		// Suspect, Room, Weapon
		SuspectCard murderer = (SuspectCard) promptCard("I accuse ...", game.getSuspectCards());
		RoomCard murderRoom = (RoomCard) promptCard("of committing the crime in the ...", game.getRoomCards());
		WeaponCard murderWeapon = (WeaponCard) promptCard("with the ...", game.getWeaponCards());

		String verificationQuestion = String.format("Are you sure you want to accuse %s of killing John Boddy in the %s with the %s?", murderer.getName(), murderRoom.getName(), murderWeapon.getName());

		if (!promptMenuBoolean(verificationQuestion, "I'm sure", "On second thought..."))
		{
			return false; // The user decided not to go through with the accusation.
		}

		boolean accusationCorrect = game.makeAccusation(accusingPlayer, murderWeapon, murderRoom, murderer);

		if (accusationCorrect)
		{
			println("Congratulations on finding the murderer, " + accusingPlayer.getName() + "!");
		}
		else
		{
			println(accusingPlayer.getName() + ", you've made a very serious accusation and we have evidence to the contrary. You will no longer be able to participate in this investigation.");
		}
		
		continuePromptMenu(); // Let the user reflect on the outcome of the accusation.

		return true; // Went through with the accusation.
	}

	/**
	 * Wrapper for executeMenu.
	 * @param question The question to be asked to the user.
	 * @param cards The cards that they may select from.
	 * @return The card that was selected.
	 */
	private Card promptCard(String question, List<Card> cards)
	{
		int userSelection;
		List<String> options = new ArrayList<String>();

		for (Card c : cards)
		{
			options.add(c.getName());
		}

		userSelection = executeMenu(question, options, emptyRegex());

		return cards.get(userSelection - 1);
	}

	/**
	 * Give back an empty regex list.
	 * Should be used in place of new ArrayList<String>() when calling executeMenu because this is clearer.
	 * @return An empty list of strings.
	 */
	private List<String> emptyRegex()
	{
		return new ArrayList<String>();
	}

	/**
	 * Wrapper for executeMenu
	 * @param question The question to about the active players.
	 * @return The player that was selected.
	 */
	private Player promptActivePlayer(String question)
	{
		int userSelection;
		List<String> options = new ArrayList<String>();

		for (Player p : game.getActivePlayers())
		{
			options.add(p.getName());
		}

		userSelection = executeMenu(question, options, emptyRegex());

		return game.getActivePlayers().get(userSelection - 1);
	}

	/**
	 * Wrapper for promptActivePlayer used to find who is making the accusatin.
	 * @return Same as promptActivePlayer.
	 */
	private Player promptAccusingPlayer()
	{
		String question = "Who is making the accusation?";
		return promptActivePlayer(question);
	}

	/**
	 * Prints out the card that the player has yet to remove from suspicion.
	 */
	private void promptReviewEvidence()
	{
		println("You haven't found evidence that removes the following from suspicion...\n");

		List<String> caseFile = new ArrayList<String>();

		for (Card suspect : game.getSuspectCards())
		{
			if (game.getPlayerSuspectCards().contains(suspect))
			{
				caseFile.add(suspect.getName());
			}
		}

		for (Card room : game.getRoomCards())
		{
			if (game.getPlayerRoomCards().contains(room))
			{
				caseFile.add(room.getName());
			}
		}

		for (Card weapon : game.getWeaponCards())
		{
			if (game.getPlayerWeaponCards().contains(weapon))
			{
				caseFile.add(weapon.getName());
			}
		}

		printMenu(caseFile);
		printBlankLines(2); // Give a bit of room before the turn menu prompt.
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
	
	/**
	 * Loops until a valid string is entered by the user.
	 * In case of IOException, calls handleIOException.
	 * @return The user input, converted to lower case.
	 */
	private String getUserInput()
	{
		String movement = null;

		do
		{
			try
			{
				print(userPrompt);
				movement = input.readLine();
			}
			catch (IOException e)
			{
				handleIOException(e);
			}
		} while (movement == null);

		return movement.toLowerCase();

	}
	
	/**
	 * Gives the prompt for movement, and makes the given moves.
	 * @throws InvalidMoveException If any given moves are invalid.
	 */
	private void promptMovement() throws InvalidMoveException
	{
		makeMoves(getMovement());
	}
	
	/**
	 * The main menu, as presented to the user.
	 * Branches off into instructions, new game, options, and quiting. 
	 */
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
					// Done, exit; we don't break here to execute the default.
				default:
					endGame = true;
			}

			printBlankLines(7); // Give some space between the main menu and the game start.
		}
	}
	
	/**
	 * Wait for user input.
	 * Wrapper for executeMenu.
	 */
	private void continuePromptMenu()
	{
		List<String> menu = new ArrayList<String>();
		List<String> regex = new ArrayList<String>();

		menu.add("Press enter to continue");
		regex.add("");
		executeMenu("", menu, regex);
		printBlankLines(15); // So the previous text isn't available
	}

	
	/*
	 * OPTIONS
	 */
	
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

	
	/*
	 * CREATION
	 */
	
	/**
	 * Used for creating players and weapon tokens.
	 * @param count The number of tokens needed.
	 * @return A list of Pieces with empty display methods.
	 */
	private List<Piece> createEmptyPiece(int count)
	{
		List<Piece> pieces = new ArrayList<Piece>();

		for (int piece = 0; piece < count; piece++)
		{
			Piece p = new Piece()
			{ // We can't use lambdas because they have the same reference.
				public void display()
				{

				}
			};
			pieces.add(p);
		}

		return pieces;
	}

	/*
	 * BOARD GENERATION
	 */
	
	/**
	 * Generates, and then displays the board.
	 * If you're looking to draw the board and don't have
	 * any special requirements (like placing a particular string inside a specific cell)
	 * then this is the method to call.
	 */
	private void makeAndDisplayBoard()
	{
		generateBoard();
		printBoard();
	}
	
	/**
	 * Adds the board, with Cells, Players, and Weapons.
	 * There should be no reason to call this method directly.
	 * 
	 * If you're looking to print the board normally use makeAndDisplayBoard().
	 * If you're looking to add something special to a Cell use 
	 * addCentreDrawingBuffer(myCell, myString) and then printBoard().
	 */
	private void generateBoard()
	{
		Cell[][] board = game.getCells();

		for (int x = 0; x < board.length; x++)
		{
			for (int y = 0; y < board[x].length; y++)
			{
				addCellLayerDrawingBuffer(board[x][y]);
			}
		}
		
		addPlayerLayerDrawingBuffer();
		addWeaponLayerDrawingBuffer();
	}

	/**
	 * Add things to the centre of cells in the drawing buffer.
	 * Replaces what was there previously. It is your responsibility to ensure the space is empty.
	 *
	 * @param cell The cell to add displayable to.
	 * @param displayable The character(s) you want to display in the middle of the Cell.
	 * Cells use 1 cell per wall segment (3 wall segments making up a wall) so exactly 1 printable character
	 * is HIGHLY recommended to ensure consistent width of the board.
	 */
	private void addCentreDrawingBuffer(Cell cell, Character displayable)
	{
		// Cells are 3x3 and we need +1 to get to the middle of the Cell 
		drawingBuffer[(3 * cell.getX()) + 1][(3 * cell.getY()) + 1] = displayable;
	}

	/**
	 * Add players to the drawing buffer.
	 * There should be no reason to call this method on it's own.
	 * This method is called when generating the board.
	 * 
	 * Replaces what was there previously. It is your responsibility to ensure the space is empty.
	 */
	private void addPlayerLayerDrawingBuffer()
	{
		List<Player> players = game.getAllPlayers();

		for (Player p : players)
		{
			addCentreDrawingBuffer(game.getPosition(p), getPlayerDisplayable(p));
		}
	}

	/**
	 * Add weapons to the drawing buffer.
	 * There should be no reason to call this method on it's own.
	 * This method is called when generating the board.
	 * 
	 * Replaces what was there previously. It is your responsibility to ensure the space is empty.
	 */
	private void addWeaponLayerDrawingBuffer()
	{
		List<Weapon> weapons = game.getWeapons();

		for (Weapon w : weapons)
		{
			addCentreDrawingBuffer(game.getPosition(w), getWeaponDisplayable(w));
		}
	}

	/**
	 * As GameBuilder is non public and doesn't provide
	 * the names in a public and fixed manner we need to
	 * hard code the names here. If the names or order changes
	 * then this method will need to change too.
	 * @param w The weapon that you are interested in getting the representation of.
	 * @return The string that represents w. An empty string is returned if the weapon is not recognised.
	 */
	private Character getWeaponDisplayable(Weapon w)
	{
		Character weaponDisplayable = ' ';

		switch (w.getName())
		{
			case "Dagger":
				weaponDisplayable = 'D';
				break;
			case "Candlestick":
				weaponDisplayable = 'c'; // Capital C is Colonel Mustard.
				break;
			case "Revolver":
				weaponDisplayable = 'R';
				break;
			case "Rope":
				weaponDisplayable = 'r';
				break;
			case "Lead Pipe":
				weaponDisplayable = 'L';
				break;
			case "Spanner":
				weaponDisplayable = 's'; // Capital S is Miss Scarlett.
				break;
			default:
				// Weapon not recognised, don't draw them.
		}

		return weaponDisplayable;
	}

	/**
	 * As GameBuilder is non public and doesn't provide
	 * the names in a public and fixed manner we need to
	 * hard code the names here. If the names or order changes
	 * then this method will need to change too.
	 * @param p The player whose character we need to represent.
	 * @return The string that represents p. An empty string is returned if the player is not recognised.
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
				playerDisplayable = 'P';
				break;
			case "Professor Plum":
				playerDisplayable = 'p'; // Capital P is Mrs. Peacock.
				break;
			default:
				// Player not recognised, don't draw them
		}

		return playerDisplayable;
	}

	/**
	 * Add Cells and their walls to the drawing buffer.
	 * There should be no reason to call this method on it's own.
	 * This method is called when generating the board.
	 * 
	 * Replaces what was there previously. It is your responsibility to ensure the space is empty.
	 * @param cell The cell you want to draw. Will usually be called over all cells.
	 */
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

	/**
	 * Calculate the bottom right wall of the cell, given the relevant directions.
	 * @param east True if there is a wall to the east, false otherwise.
	 * @param south True if there is a wall to the south, false otherwise.
	 * @return The character at the bottom right of this cell.
	 */
	private Character cellBottomRight(boolean east, boolean south)
	{
		Character result = ' ';
		if (south)
		{
			if (east) // Corner piece
			{
				result = bottomRightCorner;
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

	/**
	 * Calculate the bottom centre wall of the cell, given the relevant directions.
	 * @param south True if there is a wall to the south, false otherwise.
	 * @return The character at the bottom of this cell.
	 */
	private Character cellBottomCentre(boolean south)
	{
		return (south) ? horizontalLine : ' ';
	}

	/**
	 * Calculate the bottom left wall of the cell, given the relevant directions.
	 * @param west True if there is a wall to the west, false otherwise.
	 * @param south True if there is a wall to the south, false otherwise.
	 * @return The character at the bottom left of this cell.
	 */
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

	/**
	 * Calculate the middle right wall of the cell, given the relevant directions.
	 * @param east True if there is a wall to the east, false otherwise.
	 * @return The character at the right of this cell.
	 */
	private Character cellMiddleRight(boolean east)
	{
		return (east) ? verticalLine : ' ';
	}

	/**
	 * The middle of a regular cell is always empty.
	 * To add something to the middle of a given cell use:
	 * 
	 * addPlayerLayerDrawingBuffer for adding all players.
	 * addWeaponLayerDrawingBuffer for adding all weapons.
	 * addCentreDrawingBuffer for adding arbitrary characters.
	 * 
	 * @return cellEmpty.
	 */
	private Character cellMiddleCentre()
	{
		return cellEmpty; // This will be overridden if there's a weapon or player there.
	}

	/**
	 * Calculate the middle left wall of the cell, given the relevant directions.
	 * @param west True if there is a wall to the west, false otherwise.
	 * @return The character at the left of this cell.
	 */
	private Character cellMiddleLeft(boolean west)
	{
		return (west) ? verticalLine : ' ';
	}

	/**
	 * Calculate the top right wall of the cell, given the relevant directions.
	 * @param east True if there is a wall to the east, false otherwise.
	 * @param south True if there is a wall to the south, false otherwise.
	 * @return The character at the top right of this cell.
	 */
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

	/**
	 * Calculate the top centre wall of the cell, given the relevant directions.
	 * @param north True if there is a wall to the north, false otherwise.
	 * @return The character at the top of this cell.
	 */
	private Character cellTopCentre(boolean north)
	{
		return (north) ? horizontalLine : ' ';
	}

	/**
	 * Calculate the top left wall of the cell, given the relevant directions.
	 * @param west True if there is a wall to the west, false otherwise.
	 * @param north True if there is a wall to the north, false otherwise.
	 * @return The character at the top left of this cell.
	 */
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

	
	/*
	 * MAIN
	 */
	
	public static void main(String[] args)
	{
		TextUserInterface t = new TextUserInterface();
		t.mainMenu();
	}
}