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
import cluedo.utility.Heading;
import cluedo.utility.Turn;

/**
 * The Cluedo game.
 * Requires the GameBuilder and RoomBuilder class to set up the game
 * Also requires the CellBuilder class to create the board
 * representation of the game.
 * This class dictates the rules and logic of Cluedo 
 * Players can move, enter and exit rooms,
 * make a suggestion and make an accusation.
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
	 * The values of the dice roll.
	 * There are exactly two dice.
	 */
	private int[] diceRoll = new int[2];
	/**
	 * The cells the current player moved to during their turn
	 */
	private Set<Cell> playerPath;
	/**
	 * Used to check that a player has made a suggestion as
	 * they can only make one suggestion per turn
	 */
	private boolean hasMadeSuggestion;
	private Player currentPlayer;
	/**
	 * One round of the Cluedo game. Contains all the active players
	 */
	private final Turn<Player> turn;
	/**
	 * Iterator that contains all human players in the game (i.e. eliminated and
	 * active players)
	 */
	private Turn<Player> allHumanIterator;
	
	/**
	 * All the players in Cluedo (human and non-human)
	 */
	public static final List<Player> allPlayers = Collections.unmodifiableList(GameBuilder.createPlayers());
	/**
	 * All the human players in the game Does not include eliminated players
	 * from the game.
	 */
	private List<Player> activeHumanPlayers;
	
	/**
	 * The names of the human players. This is useful if the UI wants to
	 * reference characters by their associated human name.
	 */
	private List<String> humanPlayerNames;
	
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
	private Set<Cell> doorCells;
	private Set<Cell> outOfBoundCells;
	private Set<Cell> secretPassageCells;

	public Game(List<Player> activePlayers, List<String> playerNames) 
	{
		if (playerNames == null || activePlayers == null || playerNames.size() != activePlayers.size())
		{
			throw new IllegalArgumentException("Arguments must be non null lists of equal size");
		}
		
		int numPlayers = activePlayers.size();
		if (numPlayers < MIN_HUMAN_PLAYERS || numPlayers > MAX_HUMAN_PLAYERS) 
		{
			throw new IllegalArgumentException(
					"Must have between: " + MIN_HUMAN_PLAYERS + " and " + MAX_HUMAN_PLAYERS + " human players");
		}
		
		CellBuilder cellBuilder = new CellBuilder();
		doorCells = cellBuilder.getDoorCells();
		outOfBoundCells = cellBuilder.getOutOfBoundsCells();
		secretPassageCells = cellBuilder.getSecretPassageCells();
		board = new Board(cellBuilder.getCells());
		//Players
		activeHumanPlayers = activePlayers;
		humanPlayerNames = playerNames;
		
		/*
		 * Cannot have the same references as removing a player from active players
		 * should not remove it from a turn (as nextTurn will check if the player is active or not)
		 */
		List<Player> allHumanPlayers = new ArrayList<Player>(activeHumanPlayers);
		turn = new Turn<Player>(allHumanPlayers); //Ensure turn starts on the first player
		allHumanIterator = new Turn<Player>(allHumanPlayers);
		//Weapons
		weapons = GameBuilder.createWeapons();
		// Cards
		suspectCards = GameBuilder.createSuspectCards();
		weaponCards = GameBuilder.createWeaponCards();
		roomCards = GameBuilder.createRoomCards();
		answer = GameBuilder.createCaseFiles(suspectCards, weaponCards, roomCards,playerToCasefile,activeHumanPlayers);
		extraCards = distributeCards(suspectCards, weaponCards, roomCards);
		//Room
		RoomBuilder roomBuilder = new RoomBuilder(board.getCells());
		cellToRoom = roomBuilder.getCellToRoom();
		roomCells = roomBuilder.getRoomCells();
		entranceCells = roomBuilder.getEntranceCells();
		exitCells = roomBuilder.getExitCells();
		rooms = roomBuilder.getRooms();
		//Initialisation
		setStartingPosition();
		nextTurn();
	}
	
	/**
	 * Distribute the remaining cards (all the cards except the cards in the 
	 * answer casefile) to the players. 
	 * The cards that each player has is removed from their
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
		//Remove answer cards as they cannot be distributed
		RoomCard answerRoom = answer.getRoomCards().get(0);
		WeaponCard answerWeapon = answer.getWeaponCards().get(0);
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		allCards.remove(answerWeapon);
		allCards.remove(answerRoom);
		allCards.remove(answerSuspect);
		int numPlayers = activeHumanPlayers.size();
		int numExtra = allCards.size() % numPlayers;
		//Number of cards each player will get
		int numCards = (allCards.size() - numExtra) / numPlayers; 
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
				//Remove extra card from each player's CaseFile
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
			board.setPosition(allPlayers.get(playerCount), x, y);
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
			Piece piece = weapons.get(i);
			this.putInRoom(piece, room);
			i++;
		}
	}
	/**
	 * Set the player to their starting position.
	 * Used for resetting the player's position
	 * when they failed their accusation
	 * @param player - Player who failed an accusation
	 */
	private void setStartingPosition(Player player)
	{
		int x = allPlayers.indexOf(player)*2;
		int y = x+1;
		board.setPosition(player, STARTINGPOSITION[x],STARTINGPOSITION[y]);
	}

	/**
	 * Checks whether a player can move or exit a room
	 * @return true if the player can move in any direction, 
	 * false if the player cannot move
	 */
	public boolean canMove()
	{		
		if(remainingMoves == 0)
		{
			return false;
		}
		Set<Direction> directions = new HashSet<Direction>();
		Set<Direction> toRemove = new HashSet<Direction>();
		directions.add(Direction.North);
		directions.add(Direction.South);
		directions.add(Direction.West);
		directions.add(Direction.East);
		if(!isInRoom())
		{
			Cell pos = getPosition(currentPlayer);
			Set<Direction> walls = pos.getWalls();
			directions.removeAll(walls);
			int x = pos.getX();
			int y = pos.getY();
			Cell[][] cells = getCells();
			for(Direction dir : directions)
			{
				Cell checkCell = null;
				switch(dir)
				{
					case North:
						checkCell = cells[x][y-1];
						break;
					case South:
						checkCell = cells[x][y+1];
						break;
					case East:
						checkCell = cells[x+1][y];
						break;
					case West:
						checkCell = cells[x-1][y];
						break;
				}
				assert checkCell != null;
				if(board.containsPiece(checkCell) ||
						playerPath.contains(checkCell) ||
				checkCell.hasWall(Heading.opposite(dir)))
				{
					toRemove.add(dir);
				}
			}
			directions.removeAll(toRemove);
			if(directions.isEmpty())
			{
				/*
				 * Set remaining moves to zero so nextTurn() can be called
				 * without throwing an exception
				 */
				remainingMoves = 0;
				return false;
			}
		}
		else
		{
			try 
			{
				return !getAvailableExits().isEmpty();
			} 
			catch (NoAvailableExitException e) {
				remainingMoves = 0;
				return false;
			} 
			catch (InvalidMoveException e)
			{	
			}
		}
		return true;
	}

	/**
	 * Checks if the player can move by calling the move method in the Board
	 * class. Assigns new player's moves using rollDice()
	 * 
	 * @param direction
	 *            - direction player is moving towards from their current cell
	 *            position
	 * @return The cell that the player has moved to
	 * @throws InvalidMoveException
	 *             If the current player has no more moves remaining
	 *             or reentered the room they exited in the same turn
	 *             or move on a cell they already passed on the same turn
	 *             or moved towards a wall
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
		Cell oldPos = getPosition(currentPlayer);
		/*
		 * Walls are not defined in both cells 
		 * (i.e. cell may not have a South wall
		 * but neighbouring cell will have North wall)
		 *  so have to check both cells if there is a wall
		 */
		if(oldPos.hasWall(direction))
		{
			throw new InvalidMoveException("Cannot move in that direction as a wall is blocking the way");
		}
		Cell newPos = board.getNeighbouringCell(oldPos, direction);
		if(newPos.hasWall(Heading.opposite(direction)))
		{
			throw new InvalidMoveException("Cannot move in that direction as a wall is blocking the way");
		}
		if(lastRoom != null && lastRoom.equals(cellToRoom.get(newPos)))
		{
			throw new InvalidMoveException(currentPlayer.getName() + " cannot reenter the same room they exited");
		}
		if(playerPath.contains(newPos))
		{
			throw new InvalidMoveException("Cannot move to the same cell in the same turn");
		}
		//Player going into a room
		if(cellToRoom.containsKey(newPos))
		{
			Room room = cellToRoom.get(newPos);
			if(!entranceCells.get(room).contains(newPos))
			{
				throw new InvalidMoveException("Did not enter the room through a valid entrance");
			}
			//Reallocate the player to a cell in the room
			newPos = this.putInRoom(currentPlayer, room);
			remainingMoves = 0;
		}
		else
		{
			playerPath.add(newPos);
			//Actually move the player to the cell
			board.move(currentPlayer, direction);
			remainingMoves--;
		}
		return newPos;
	}
	
	/**
	 * Checks whether the player can actually make a suggestion
	 * The player must be in a room to make a suggestion and 
	 * can also make a suggestion if they 
	 * were transferred into that room.
	 * A suggestion can only be made once per turn
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
		if(hasMadeSuggestion)
		{
			return false;
		}
		/*
		 * A player that has transferred (due to another player's suggestion)
		 * can make a suggestion
		 */
		if(transferred.containsKey(currentPlayer) && transferred.get(currentPlayer))
		{
			return true;
		}
		if (remainingMoves != 0) 
		{
			return false;
		}
		return true;
	}

	/**
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
	 * @return A map of the disproving player and the card(s) that the disproving
	 * player has that matches the suggestion.
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
		if(hasMadeSuggestion)
		{
			throw new IllegalMethodCallException("Can only make a suggestion once per turn");
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
		//Find the player that matches the suspect card
		for(Player p : allPlayers) 
		{
			if(p.getName().equals(suspectCard.getName())) 
			{ 
				//Transfer a player if they are not in the room
				if(playerToRoom.get(p) != getCurrentRoom())
				{
					//Put in the room
					this.putInRoom(p,getCurrentRoom());
					playerToRoom.put(p, getCurrentRoom());
					transferred.put(p, true);
				}
				break; 
			} 
		} 
		
		//Transfer the weapon that matches the weapon card into the room
		for(Weapon w : weapons) 
		{
			if(w.getName().equals(weaponCard.getName())) 
			{ 
				Cell weaponPos = getPosition(w);
				//Weapon is not in this room
				if(cellToRoom.containsKey(weaponPos) &&
 						!cellToRoom.get(weaponPos).equals(getCurrentRoom()))
				{
					this.putInRoom(w,getCurrentRoom());
					break; 
				}	
			} 
		}
		// Iterate through each human player to try and disprove the suggestion
		allHumanIterator = new Turn<Player>(allHumanIterator.getList(), turn.getPos());
		Player player = allHumanIterator.next();
		Map<Player, Set<Card>> disprover = new HashMap<Player, Set<Card>>();
		hasMadeSuggestion = true;
		if(transferred.containsKey(currentPlayer) && transferred.get(currentPlayer))
		{
			remainingMoves = 0;
		}
		while (player != currentPlayer) 
		{
			Set<Card> playerCards = playerHand.get(player);
			Set<Card> disprovingCards = new HashSet<Card>();
			if (playerCards.contains(roomCard)) 
			{
				disprovingCards.add(roomCard);
			}
			if (playerCards.contains(weaponCard)) 
			{
				disprovingCards.add(weaponCard);
			}
			if (playerCards.contains(suspectCard)) 
			{
				disprovingCards.add(suspectCard);
			}
			if (!disprovingCards.isEmpty()) 
			{
				disprover.put(player, disprovingCards);
				return disprover;
			}
			player = allHumanIterator.next();
		}
		return new HashMap<Player, Set<Card>>();
	}
	/**
	 * Removes the card from the current player's CaseFile. 
	 * This is used when a disproving player 
	 * has one or more cards that match the suggestion
	 * and therefore must choose one card to show to the
	 * suggesting player.
	 * 
	 * @param disprover - The player disproving the suggestion and 
	 * who has selected a card
	 * @throws IllegalMethodCallException
	 * If the game is over
	 * @throws IllegalArgumentException
	 * If the map is null
	 * or there is not one key-value pair 
	 * or the disproving player doesn't have that card
	 */
	public void removeCard(Map<Player,Card> disprover)
	{
		if (gameOver) 
		{
			throw new IllegalMethodCallException("Game is over.");
		}
		if (disprover == null) 
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if(disprover.size() != 1)
		{
			throw new IllegalArgumentException("Map must only contain one key value pair");
		}
		for(Map.Entry<Player,Card> entry : disprover.entrySet())
		{
			Player p = entry.getKey();
			Card card = entry.getValue();
			if(!playerHand.get(p).contains(card))
			{
				throw new IllegalArgumentException("Disproving player must have the card in their hand");
			}
			playerToCasefile.get(currentPlayer).removeCard(card);
			return;
		}
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
			players.remove(player);
			setStartingPosition(player);
			gameOver = true;
			return false;
		} 
		else 
		{
			// Accusation failed, remove player from the game
			players.remove(player);
			setStartingPosition(player);
			if(player == currentPlayer)
			{
				remainingMoves = 0;
				nextTurn();
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

	/**
	 * The current player actually takes an exit out of 
	 * the current room they are in
	 * @param cell - The proposed exit taken by the player
	 * @throws InvalidMoveException
	 * If player is not in a room,
	 * or player has no moves left
	 * or player is trying to move to an 
	 * exit that is blocked by another player
	 * @throws IllegalArgumentException
	 * If cell given is not an exit cell or is null
	 * @return The exit the player took or 
	 * a random room cell position if the player took the secret passage
	 */
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
		if(!exitCells.get(getCurrentRoom()).contains(cell))
		{
			throw new IllegalArgumentException("Cannot move to a cell that is not an exit");
		}

		if (remainingMoves <= 0) 
		{
			throw new InvalidMoveException("Cannot move as no moves left");
		}	
		if(board.containsPiece(cell))
		{
			throw new InvalidMoveException("Another player is blocking that exit");
		}
		//Used secret passage
		if(cellToRoom.containsKey(cell))
		{
			remainingMoves = 0;
			Room newRoom = cellToRoom.get(cell);
			return putInRoom(currentPlayer, newRoom);
		}
		else
		{
			board.setPosition(currentPlayer, cell);
			lastRoom = getCurrentRoom();
			playerToRoom.put(currentPlayer, null);
			remainingMoves--;
			playerPath.add(cell);
		}
		return cell;
	}
	
	/**
	 * Switches to the next active human player. 
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
		//Ensure player is actually active
		while(!activeHumanPlayers.contains(currentPlayer))
		{
			currentPlayer = turn.next();
		}
		//Reset for the next player
		if(lastRoom != null)
		{
			lastRoom = null;
		}
		playerPath = new HashSet<Cell>();
		Cell playerPos = getPosition(currentPlayer);
		playerPath.add(playerPos);
		hasMadeSuggestion = false;
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
	 * @return The cell the piece is allocated to in the room
	 */
	private Cell putInRoom(Piece piece, Room room) 
	{
		if(!rooms.contains(room))
		{
			throw new IllegalArgumentException(room.getName()+ " is not a valid room!");
		}
		if(piece == currentPlayer)
		{
			playerToRoom.put(currentPlayer, room);
		}
		for(Cell cell:roomCells.get(room))
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
					return cell;
				}
			}
		}
		throw new IllegalMethodCallException("No free room cells");
	}

	/**
	 * Simulates the roll of two six-sided die 
	 * Generate the number of moves for the current player (when they first decide to move) 
	 * The number of moves is between 2 to 12
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
		int d1 = (int) (Math.random() * 6 + 1);
		int d2 = (int) (Math.random() * 6 + 1);
		
		// Update our diceRoll values for the calling class.
		diceRoll[0] = d1;
		diceRoll[1] = d2;
		
		remainingMoves = d1 + d2;
	}

	// Getters
	
	/**
	 * The dice that made the remainingMoves value.
	 * @return The two values that the dice turned up.
	 * Will have exactly two values between 1 and 6 inclusive.
	 */
	public int[] getDiceRoll()
	{
		assert diceRoll.length == 2;
		return diceRoll;
	}
	
	/**
	 * Gets the answer casefile of the game when the game is over
	 * @throws IllegalMethodCallException
	 * If the game is not over
	 * @return The cards from the answer caseFile
	 */
	public List<Card> getAnswer()
	{
		if(gameOver)
		{
			List<Card> answerCards = new ArrayList<Card>();
			Card roomCard = answer.getRoomCards().get(0);
			Card suspectCard = answer.getSuspectCards().get(0);
			Card weaponCard = answer.getWeaponCards().get(0);
			answerCards.add(suspectCard);
			answerCards.add(weaponCard);
			answerCards.add(roomCard);
			return Collections.unmodifiableList(answerCards);
		}
		throw new IllegalMethodCallException("Cannot access answer if game is not over");
	}
	/**
	 * @return The human players still playing the Cluedo game (have not been
	 *         eliminated)
	 */
	public List<Player> getActivePlayers() 
	{
		return Collections.unmodifiableList(activeHumanPlayers);
	}
	
	/**
	 * 
	 */
	public String getHumanName(Player character)
	{
		int nameIndex = activeHumanPlayers.indexOf(character);
		
		if (nameIndex == -1)
		{
			throw new IllegalArgumentException("character must be an active human player");
		}
		
		return humanPlayerNames.get(nameIndex);
	}

	/**
	 * @return The player whose turn it is in the Cluedo game
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
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

	/**
	 * Get the room that a cell is part of
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
	
	/**
	 * Find the location of pieces in the game
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
	
	public Set<Cell> getDoorCells() 
	{
		return doorCells;
	}

	public Set<Cell> getOutOfBoundCells() 
	{
		return outOfBoundCells;
	}

	public Set<Cell> getSecretPassageCells() 
	{
		return secretPassageCells;
	}
	
	public Set<Cell> getRoomCells()
	{
		Set<Cell> cellsInRoom = new HashSet<Cell>();
		for(Set<Cell> cells : roomCells.values())
		{
			cellsInRoom.addAll(cells);
		}
		return cellsInRoom;
	}

	/**
	 * @return true if the game is over (finished) false if the game is still going
	 */
	public boolean isGameOver() {
		return gameOver;
	}

}