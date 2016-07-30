package cluedo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import cluedo.board.Board;
import cluedo.exceptions.HasRemainingMovesException;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.exceptions.NoAvailableExitException;
import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.model.Room;
import cluedo.model.Weapon;
import cluedo.model.cards.Card;
import cluedo.model.cards.CaseFile;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;
import cluedo.utility.Heading.Direction;
import cluedo.utility.Turn;

//TODO Game description
/**
 * The Cluedo game.
 * This dictates the rules of Cluedo 
 * Players can move, enter and exit rooms,
 * make a suggestion and make an accusation.
 * Creates all objects used in the game
 *
 */
public class Game 
{
	// Constants
	public static final int MIN_HUMAN_PLAYERS = 3;
	public static final int MAX_HUMAN_PLAYERS = 6;
	public static final int MAX_PLAYERS = 6;
	public static final int NUM_WEAPONS = 6;
	public static final int NUM_ROOMS = 9;
	
	/**
	 * Each player's starting position according to the order
	 * specified in SUSPECT_NAMES
	 */
	private final int[] STARTINGPOSITION = new int[]{
			7, 24, 0, 17, 9, 0, 14, 0, 23, 6, 23, 19 };
	/**
	 * Number of moves the player can move 
	 */
	private int remainingMoves;
	/**
	 * The cells the current player moved to during their turn
	 */
	private Set<Cell> playerPath;
	private Player currentPlayer;
	/**
	 * One round of the Cluedo game. Contains all the active players
	 */
	private Turn<Player> turn;
	/**
	 * Iterator that contains all human players in the game (i.e. eliminated and
	 * active players)
	 */
	private Turn<Player> allHumanIterator;
	
	/**
	 * All the players in Cluedo (human and non-human)
	 */
	private final List<Player> allPlayers;
	/**
	 * All the human players in the game Does not include eliminated players
	 * from the game.
	 */
	private List<Player> activeHumanPlayers;
	/**
	 * Every human player's casefile.
	 * Includes all cards that may be part of the murder
	 */
	private Map<Player, CaseFile> playerToCasefile = new HashMap<Player,CaseFile>();
	/**
	 * The cards that each player has in their hand. Every player has different
	 * cards to each other
	 */
	private Map<Player, Set<Card>> playerHand = new HashMap<Player,Set<Card>>();
	/**
	 * These are the cards leftover after evenly distributing the cards to all
	 * the players. Does not contain the answer cards. Every player will be able
	 * to view these cards. This Set may be empty
	 */
	private List<Card> extraCards;
	/**
	 * Contains one of each type of card (SuspectCard, WeaponCard, RoomCard).
	 * These are the cards that the player needs to guess correctly to win the
	 * game
	 */
	private CaseFile answer;

	//Cards
	private final List<SuspectCard> suspectCards;
	private final List<WeaponCard> weaponCards;
	private final List<RoomCard> roomCards;

	//Rooms
	private final Map<Room,Set<Cell>> roomCells; 
	private final Map<Room,Set<Cell>> entranceCells;
	private final Map<Room,List<Cell>> exitCells;
	private final Map<Cell, Room> cellToRoom;
	/**
	 * This is when a player was transferred
	 * to another room during a suggestion.
	 * The player is allowed to make a suggestion
	 * in the room they were transferred to
	 */
	private Map<Player, Boolean> transferred = new HashMap<Player,Boolean>();
	private Map<Player, Room> playerToRoom = new HashMap<Player,Room>();
	/**
	 * The room the player is in. 
	 * This is used to ensure the player doesn't enter
	 * the room they exited on the same turn
	 */
	private Room lastRoom; 
	private final List<Weapon> weapons;
	private final List<Room> rooms;
	private final Board board;
	private boolean gameOver;

