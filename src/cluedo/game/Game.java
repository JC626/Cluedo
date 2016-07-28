package cluedo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cluedo.board.Board;
import cluedo.exceptions.HasRemainingMovesException;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.model.Cell;
import cluedo.model.Displayable;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.model.Room;
import cluedo.model.Weapon;
import cluedo.model.cards.Card;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;
import cluedo.utility.Heading.Direction;
import cluedo.utility.Turn;

//TODO Game description
/**
 * The Cluedo game This dictates the rules of Cluedo Creates all objects used in
 * the game
 *
 */
public class Game {
	// Constants
	public static final int MIN_HUMAN_PLAYERS = 3;
	public static final int MAX_HUMAN_PLAYERS = 6;
	public static final int MAX_PLAYERS = 6;
	public static final int NUM_WEAPONS = 6;
	public static final int NUM_ROOMS = 9;
	/**
	 * A map of the suspect names mapped to the order the player is according to
	 * the clockwise order of the player's starting position in the Cluedo game
	 */
	private static final Map<String, Integer> SUSPECT_NAMES = new HashMap<String, Integer>();
	private static final String[] WEAPON_NAMES = new String[] { "Dagger", "Candlestick", "Revolver", "Rope",
			"Lead Pipe", "Spanner" };

	private static final String[] ROOM_NAMES = new String[] { "Ballroom", "Billiard Room", "Conservatory",
			"Dining Room", "Hall", "Kitchen", "Library", "Lounge", "Study" };

	int[] KITCHEN = new int[] { 0, 1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 0, 2, 1, 2, 2, 2, 3, 2, 4, 2, 5, 2, 0, 3, 1, 3, 2,
			3, 3, 3, 4, 3, 5, 3, 0, 4, 1, 4, 2, 4, 3, 4, 4, 4, 5, 4, 0, 5, 1, 5, 2, 5, 3, 5, 4, 5, 5, 5, 1, 6, 2, 6, 3,
			6, 4, 6, 5, 6 };

	int[] BALLROOM = new int[] { 10, 2, 11, 2, 12, 2, 13, 2, 8, 3, 9, 3, 10, 3, 11, 3, 12, 3, 13, 3, 14, 3, 15, 3, 8, 4,
			9, 4, 10, 4, 11, 4, 12, 4, 13, 4, 14, 4, 15, 4, 8, 5, 9, 5, 10, 5, 11, 5, 12, 5, 13, 5, 14, 5, 15, 5, 8, 6,
			9, 6, 10, 6, 11, 6, 12, 6, 13, 6, 14, 6, 15, 6, 8, 7, 9, 7, 10, 7, 11, 7, 12, 7, 13, 7, 14, 7, 15, 7 };

	int[] CONSERVATORY = new int[] { 18, 1, 19, 1, 20, 1, 21, 1, 22, 1, 23, 1, 18, 2, 19, 2, 20, 2, 21, 2, 22, 2, 23, 2,
			18, 3, 19, 3, 20, 3, 21, 3, 22, 3, 23, 3, 18, 4, 19, 4, 20, 4, 21, 4, 22, 4, 23, 4, 19, 5, 20, 5, 21, 5, 22,
			5 };

	int[] DINING_ROOM = new int[] { 0, 9, 1, 9, 2, 9, 3, 9, 4, 9, 0, 10, 1, 10, 2, 10, 3, 10, 4, 10, 5, 10, 6, 10, 7,
			10, 0, 11, 1, 11, 2, 11, 3, 11, 4, 11, 5, 11, 6, 11, 7, 11, 0, 12, 1, 12, 2, 12, 3, 12, 4, 12, 5, 12, 6, 12,
			7, 12, 0, 13, 1, 13, 2, 13, 3, 13, 4, 13, 5, 13, 6, 13, 7, 13, 0, 14, 1, 14, 2, 14, 3, 14, 4, 14, 5, 14, 6,
			14, 7, 14, 0, 15, 1, 15, 2, 15, 3, 15, 4, 15, 5, 15, 6, 15, 7, 15 };

