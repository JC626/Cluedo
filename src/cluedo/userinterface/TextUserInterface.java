package cluedo.userinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import cluedo.board.Board;
import cluedo.exceptions.InvalidMoveException;
import cluedo.game.Game;

import cluedo.model.Cell;
import cluedo.model.Displayable;
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

	private static final Character horizontalLine = '=';//'\u2550';
	private static final Character verticalLine = '|';//'\u2551';

	private static final Character topLeftCorner = '+';//'\u2554';
	private static final Character topRightCorner = '+';//'\u2557';

	private static final Character bottomLeftCorner = '+';//'\u255A';
	private static final Character bottomRightCorner = '+';//'\u255D';

	private final GameOptions gameOptions = new GameOptions();
	private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

	// The size of the buffer is the number of Cells, multiplied by the size of each cell (3 by 3).
	private final Character[][] drawingBuffer = new Character[3 * Board.WIDTH][3 * Board.HEIGHT]; // The drawing buffer, which will be modified in layers based in game state.

	private Game game;

	private void newGame()
	{
		int numberOfPlayers = promptMenuNumber("Select players: ", Game.MIN_HUMAN_PLAYERS, Game.MAX_PLAYERS, " players");

		List<Piece> weaponTokens = createEmptyPiece(Game.NUM_WEAPONS);
		List<Piece> playerTokens = createEmptyPiece(Game.MAX_PLAYERS);

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
			int userSelection;


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

				options.add("Exit the " + game.getRoom(game.getPosition(game.getCurrentPlayer().getPiece())).getName());
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
			else if (game.getRemainingMoves() > 0)
			{
				options.add("Move");
				regex.add("m|"); // Make movement the default
			}

			if (game.getRemainingMoves() <= 0)
			{
				options.add("End turn");
				regex.add("done|next");
			}

			if (gameOptions.printBoardAtStartTurn)
			{
				printBoard();
			}

			userSelection = executeDefaultMenu(game.getCurrentPlayer().getName(), options, regex);

			if (userSelection == 0) // Incorrect accusation
			{
				// Carry on, this player has been eliminated.
			}
			else
			{

				if (game.isInRoom())
				{
					if (userSelection == 1) // Exit room
					{
						promptExitRoom();
					}
					else if (game.canMakeSuggestion()) // 2 == make suggestion, else 2 == end turn
					{
						promptMakeSuggestion();
					}
				}
				else if (game.getRemainingMoves() > 0)
				{
					try
					{
						printRemainingMoves();
						promptMovement();
					}
					catch (InvalidMoveException e)
					{
						println("You can't move in that direction");
					}
				}
				else
				{
					game.nextTurn();
				}
			}

			continuePromptMenu();

		}
	}

	private void printRemainingMoves()
	{

		int remaining = game.getRemainingMoves();
		String remainingMoves = "You have " + remaining + " move";

		if (remaining != 1)
		{
			remainingMoves = remainingMoves + "s";
		}

		remainingMoves = remainingMoves + " remaining";
		println(remainingMoves);
	}

	private void promptMovement() throws InvalidMoveException
	{

		String movement;

		try
		{
			movement = getMovement();

			makeMoves(movement);
		}
		catch (IOException e)
		{
			handleIOException(e);
		}

	}

	private void makeMoves(String movement) throws InvalidMoveException
	{
		List<Direction> moves = Heading.convertStringToDirection(movement);

		for (Direction d : moves)
		{
			game.move(d);
		}
	}

	private String getMovement() throws IOException
	{
		String movement = null; // Will always be set, if we reach the return statement.
		boolean validMovement = false;

		while (!validMovement)
		{
			print(userPrompt);
			movement = input.readLine().toLowerCase();

			while (!Pattern.matches("(n|s|e|w)+".toLowerCase(), movement))
			{
				movement = input.readLine().toLowerCase();
			}

			if (movement != null)
			{
				if (movement.length() <= game.getRemainingMoves())
				{
					validMovement = true;
				}
			}
		}

		return movement;
	}

	private void promptExitRoom()
	{
		// TODO Auto-generated method stub

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

	private void addPlayerLayerDrawingBuffer()
	{
		Cell[][] board = game.getCells();
		List<Player> players = game.getAllPlayers();

		for (int x = 0; x < board.length; x++)
		{
			for (int y = 0; y < board[x].length; y++)
			{
				for (Player p : players)
				{
					Cell playerLocation = game.getPosition(p.getPiece());

					if (board[x][y].equals(playerLocation)) // Draw the Piece in this Cell
					{
						// Cells are 3x3 and we need +1 to get to the middle of the Cell 
						drawingBuffer[(3 * x) + 1][(3 * y) + 1] = getPlayerDisplayable(p);
					}
				}

			}
		}
	}

	private void addWeaponLayerDrawingBuffer()
	{
		Cell[][] board = game.getCells();
		List<Weapon> weapons = game.getWeapons();

		for (int x = 0; x < board.length; x++)
		{
			for (int y = 0; y < board[x].length; y++)
			{
				for (Weapon w : weapons)
				{
					Cell weaponLocation = game.getPosition(w.getPiece());

					if (board[x][y].equals(weaponLocation)) // Draw the Piece in this Cell
					{
						// Cells are 3x3 and we need +1 to get to the middle of the Cell 
						drawingBuffer[(3 * x) + 1][(3 * y) + 1] = getWeaponDisplayable(w);
					}
				}

			}
		}
	}

	private Character getWeaponDisplayable(Weapon w)
	{
		Character weaponDisplayable = ' ';

		switch (w.getName())
		{
			case "Dagger":
				weaponDisplayable = 'D';
				break;
			case "Candlestick":
				weaponDisplayable = 'c';
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
				weaponDisplayable = 's';
				break;
			default:
				// Player not recognised, don't draw them.
		}

		return weaponDisplayable;
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
				handleIOException(e);
			}
		} while (userSelection == userSelectionSentinel);

		return userSelection;
	}

	private void handleIOException(IOException e)
	{
		if (gameOptions.verboseErrors)
		{
			e.printStackTrace();
		}
		println();
		print("Sorry, I couldn't hear you. Could you please repeat that?");
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

	private void promptMakeSuggestion()
	{
		SuspectCard murderer = (SuspectCard) promptCard("I suggest the crime was committed in the " + game.getCurrentRoom().getName() + " by ...", game.getSuspectCards());
		WeaponCard murderWeapon = (WeaponCard) promptCard("with the ...", game.getWeaponCards());

		String verificationQuestion = String.format("You're suggesting %s committed the crime in the %s with the %s??", murderer.getName(), game.getCurrentRoom().getName(), murderWeapon.getName());

		if (!promptMenuBoolean(verificationQuestion, "That's correct", "Actually, I'll take another look at the evidence"))
		{
			return; // The user decided not to go through with the suggestion.
		}

		Map<Player, Set<Card>> disproved = game.makeSuggestion(murderWeapon, murderer);

		if (!disproved.isEmpty())
		{
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

			for (Card c : disprovingHandSet)
			{
				disprovingHandList.add(c);
			}

			String question = String.format("%s choose a card to reveal to %s:", disprovingPlayer.getName(), game.getCurrentPlayer().getName());
			promptCard(question, disprovingHandList);
		}
		else
		{
			println("No one could disprove your suggestion!");
		}
	}

	/**
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
			println(accusingPlayer.getName() + ", you've made a very serious accusation and are incorrect. You will no longer be able to participate in this investigation.");
		}

		return true;
	}

	private Card promptCard(String question, List<Card> cards)
	{
		int userSelection;
		List<String> options = new ArrayList<String>();

		for (Card c : cards)
		{
			options.add(c.getName());
		}

		userSelection = executeMenu(question, options, new ArrayList<String>());

		return cards.get(userSelection - 1);
	}

	private Player promptActivePlayer(String question)
	{
		int userSelection;
		List<String> options = new ArrayList<String>();

		for (Player p : game.getActivePlayers())
		{
			options.add(p.getName());
		}

		userSelection = executeMenu(question, options, new ArrayList<String>());

		return game.getActivePlayers().get(userSelection - 1);
	}

	private Player promptAccusingPlayer()
	{
		String question = "Who is making the accusation?";
		return promptActivePlayer(question);
	}

	private void promptReviewEvidence()
	{
		/*
		 * The case file is provided to us by Suspect, Room, and Weapon cards.
		 * We print out the cards that are in the global set but have not been
		 * marked off the player's case file. 
		 */
		//TODO

	}

	private List<Piece> createEmptyPiece(int count)
	{
		List<Piece> pieces = new ArrayList<Piece>();

		for (int piece = 0; piece < count; piece++)
		{
			Piece p = new Piece(){
				public void display(){

				}
			};
			pieces.add(p);
		}

		return pieces;
	}

	private List<Displayable> createRoomCards()
	{
		List<Displayable> roomCards = new ArrayList<Displayable>();

		for (int room = 0; room < Game.NUM_ROOMS; room++)
		{
			Displayable dis = new Displayable(){
				public void display(){

				}
			};
			roomCards.add(dis);
		}

		return roomCards;
	}

	private List<Displayable> createWeaponCards()
	{
		List<Displayable> weaponCards = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			Displayable dis = new Displayable(){
				public void display(){

				}
			};
			weaponCards.add(dis);
		}

		return weaponCards;
	}

	private List<Displayable> createSuspectCards()
	{
		List<Displayable> suspectCards = new ArrayList<Displayable>();

		for (int suspect = 0; suspect < Game.MAX_PLAYERS; suspect++)
		{
			Displayable dis = new Displayable(){
				public void display(){

				}
			};
			suspectCards.add(dis);
		}

		return suspectCards;
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

	public static void main(String[] args)
	{
		TextUserInterface t = new TextUserInterface();
		t.mainMenu();

		System.out.println("Thank you for playing!");
	}
}