	public Game(int numPlayers, List<Piece> playerTokens, List<Piece> weaponTokens,
			List<Displayable> suspectCardFaces, List<Displayable> weaponCardFaces, List<Displayable> roomCardFaces) 
	{
		if (numPlayers < MIN_HUMAN_PLAYERS || numPlayers > MAX_HUMAN_PLAYERS) 
		{
			throw new IllegalArgumentException(
					"Must have between: " + MIN_HUMAN_PLAYERS + " and " + MAX_HUMAN_PLAYERS + " human players");
		}
		CellBuilder cellBuilder = new CellBuilder();
		board = new Board(cellBuilder.getCells());
		allPlayers = GameBuilder.createPlayers(playerTokens);
		activeHumanPlayers = GameBuilder.createHumanPlayers(numPlayers,allPlayers);
		turn = new Turn<Player>(activeHumanPlayers,activeHumanPlayers.size()-1); //Ensure turn starts on the first player
		allHumanIterator = new Turn<Player>(activeHumanPlayers);
		weapons = GameBuilder.createWeapons(weaponTokens);
		// Cards
		suspectCards = GameBuilder.createSuspectCards(suspectCardFaces);
		weaponCards = GameBuilder.createWeaponCards(weaponCardFaces);
		roomCards = GameBuilder.createRoomCards(roomCardFaces);
		answer = GameBuilder.createCaseFiles(suspectCards, weaponCards, roomCards,playerToCasefile,activeHumanPlayers);
		extraCards = distributeCards(suspectCards, weaponCards, roomCards);
		//Room
		RoomBuilder roomBuilder = new RoomBuilder(board.getCells());
		cellToRoom = roomBuilder.getCellToRoom();
		roomCells = roomBuilder.getRoomCells();
		entranceCells = roomBuilder.getEntranceCells();
		exitCells = roomBuilder.getExitCells();
		rooms = roomBuilder.getRooms();
		setStartingPosition();
	}
	
	/**
	 * Distribute the remaining cards (all the cards except the answer cards) to
	 * the players. The cards that each player has is removed from their
	 * CaseFile.
	 * 
	 * @param suspectCards
	 *            - all the suspect cards
	 * @param weaponCards
	 *            - all the weapon cards
	 * @param roomCards
	 *            - all the room cards
	 * @return The cards that were leftover after evenly distributing the cards.
	 */
	private List<Card> distributeCards(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards,
			List<RoomCard> roomCards) 
	{
		List<Card> extra = new ArrayList<Card>();
		List<Card> allCards = new ArrayList<Card>();
		allCards.addAll(suspectCards);
		allCards.addAll(weaponCards);
		allCards.addAll(roomCards);
		Collections.shuffle(allCards);
		int numPlayers = activeHumanPlayers.size();
		//Minus 3 from allCards size as want to remove the answer cards
		int numExtra = (allCards.size()- 3) % numPlayers;
		//Number of cards each player will get
		int numCards = (allCards.size() - numExtra - 3) / numPlayers; 
		int countCards = 0;
		Set<Card> cardsForPlayer = new HashSet<Card>();
		for (Card card : allCards) {
			// All cards evenly distributed, put the rest of the cards in extra
			if(answer.containsCard(card))
			{
				continue;
			}
			if (numPlayers == 0) 
			{
				extra.add(card);
				//Remove extra card from each player's casefile
				for(Player p: getActivePlayers())
				{
					 playerToCasefile.get(p).removeCard(card);
				}
				continue;
			}
			// Remove the card from one of the player's CaseFile
			Player player = activeHumanPlayers.get(numPlayers - 1);
			CaseFile caseFile = playerToCasefile.get(player);
			caseFile.removeCard(card);
			cardsForPlayer.add(card);
			countCards++;
			if (countCards == numCards) 
			{
				// Put the cards in for a human player
				playerHand.put(player, cardsForPlayer);
				// Go to the next player
				numPlayers--;
				cardsForPlayer = new HashSet<Card>();
				countCards = 0;
			}
		}
		return extra;
	}
	/**
	 * For the start of the game.
	 * Set the default starting positions of the players 
	 * and randomly allocate weapons to one room each
	 */
	private void setStartingPosition()
	{
		int playerCount = 0;
		for(int i = 0; i < STARTINGPOSITION.length;i+=2)
		{
			int x = STARTINGPOSITION[i];
			int y = STARTINGPOSITION[i+1];
			board.setPosition(allPlayers.get(playerCount).getPiece(), x, y);
			playerCount++;
		}
		List<Room> randRooms = new ArrayList<Room>(rooms);
		Collections.shuffle(randRooms);
		int i = 0;
		for(Room room : randRooms)
		{
			if(i >= NUM_WEAPONS)
			{
				break;
			}
			Piece piece = weapons.get(i).getPiece();
			this.putInRoom(piece, room);
			i++;
		}
	}