	int[] BILLIARD_ROOM = new int[] { 18, 8, 19, 8, 20, 8, 21, 8, 22, 8, 23, 8, 18, 9, 19, 9, 20, 9, 21, 9, 22, 9, 23,
			9, 18, 10, 19, 10, 20, 10, 21, 10, 22, 10, 23, 10, 18, 11, 19, 11, 20, 11, 21, 11, 22, 11, 23, 11, 18, 12,
			19, 12, 20, 12, 21, 12, 22, 12, 23, 12 };

	int[] LIBRARY = new int[] { 18, 14, 19, 14, 20, 14, 21, 14, 22, 14, 17, 15, 18, 15, 19, 15, 20, 15, 21, 15, 22, 15,
			23, 15, 17, 16, 18, 16, 19, 16, 20, 16, 21, 16, 22, 16, 23, 16, 17, 17, 18, 17, 19, 17, 20, 17, 21, 17, 22,
			17, 23, 17, 18, 18, 19, 18, 20, 18, 21, 18, 22, 18 };

	int[] LOUNGE = new int[] { 0, 18, 1, 18, 2, 18, 3, 18, 4, 18, 5, 18, 6, 18, 0, 19, 1, 19, 2, 19, 3, 19, 4, 19, 5,
			19, 6, 19, 0, 20, 1, 20, 2, 20, 3, 20, 4, 20, 5, 20, 6, 20, 0, 21, 1, 21, 2, 21, 3, 21, 4, 21, 5, 21, 6, 21,
			0, 22, 1, 22, 2, 22, 3, 22, 4, 22, 5, 22, 6, 22, 0, 23, 1, 23, 2, 23, 3, 23, 4, 23, 5, 23, 6, 23, 0, 24, 1,
			24, 2, 24, 3, 24, 4, 24, 5, 24 };

	int[] HALL = new int[] { 9, 18, 10, 18, 11, 18, 12, 18, 13, 18, 14, 18, 9, 19, 10, 19, 11, 19, 12, 19, 13, 19, 14,
			19, 9, 20, 10, 20, 11, 20, 12, 20, 13, 20, 14, 20, 9, 21, 10, 21, 11, 21, 12, 21, 13, 21, 14, 21, 9, 22, 10,
			22, 11, 22, 12, 22, 13, 22, 14, 22, 9, 23, 10, 23, 11, 23, 12, 23, 13, 23, 14, 23, 9, 24, 10, 24, 11, 24,
			12, 24, 13, 24, 14, 24 };

	int[] STUDY = new int[] { 17, 21, 18, 21, 19, 21, 20, 21, 21, 21, 22, 21, 23, 21, 17, 22, 18, 22, 19, 22, 20, 22,
			21, 22, 22, 22, 23, 22, 17, 23, 18, 23, 19, 23, 20, 23, 21, 23, 22, 23, 23, 23, 18, 24, 19, 24, 20, 24, 21,
			24, 22, 24, 23, 24 };
	/**
	 * Each player's starting position according to the order
	 * specified in SUSPECT_NAMES
	 */
	int[] STARTINGPOSITION = new int[]{
			7,24, 0,17, 9,0, 14,0, 23,6, 19,23
	};

	private boolean firstTimeMoving;
	private int remainingMoves;
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

	private List<Player> allPlayers;
	/**
	 * All the human players in the game Does not include eliminated players
	 * from the game.
	 */
	private List<Player> activeHumanPlayers;
	private Map<Player, CaseFile> playerToCasefile;

	/**
	 * The cards that each player has in their hand. Every player has different
	 * cards to each other
	 */
	private Map<Player, Set<Card>> playerHand;
	/**
	 * These are the cards leftover after evenly distributing the cards to all
	 * the players. Does not contain the answer cards. Every player will be able
	 * to view these cards. This Set may be empty
	 */
	private Set<Card> extraCards;
	/**
	 * Contains one of each type of card (SuspectCard, WeaponCard, RoomCard).
	 * These are the cards that the player needs to guess correctly to win the
	 * game
	 */
	private CaseFile answer;

	List<SuspectCard> suspectCards;
	List<WeaponCard> weaponCards;
	List<RoomCard> roomCards;

	private Map<Cell, Room> cellToRoom;
	private Map<Player, Boolean> isTransferred;
	private Map<Player, Room> playerToRoom;
	private Room lastRoom; //lastRoom player entered in
	// FIXME Need weapons?
	private Set<Weapon> weapons;
	
	private Board board;
	private boolean gameOver;
	
	// Static initializer
	{
		SUSPECT_NAMES.put("Miss Scarlett", 0);
		SUSPECT_NAMES.put("Colonel Mustard", 1);
		SUSPECT_NAMES.put("Mrs. White", 2);
		SUSPECT_NAMES.put("Reverend Green", 3);
		SUSPECT_NAMES.put("Mrs. Peacock", 4);
		SUSPECT_NAMES.put("Professor Plum", 5);
	}

	// TODO Game class methods

	public Game(int numPlayers, List<Piece> playerTokens, List<Piece> weaponTokens,
			List<Displayable> suspectCardFaces, List<Displayable> weaponCardFaces, List<Displayable> roomCardFaces) {
		if (numPlayers < MIN_HUMAN_PLAYERS || numPlayers > MAX_HUMAN_PLAYERS) {
			throw new IllegalArgumentException(
					"Must have between: " + MIN_HUMAN_PLAYERS + " and " + MAX_HUMAN_PLAYERS + " human players");
		}
		this.allPlayers = createPlayers(playerTokens);
		this.activeHumanPlayers = createHumanPlayers(numPlayers);
		turn = new Turn<Player>(activeHumanPlayers);
		allHumanIterator = new Turn<Player>(activeHumanPlayers);
		this.weapons = createWeapons(weaponTokens);
		// Cards
		suspectCards = createSuspectCards(suspectCardFaces);
		weaponCards = createWeaponCards(weaponCardFaces);
		roomCards = createRoomCards(roomCardFaces);
		answer = createCaseFiles(suspectCards, weaponCards, roomCards);
		extraCards = distributeCards(suspectCards, weaponCards, roomCards);

		isTransferred = new HashMap<Player, Boolean>();
		playerToRoom = new HashMap<Player, Room>();
		setStartingPosition();
		// TODO StartingPositions of players
		
	}

	/**
	 * Create all the players in the Cluedo game
	 * 
	 * @param playerTokens
	 * @return All the players in the Cluedo game
	 */
	private List<Player> createPlayers(List<Piece> playerTokens) {
		List<Player> players = new ArrayList<Player>();
		int i = 0;
		for (String playerName : SUSPECT_NAMES.keySet()) {
			assert i < MAX_PLAYERS : "Exceeded the total number of players";
			Player p = new Player(playerName, playerTokens.get(i));
			players.add(p);
			i++;
		}
		return players;
	}

	/**
	 * Select randomly the characters the human players will play as. The
	 * starting character (and therefore player) is selected randomly. The order
	 * of play is then based off the starting position of the other characters
	 * in a clockwise order. This order is: Miss Scarlett, Colonel Mustard, Mrs.
	 * White, Reverend Green, Mrs. Peacock, Professor Plum
	 * 
	 * @param numPlayers
	 *            - The number of players playing Cluedo
	 * @return The characters that the human players will play as
	 */
	private List<Player> createHumanPlayers(int numPlayers) {
		assert allPlayers != null : "Must create all player objects first";
		assert allPlayers.size() == MAX_PLAYERS : "Must contain all players in the game";
		Set<Player> allRandomPlayers = new TreeSet<Player>(allPlayers);
		Player[] playerArr = new Player[MAX_HUMAN_PLAYERS];
		Player startingPlayer = null;
		// Generate random players
		for (Player randPlayer : allRandomPlayers) {
			if (numPlayers == 0) {
				break;
			}
			if (startingPlayer == null) {
				startingPlayer = randPlayer;
				playerArr[0] = startingPlayer;
			} else {
				// Put added players in a clockwise order based off the starting
				// player's position on the board
				int startOrder = SUSPECT_NAMES.get(startingPlayer.getName());
				int playerOrder = SUSPECT_NAMES.get(randPlayer.getName());
				int index = playerOrder > startOrder ? playerOrder - startOrder : playerOrder + startOrder;
				playerArr[index] = randPlayer;
			}
			numPlayers--;

		}
		List<Player> players = new ArrayList<Player>();
		// Remove all null references in the array to put in the list
		for (Player sortPlayer : playerArr) {
			if (sortPlayer != null) {
				players.add(sortPlayer);
			}
		}
		return players;
	}