	/**
	 * Checks if the player can move by calling the move method in the Board
	 * class. Assigns new player’s moves using rollDice()
	 * 
	 * @param direction
	 *            - direction player is moving towards from their current cell
	 *            position
	 * @return The cell that the player has moved to
	 * @throws InvalidMoveException
	 *             If the current player has no more moves remaining
	 *             or reentered the room they exited in the same turn
	 *             or move on a cell they already passed on the same turn
	 * @throws IllegalMethodCallException
	 *             If the game is over
	 * @throws IllegalArgumentException
	 *             If the argument is null
	 */
	public Cell move(Direction direction) throws InvalidMoveException 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (direction == null) 
		{
			throw new IllegalArgumentException("Argument cannot be null");
		}
		if (remainingMoves <= 0) 
		{
			throw new InvalidMoveException("Cannot move as no moves left");
		}
		
		Cell newPos = board.move(currentPlayer.getPiece(), direction);
		
		if(lastRoom != null && lastRoom.equals(cellToRoom.get(newPos)))
		{
			throw new InvalidMoveException(currentPlayer.getName() + " cannot reenter the same room they exited");
		}
		if(playerPath.contains(newPos))
		{
			throw new InvalidMoveException("Cannot move to the same cell in the same turn");
		}
		if(cellToRoom.containsKey(newPos))
		{
			Room room = cellToRoom.get(newPos);
			//Reallocate the player to a cell in the room
			this.putInRoom(currentPlayer.getPiece(), room);
			playerToRoom.put(currentPlayer, room);
		}
		playerPath.add(newPos);
		remainingMoves--;
		return newPos;
	}
	
	/**
	 * Checks whether the player can actually make a suggestion
	 * The player must be in a room to make a suggestion and 
	 * can also make a suggestion if they 
	 * were transferred into that room
	 * @return whether the player can make a suggestion
	 */
	public boolean canMakeSuggestion()
	{
		if (gameOver) 
		{
			return false;
		}
		if (!isInRoom()) 
		{
			return false;
		}
		if (remainingMoves != 0) 
		{
			return false;
		}
		/*
		 * A player that has transferred (due to another player's suggestion)
		 * can make a suggestion
		 */
		if(!transferred.get(currentPlayer))
		{
			return false;
		}
		return true;
	}

	/**
	 * A suggestion is when a player suggests what the murder weapon, murderer
	 * and murder room is. This is so that the player can determine what cards
	 * were part of the murder by a process of elimination. The current player
	 * can make a suggestion when they are in a room. The room the player is in
	 * is part of the suggestion.
	 * 
	 * Players in a clockwise order to the current player must disprove the
	 * suggestion if they have at least one of the cards in their hand. The
	 * suggestion ends when one or none of the players can disprove the
	 * suggestion
	 * 
	 * The suspected murder weapon and suspect are transferred to the 
	 * current player's room
	 * 
	 * @param weaponCard
	 *            that is suggested is part of the murder (in the answer
	 *            CaseFile)
	 * @param suspectCard
	 *            that is suggested is part of the murder (in the answer
	 *            CaseFile)
	 * @return One player with the card or cards they have that can disprove the
	 *         suggestion
	 * @throws IllegalMethodCallException
	 *             If the player is not in the room or the game is over
	 * @throws IllegalArgumentException
	 *             If the arguments are null
	 */
	public Map<Player, Set<Card>> makeSuggestion(WeaponCard weaponCard, SuspectCard suspectCard) 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (weaponCard == null || suspectCard == null) 
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if (!isInRoom()) 
		{
			throw new IllegalMethodCallException(
					currentPlayer.getName() + " cannot make a suggestion as they are not in a room");
		}
		// Get the roomCard for the room the current player is in
		RoomCard roomCard = null;
		String roomName = getCurrentRoom().getName();
		for (RoomCard card : roomCards) 
		{
			if (card.getName().equals(roomName)) 
			{
				roomCard = card;
				break;
			}
		}
		//Transfer the suspected murder weapon and suspect to the current player's room
		for(Player p : allPlayers) 
		{
			if(p.getName().equals(suspectCard.getName())) 
			{ //Put in the room
				this.putInRoom(p.getPiece(),getCurrentRoom());
				break; 
				} 
			} 
		for(Weapon w : weapons) 
		{
			if(w.getName().equals(weaponCard.getName())) 
			{ 
				this.putInRoom(w.getPiece(),getCurrentRoom());
				break; 
			} 
		}
		// Iterate through each human player to try and disprove the suggestion
		allHumanIterator = new Turn<Player>(allHumanIterator.getList(), turn.getPos());
		Player player = allHumanIterator.next();
		Map<Player, Set<Card>> disprover = new HashMap<Player, Set<Card>>();

		while (player != currentPlayer) 
		{
			Set<Card> cards = playerHand.get(player);
			if (cards.contains(roomCard)) 
			{
				cards.add(roomCard);
			}
			if (cards.contains(weaponCard)) 
			{
				cards.add(weaponCard);
			}
			if (cards.contains(suspectCard)) 
			{
				cards.add(suspectCard);
			}
			if (!cards.isEmpty()) 
			{
				disprover.put(player, cards);
				return disprover;
			}
		}
		return new HashMap<Player, Set<Card>>();
	}

	/**
	 * 
	 * An accusation is when the player guesses what the murder weapon, murderer
	 * and the murder room is. Any player can make an accusation. The cards must
	 * correctly match the cards in the answer CaseFile If the player is
	 * incorrect, the player is eliminated from the game but their cards are
	 * still hidden from active players and are still used during suggestions.
	 * 
	 * @param player
	 *            - The player making the accusation
	 * @param weaponCard
	 *            - The suspected murder weapon
	 * @param roomCard
	 *            - The suspected room of the murder
	 * @param suspectCard
	 *            - The suspected murderer
	 * @return Whether the player was successful in making their accusation
	 * @throws IllegalMethodCallException
	 *             If the game is over or there are no players left in the game
	 * @throws IllegalArgumentException
	 *             If the player making the accusation does not exist or is not
	 *             an active player Also if the arguments are null
	 */
	public boolean makeAccusation(Player player, WeaponCard weaponCard, RoomCard roomCard, SuspectCard suspectCard) 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (player == null || weaponCard == null || roomCard == null || suspectCard == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		List<Player> players = activeHumanPlayers;
		if (players.isEmpty()) 
		{
			throw new IllegalMethodCallException("No players playing");
		}
		if (!players.contains(player)) 
		{
			throw new IllegalArgumentException("Only active players can make accusations");
		}
		//CaseFile accusation = new CaseFile(suspectCard, weaponCard, roomCard);
		// Game over, the player won!
		if (answer.containsRoomCard(roomCard) && answer.containsSuspectCard(suspectCard)
				&& answer.containsWeaponCard(weaponCard)) 
		{
			gameOver = true;
			return true;
		}
		// Last player in the game failed.
		else if (players.size() == 1) 
		{
			gameOver = true;
			return false;
		} 
		else 
		{
			// Accusation failed, remove player from the game
			players.remove(player);
			int pos = turn.getPos();
			int currentPlayerPos = GameBuilder.SUSPECT_ORDER.get(currentPlayer.getName());
			int removedPos = GameBuilder.SUSPECT_ORDER.get(player.getName());
			if (removedPos <= currentPlayerPos) 
			{
				pos--;
				// Current player failed the accusation
				if (removedPos == currentPlayerPos) 
				{
					// Current player has no more moves as they are eliminated
					remainingMoves = 0;
					// Go to next player if it was the current player who failed
					// to make the accusation
					nextTurn();
					// Remove player from the active players
					turn = new Turn<Player>(players, pos);
				}
			}
			return false;
		}
	}

	/**
	 * Checks if the current player is in the room
	 * 
	 * @return true if the current player is in a room, false otherwise
	 */
	public boolean isInRoom() 
	{
		return playerToRoom.get(currentPlayer) != null; 
	}

	//TODO takeExit description. Unsure why we're returning a Cell? Maybe boolean and rename to canTakeExit?
	public Cell takeExit(Cell cell) throws InvalidMoveException
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (cell == null)
		{
			throw new IllegalArgumentException("Argument cannot be null");
		}
		if (!isInRoom()) 
		{
			throw new InvalidMoveException(currentPlayer.getName() + " is not in a room therefore cannot exit");
		}
		//Used secret passage
		if(cellToRoom.containsKey(cell))
		{
			remainingMoves = 0;
			Room newRoom = cellToRoom.get(cell);
			playerToRoom.put(currentPlayer, newRoom);
			putInRoom(currentPlayer.getPiece(), newRoom);
		}
		else
		{
			board.setPosition(currentPlayer.getPiece(), cell);
		}
		return cell;
	}
	
	/**
	 * Switches to the next player. 
	 * Also resets global values for the next player
	 * @return the next player in the turn
	 */
	public Player nextTurn() 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (remainingMoves != 0) 
		{
			throw new HasRemainingMovesException(currentPlayer.getName() + "  must continue moving");
		}
		if(currentPlayer != null)
		{
			transferred.put(currentPlayer,false);
		}
		currentPlayer = turn.next();
		//Reset for the next player
		if(lastRoom != null)
		{
			lastRoom = getCurrentRoom();
		}
		playerPath = new HashSet<Cell>();
		rollDice();
		return currentPlayer;
	}

	/**
	 * Puts the piece into a free cell in the room
	 * This is to ensure that the piece does not 
	 * appear to block the entrance (cell inside the room
	 * after the door) of the room so that other player's
	 * can enter the room
	 * @param piece - Piece to put in the room
	 * @param room - The room to put the piece in
	 */
	private void putInRoom(Piece piece, Room room) 
	{
		for(Cell cell :roomCells.get(room))
		{
			/*
			 * Do not put a piece on one of the entrance cells as 
			 * it would look like the piece is blocking the room
			 * entrance
			 */
			if(!entranceCells.get(room).contains(cell))
			{
				/*
				 * Put the piece on one of the roomCells if it 
				 * has no other pieces on it
				 */
				if(!board.containsPiece(cell))
				{
					board.setPosition(piece, cell);
					break;
				}
			}
		}
	}

	/**
	 * Simulates the roll of two six-sided die Generate the number of moves for
	 * the current player (when they first decide to move) The number of moves
	 * is between 2 to 12
	 * 
	 * @throws IllegalMethodCallException
	 * If the game is over
	 */
	private void rollDice() 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		assert remainingMoves == 0 : "Last player must not have any remaining moves ";
		int d1 = (int) (Math.random() * 7 + 2);
		int d2 = (int) (Math.random() * 7 + 2);
		remainingMoves = d1 + d2;
	}

	// Getters

	/**
	 * @return The human players still playing the Cluedo game (have not been
	 *         eliminated)
	 */
	public List<Player> getActivePlayers() 
	{
		return Collections.unmodifiableList(activeHumanPlayers);
	}

	/**
	 * @return All the players (human and non-human) in the Cluedo game
	 */
	public List<Player> getAllPlayers() 
	{
		return Collections.unmodifiableList(allPlayers);
	}

	/**
	 * Gets the available exits for the room that the current player is in
	 * This includes the secret passage in the corner rooms
	 * @return The available exits for the room the current player is in
	 * @throws InvalidMoveException
	 * If the player is not in a room
	 * @throws IllegalMethodCallException
	 * If the game is over
	 * @throws NoAvailableExitException
	 * If all exits out of the room are blocked, the player is stuck in the room
	 */
	public List<Cell> getAvailableExits() throws InvalidMoveException, NoAvailableExitException 
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		// Check if player is in a room
		if (!isInRoom()) 
		{
			throw new InvalidMoveException("Cannot move out of a room when the player is not in a room");
		}
		// Get all the cells in the room
		Room room = getCurrentRoom();
		List<Cell> exits = exitCells.get(room);
		/*
		 * Iterate through all the cells and add to the list if the exit isn't
		 * blocked;
		 */
		List<Cell> availableExits = new ArrayList<Cell>();
		for(Cell cell : exits)
		{
			if(!board.containsPiece(cell))
			{
				availableExits.add(cell);
			}
		}
		
		 if(availableExits.isEmpty()) 
		 { 
			 throw new NoAvailableExitException(currentPlayer.getName() + " cannot exit the room as all exits are blocked"); 
		 }
		 
		return Collections.unmodifiableList(availableExits);
	}

	/**
	 * The number of moves the current player has left to make in their turn
	 * 
	 * @return Remaining moves the current player can make
	 */
	public int getRemainingMoves() 
	{
		assert remainingMoves >= 0 : "Cannot have negative remaining moves";
		return remainingMoves;
	}

	// TODO better description for getRoom
	/**
	 * Convenience method for the UI for example to print out whether a player
	 * entered a room
	 * 
	 * @param cell - A room cell
	 * @return The room the cell is in
	 * @throws IllegalArgumentException
	 * If the cell is not in a room or argument is null
	 */
	public Room getRoom(Cell cell) 
	{
		if (cell == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if (!cellToRoom.containsKey(cell)) {
			throw new IllegalArgumentException("Cell must be in a room");
		}
		return cellToRoom.get(cell);
	}
	/**
	 * @return the room the current player is in
	 * @throws IllegalMethodCallException 
	 * If the current player is not in a room
	 */
	public Room getCurrentRoom()
	{
		Room room = playerToRoom.get(currentPlayer);
		if(room == null)
		{
			throw new IllegalMethodCallException(currentPlayer.getName() + " is not in a room");
		}
		return room;
	}
	
	// TODO do description for Game - getPosition
	/**
	 * Used by the UI to read the state of pieces in the game
	 * 
	 * @param piece
	 * @return The cell the piece is in
	 * @throws IllegalArgumentException
	 * If the piece does not exist or argument is null
	 */
	public Cell getPosition(Piece piece) 
	{
		if (piece == null) 
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		return board.getPosition(piece);
	}

	/**
	 * @return All the rooms in the Cluedo game
	 */
	public List<Room> getRooms() 
	{
		return Collections.unmodifiableList(rooms);
	}

	/**
	 * @return All the weapons in the Cluedo game
	 */
	public List<Weapon> getWeapons() 
	{
		return Collections.unmodifiableList(weapons);
	}

	/**
	 * @return The cards not distributed to players or in the answer.
	 */
	public List<Card> getExtraCards() 
	{
		return Collections.unmodifiableList(extraCards);
	}

	/**
	 * @return The weapon cards in the current player's CaseFile
	 */
	public List<WeaponCard> getPlayerWeaponCards() 
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getWeaponCards());
	}

	/**
	 * @return The room cards in the current player's CaseFile
	 */
	public List<RoomCard> getPlayerRoomCards() 
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getRoomCards());
	}

	/**
	 * @return The suspect cards in the current player's CaseFile
	 */
	public List<SuspectCard> getPlayerSuspectCards() 
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getSuspectCards());
	}
	/**
	 * @return All suspect cards
	 */
	public List<Card> getSuspectCards() 
	{
		return Collections.unmodifiableList(suspectCards);
	}
	
	/**
	 * @return All weapon cards
	 */
	public List<Card> getWeaponCards() 
	{
		return Collections.unmodifiableList(weaponCards);
	}
	
	/**
	 * @return All room cards
	 */
	public List<Card> getRoomCards() {
		
		return Collections.unmodifiableList(roomCards);
	}
	
	/**
	 * @return All the cells in the board 
	 */
	public Cell[][] getCells()
	{
		return board.getCells();
	}
	
	/**
	 * @return true if the game is over (finished) false if the game is still going
	 */
	public boolean isGameOver() {
		return gameOver;
	}

}