	/**
	 * Create the weapons in the Cluedo Game
	 * 
	 * @param weaponTokens
	 * @return All the weapons in the Cluedo Game
	 */
	private Set<Weapon> createWeapons(List<Piece> weaponTokens) {
		Set<Weapon> weapons = new TreeSet<Weapon>();
		for (int i = 0; i < NUM_WEAPONS; i++) {
			Weapon w = new Weapon(WEAPON_NAMES[i], weaponTokens.get(i));
			weapons.add(w);
		}
		return weapons;
	}

	/**
	 * Create the weapon cards in the Cluedo Game
	 * 
	 * @param weaponCardFaces
	 * @return All the weapon cards in the Cluedo Game
	 */
	private List<WeaponCard> createWeaponCards(List<Displayable> weaponCardFaces) {
		List<WeaponCard> weaponCards = new ArrayList<WeaponCard>();
		for (int i = 0; i < NUM_WEAPONS; i++) {
			WeaponCard card = new WeaponCard(WEAPON_NAMES[i], weaponCardFaces.get(i));
			weaponCards.add(card);
		}
		return weaponCards;
	}

	/**
	 * Create the suspect cards in the Cluedo Game
	 * 
	 * @param suspectCardFaces
	 * @return All the suspect cards in the Cluedo Game
	 */
	private List<SuspectCard> createSuspectCards(List<Displayable> suspectCardFaces) {
		List<SuspectCard> suspectCards = new ArrayList<SuspectCard>();
		int i = 0;
		for (String suspectName : SUSPECT_NAMES.keySet()) {
			assert i < MAX_PLAYERS : "Exceeded the total number of suspects";
			SuspectCard p = new SuspectCard(suspectName, suspectCardFaces.get(i));
			suspectCards.add(p);
			i++;
		}
		return suspectCards;
	}

	/**
	 * Create the room cards in the Cluedo Game
	 * 
	 * @param roomCardFaces
	 * @return All the room cards in the Cluedo Game
	 */
	private List<RoomCard> createRoomCards(List<Displayable> roomCardFaces) {
		List<RoomCard> roomCards = new ArrayList<RoomCard>();
		for (int i = 0; i < NUM_ROOMS; i++) {
			RoomCard card = new RoomCard(ROOM_NAMES[i], roomCardFaces.get(i));
			roomCards.add(card);
		}
		return roomCards;
	}

	/**
	 * Creates a CaseFile for each human player and the answer CaseFile
	 * 
	 * @param suspectCards
	 *            - all the suspect cards
	 * @param weaponCards
	 *            - all the weapon cards
	 * @param roomCards
	 *            - all the room cards
	 * @return The CaseFile for the answer of the game
	 */
	private CaseFile createCaseFiles(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards,
			List<RoomCard> roomCards) {
		for (Player player : activeHumanPlayers) {
			playerToCasefile.put(player, new CaseFile(suspectCards, weaponCards, roomCards));
		}
		SuspectCard answerSuspect = null;
		for (SuspectCard suspect : suspectCards) {
			answerSuspect = suspect;
			suspectCards.remove(suspect);
			break;
		}
		WeaponCard answerWeapon = null;
		for (WeaponCard weapon : weaponCards) {
			answerWeapon = weapon;
			weaponCards.remove(weapon);
			break;
		}
		RoomCard answerRoom = null;
		for (RoomCard room : roomCards) {
			answerRoom = room;
			roomCards.remove(room);
			break;
		}
		return new CaseFile(answerSuspect, answerWeapon, answerRoom);
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
	private Set<Card> distributeCards(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards,
			List<RoomCard> roomCards) {
		Set<Card> extra = new TreeSet<Card>();
		Set<Card> allCards = new TreeSet<Card>();
		allCards.addAll(suspectCards);
		allCards.addAll(weaponCards);
		allCards.addAll(roomCards);
		int numPlayers = activeHumanPlayers.size();
		int numExtra = allCards.size() % numPlayers;
		//Number of cards each player will get
		int numCards = (allCards.size() - numExtra) / numPlayers; 
		Set<Card> cardsForPlayer = new TreeSet<Card>();
		for (Card card : allCards) {
			// All cards evenly distributed, put the rest of the cards in extra
			if (numPlayers == 0) {
				extra.add(card);
				continue;
			}
			// Remove the card from the player's CaseFile
			Player player = activeHumanPlayers.get(numPlayers - 1);
			CaseFile caseFile = playerToCasefile.get(player);
			caseFile.removeCard(card);
			cardsForPlayer.add(card);
			numCards--;
			if (numCards == 0) {
				// Put the cards in for a human player
				playerHand.put(player, cardsForPlayer);
				// Go to the next player
				numPlayers--;
				cardsForPlayer = new TreeSet<Card>();
			}
		}
		return extra;
	}
	private void setStartingPosition(){
		int playerCount = 0;
		for(int i = 0; i < STARTINGPOSITION.length;i+=2)
		{
			int x = STARTINGPOSITION[i];
			int y = STARTINGPOSITION[i+1];
			board.setPosition(allPlayers.get(playerCount).getPiece(), x, y);
			playerCount++;
		}
		//TODO Set startingPosition for weapons. Need roomCells
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
	 *             or reentered the room they exited on the same turn
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
			if (firstTimeMoving) 
			{
				rollDice();
				firstTimeMoving = false;
			} else 
			{
				throw new InvalidMoveException("Cannot move as no moves left");
			}
		}
		Cell newPos = board.move(currentPlayer.getPiece(), direction);
		if(lastRoom != null && cellToRoom.containsKey(newPos))
		{
			Room room = cellToRoom.get(newPos);
			if(room.getName().equals(lastRoom.getName()))
			{
				throw new InvalidMoveException(currentPlayer.getName() + " cannot reenter the same room they exited");
			}
		}
		remainingMoves--;
		return newPos;
	}
	
	//TODO canMakeSuggestionMethod
	public boolean canMakeSuggestion(){
		if (gameOver) {
			return false;
		}
		if (!isInRoom()) {
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
	public Map<Player, Set<Card>> makeSuggestion(WeaponCard weaponCard, SuspectCard suspectCard) {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		if (weaponCard == null || suspectCard == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if (!isInRoom()) {
			throw new IllegalMethodCallException(
					currentPlayer.getName() + " cannot make a suggestion as they are not in a room");
		}
		// Get the roomCard for the room the current player is in
		RoomCard roomCard = null;
		String roomName = playerToRoom.get(currentPlayer).getName();
		for (RoomCard card : roomCards) {
			if (card.getName().equals(roomName)) {
				roomCard = card;
				break;
			}
		}
		// TODO move the suspect and weapon pieces into the room
		/*
		 * for(Player p : allPlayers) {
		 * if(p.getName().equals(suspectCard.getName())) { //Put in the room
		 * break; } } for(Weapon w : weapons) {
		 * if(w.getName().equals(weaponCard.getName())) { //Put in the room
		 * break; } }
		 */

		// Iterate through each human player to try and disprove the suggestion
		allHumanIterator = new Turn<Player>(allHumanIterator.getList(), turn.getPos());
		Player player = allHumanIterator.next();
		Map<Player, Set<Card>> disprover = new HashMap<Player, Set<Card>>();

		while (player != currentPlayer) {
			Set<Card> cards = playerHand.get(player);
			if (cards.contains(roomCard)) {
				cards.add(roomCard);
			}
			if (cards.contains(weaponCard)) {
				cards.add(weaponCard);
			}
			if (cards.contains(suspectCard)) {
				cards.add(suspectCard);
			}
			if (!cards.isEmpty()) {
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
	public boolean makeAccusation(Player player, WeaponCard weaponCard, RoomCard roomCard, SuspectCard suspectCard) {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		if (player == null || weaponCard == null || roomCard == null || suspectCard == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		List<Player> players = getActivePlayers();
		if (players.isEmpty()) {
			throw new IllegalMethodCallException("No players playing");
		}
		if (!players.contains(player)) {
			throw new IllegalArgumentException("Only active players can make accusations");
		}
		CaseFile accusation = new CaseFile(suspectCard, weaponCard, roomCard);
		if (accusation.equals(answer)) {// Game over, the player won!
			gameOver = true;
			return true;
		} else if (players.size() == 1) {// Last player in the game failed.
			gameOver = true;
			return false;
		} else {
			// Accusation failed, remove player from the game
			players.remove(player);
			int pos = turn.getPos();
			int currentPlayerPos = SUSPECT_NAMES.get(currentPlayer.getName());
			int removedPos = SUSPECT_NAMES.get(player.getName());
			if (removedPos <= currentPlayerPos) {
				pos--;
				// Current player failed the accusation
				if (removedPos == currentPlayerPos) {
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
	public boolean isInRoom() {
		return playerToRoom.get(currentPlayer) != null;
	}

	// TODO implement Game - takeExit.
	public Cell takeExit(Cell cell) throws InvalidMoveException {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		if (cell == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		if (isInRoom()) {
			throw new InvalidMoveException(currentPlayer.getName() + " is not in a room therefore cannot exit");
		}
		return null;
	}

	public Player nextTurn() {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		if (remainingMoves != 0) {
			throw new HasRemainingMovesException(currentPlayer.getName() + "  must continue moving");
		}
		currentPlayer = turn.next();
		firstTimeMoving = true;
		lastRoom = playerToRoom.get(currentPlayer);
		return currentPlayer;
	}

	// TODO method in Game class to allocate a piece into one of the room cells
	// Need map of Room -> Cell room cells. Also need Room -> Cell exit cells
	private void putInRoom(Piece piece, Room room) {

	}

	/**
	 * Simulates the roll of two six-sided die Generate the number of moves for
	 * the current player (when they first decide to move) The number of moves
	 * is between 2 to 12
	 * 
	 * @throws IllegalMethodCallException
	 *             If the game is over
	 */
	private void rollDice() {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		assert remainingMoves == 0 : "Last player must not have any remaining moves ";
		int d1 = (int) (Math.random() * 7 + 1);
		int d2 = (int) (Math.random() * 7 + 1);
		remainingMoves = d1 + d2;
	}

	// Getters

	/**
	 * @return The human players still playing the Cluedo game (have not been
	 *         eliminated)
	 */
	public List<Player> getActivePlayers() {
		return Collections.unmodifiableList(activeHumanPlayers);
	}

	// TODO implement Game - getAvailable exits
	public List<Cell> getAvailableExits() throws InvalidMoveException {
		if (gameOver) {
			throw new IllegalMethodCallException("Game is over.");
		}
		// Check if player is in a room
		if (!isInRoom()) {
			throw new InvalidMoveException("Cannot move out of a room when the player is not in a room");
		}
		// Get all the cells in the room
		List<Cell> availableExitCells = new ArrayList<Cell>();
		/*
		 * Iterate through all the cells and add to the list if the exit isn't
		 * blocked;
		 */
		/*
		 * if(availableExitCells.isEmpty()) { throw new
		 * NoAvailableExitException(""); }
		 */
		return Collections.unmodifiableList(availableExitCells);
	}

	/**
	 * The number of moves the current player has left to make in their turn
	 * 
	 * @return Remaining moves the current player can make
	 */
	public int getRemainingMoves() {
		assert remainingMoves >= 0 : "Cannot have negative remaining moves";
		return remainingMoves;
	}

	// TODO better description for getRoom
	/**
	 * Convenience method for the UI for example to print out whether a player
	 * entered a room
	 * 
	 * @param cell
	 *            - A room cell
	 * @return The room the cell is in
	 * @throws IllegalArgumentException
	 *             If the cell is not in a room or argument is null
	 */
	public Room getRoom(Cell cell) {
		if (cell == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if (!cellToRoom.containsKey(cell)) {
			throw new IllegalArgumentException("Cell must be in a room");
		}
		return cellToRoom.get(cell);
	}

	// TODO do description for Game - getPosition
	/**
	 * Used by the UI to read the state of pieces in the game
	 * 
	 * @param piece
	 * @return The cell the piece is in
	 * @throws IllegalArgumentException
	 *             If the piece does not exist or argument is null
	 */
	public Cell getPosition(Piece piece) {
		if (piece == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		return board.getPosition(piece);
	}

	// TODO getRooms Game
	public List<Room> getRooms() {
		return null;
	}

	/**
	 * @return All the weapons in the Cluedo game
	 */
	public Set<Weapon> getWeapons() {
		return Collections.unmodifiableSet(weapons);
	}

	/**
	 * @return The cards not distributed to players or in the answer.
	 */
	public Set<Card> getExtraCards() {
		return Collections.unmodifiableSet(extraCards);
	}

	/**
	 * @return The weapon cards in the current player's CaseFile
	 */
	public List<WeaponCard> getPlayerWeaponCards() {
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getWeaponCards());
	}

	/**
	 * @return The room cards in the current player's CaseFile
	 */
	public List<RoomCard> getPlayerRoomCards() {
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getRoomCards());
	}

	/**
	 * @return The suspect cards in the current player's CaseFile
	 */
	public List<SuspectCard> getPlayerSuspectCards() {
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableList(casefile.getSuspectCards());
	}
	
	public Cell[][] getCells(){
		return board.getCells();
	}

	public boolean isGameOver() {
		return gameOver;
	}

	//TODO change cards to a list
	/**
	 * A CaseFile is either an answer case file, or a player’s case file,
	 * although there is no technical distinction. 
	 * For a player's CaseFile, it contains the cards that have not been eliminated
	 * from the case and could therefore could still be part of the answer
	 * Cards may be removed from a player's CaseFile throughout the game.
	 * 
	 */
	private class CaseFile {
		private List<SuspectCard> suspectCards;
		private List<WeaponCard> weaponCards;
		private List<RoomCard> roomCards;

		public CaseFile(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards, List<RoomCard> roomCards) {
			if (suspectCards.size() == 0 || weaponCards.size() == 0 || roomCards.size() == 0) {
				throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
			}
			this.roomCards = roomCards;
			this.suspectCards = suspectCards;
			this.weaponCards = weaponCards;
		}

		public CaseFile(SuspectCard suspectC, WeaponCard weaponC, RoomCard roomC) {
			if (suspectC == null || weaponC == null || roomC == null) {
				throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
			}
			suspectCards = new ArrayList<SuspectCard>();
			suspectCards.add(suspectC);
			weaponCards = new ArrayList<WeaponCard>();
			weaponCards.add(weaponC);
			roomCards = new ArrayList<RoomCard>();
			roomCards.add(roomC);
		}

		/**
		 * Remove the card from the casefile
		 * 
		 * @param card
		 *            - The card to remove from the casefile
		 */
		public void removeCard(Card card) {
			if (card instanceof SuspectCard) {
				suspectCards.remove(card);
			} else if (card instanceof RoomCard) {
				roomCards.remove(card);
			} else if (card instanceof WeaponCard) {
				weaponCards.remove(card);
			}
		}

		public List<SuspectCard> getSuspectCards() {
			return suspectCards;
		}

		public List<WeaponCard> getWeaponCards() {
			return weaponCards;
		}

		public List<RoomCard> getRoomCards() {
			return roomCards;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((roomCards == null) ? 0 : roomCards.hashCode());
			result = prime * result + ((suspectCards == null) ? 0 : suspectCards.hashCode());
			result = prime * result + ((weaponCards == null) ? 0 : weaponCards.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CaseFile other = (CaseFile) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (roomCards == null) {
				if (other.roomCards != null)
					return false;
			} else if (!roomCards.equals(other.roomCards))
				return false;
			if (suspectCards == null) {
				if (other.suspectCards != null)
					return false;
			} else if (!suspectCards.equals(other.suspectCards))
				return false;
			if (weaponCards == null) {
				if (other.weaponCards != null)
					return false;
			} else if (!weaponCards.equals(other.weaponCards))
				return false;
			return true;
		}

		private Game getOuterType() {
			return Game.this;
		}

	}

}