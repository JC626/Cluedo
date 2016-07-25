 package cluedo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cluedo.board.Board;
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

//TODO Game description
/**
 * The Cluedo game
 * This dictates the rules of Cluedo
 * Creates all objects used in the game 
 *
 */
public class Game
{
	//Constants
	public static final int MIN_HUMAN_PLAYERS = 3;
	public static final int MAX_HUMAN_PLAYERS = 6;
	public static final int MAX_PLAYERS = 6;
	public static final int NUM_WEAPONS = 6;
	public static final int NUM_ROOMS = 9;
	/**
	 * A map of the suspect names mapped to the order the player is
	 * according to the clockwise order of the player's starting position
	 * in the Cluedo game
	 */
	private static final Map<String,Integer> SUSPECT_NAMES = new HashMap<String,Integer>();
	private static final String[] WEAPON_NAMES = new String[]
			{
					"Dagger",
					"Candlestick",
					"Revolver",
					"Rope",
					"Lead Pipe",
					"Spanner"
			};
	private static final String[] ROOM_NAMES = new String[]
			{
					"Ballroom",
					"Billiard Room",
					"Conservatory",
					"Dining Room",
					"Hall",
					"Kitchen",
					"Library",
					"Lounge",
					"Study" 
			};
	
	private boolean firstTimeMoving;
	private int remainingMoves;
	private Player currentPlayer;
	
	private Board board;
	private final Dice dice = new Dice();
	/**
	 * One cycle of the Cluedo game.
	 * Contains all the active players
	 */
	private Turn<Player> turn;
	/**
	 * Iterator that contains all human players in
	 * the game (i.e. eliminated and active players)
	 */
	private Turn<Player> allHumanIterator;
	
	private Set<Player> allPlayers;
	/**
	 * All the human players in the game
	 * Does not include eliminated players from the game.
	 */
	private List<Player> activeHumanPlayers;
	private Map<Player,CaseFile> playerToCasefile;
	
	/**
	 * The cards that each player has in their hand.
	 * Every player has different cards to each other
	 */
	private Map<Player,Set<Card>> playerHand;
	/**
	 * These are the cards leftover after evenly
	 * distributing the cards to all the players.
	 * Does not contain the answer cards.
	 * Every player will be able to view these cards.
	 * This Set may be empty
	 */
	private Set<Card> extraCards;
	/**
	 * Contains one of each type of card (SuspectCard, WeaponCard, RoomCard).
	 * These are the cards that the player needs to guess correctly to win the game
	 */
	private CaseFile answer; 
	
	private Map<Cell,Room> cellToRoom;
	private Map<Player,Boolean> isTransferred;
	private Map<Player,Room> playerToRoom; 
	
	//FIXME Need weapons?
	private List<Weapon> weapons;
	
	//Static initializer
		{
			SUSPECT_NAMES.put("Miss Scarlett", 0);
			SUSPECT_NAMES.put("Colonel Mustard", 1);
			SUSPECT_NAMES.put("Mrs. White", 2);
			SUSPECT_NAMES.put("Reverend Green", 3);
			SUSPECT_NAMES.put("Mrs. Peacock", 4);
			SUSPECT_NAMES.put("Professor Plum", 5);
		}
	
	
	//TODO Game class methods
	public Game(int numPlayers, List<Piece> playerTokens, List<Piece> weaponTokens, List<Cell> cells, 
			List<Displayable> suspectCardFaces, List<Displayable> weaponCardFaces, List<Displayable> roomCardFaces)
	{
		if(numPlayers < MIN_HUMAN_PLAYERS || numPlayers > MAX_HUMAN_PLAYERS)
		{
			throw new IllegalArgumentException("Must have between: " + MIN_HUMAN_PLAYERS + " and " + MAX_HUMAN_PLAYERS + " human players");
		}
		this.allPlayers = createPlayers(playerTokens);
		this.activeHumanPlayers = createHumanPlayers(numPlayers);
		turn = new Turn<Player>(activeHumanPlayers);
		allHumanIterator =  new Turn<Player>(activeHumanPlayers);
		this.weapons = createWeapons(weaponTokens);
		//Cards
		Set<SuspectCard> suspectCards = createSuspectCards(suspectCardFaces);
		Set<WeaponCard> weaponCards = createWeaponCards(weaponCardFaces);
		Set<RoomCard> roomCards = createRoomCards(roomCardFaces);
		answer = createCaseFiles(suspectCards, weaponCards, roomCards);
		extraCards = distributeCards(suspectCards, weaponCards, roomCards);
		isTransferred = new HashMap<Player,Boolean>();
		playerToRoom = new HashMap<Player,Room>();
		//TODO Game - create cells
	}
	
	/**
	 * Create all the players in the Cluedo game
	 * @param playerTokens
	 * @return All the players in the Cluedo game
	 */
	private Set<Player> createPlayers(List<Piece> playerTokens)
	{
		Set<Player> players = new TreeSet<Player>();
		int i = 0;
		for(String playerName : SUSPECT_NAMES.keySet())
		{
			assert i < MAX_PLAYERS : "Exceeded the total number of players";
			Player p = new Player (playerName,playerTokens.get(i));
			players.add(p);
			i++;
		}
		return players;
	}
	/**
	 * Select randomly the characters the human players will play as.
	 * The starting character (and therefore player) is selected randomly.
	 * The order of play is then based off the starting position
	 * of the other characters in a clockwise order.
	 * This order is: 
	 * Miss Scarlett, Colonel Mustard, Mrs. White, Reverend Green, Mrs. Peacock, Professor Plum
	 * 
	 * @param numPlayers - The number of players playing Cluedo
	 * @return The characters that the human players will play as
	 */
	private List<Player> createHumanPlayers(int numPlayers)
	{
		assert allPlayers != null : "Must create all player objects first";
		assert allPlayers.size() == MAX_PLAYERS : "Must contain all players in the game";
		Player[] playerArr = new Player[MAX_HUMAN_PLAYERS];
		Player startingPlayer = null;
		//Generate random players
		for(Player randPlayer : allPlayers)
		{
			if(startingPlayer == null)
			{
				startingPlayer = randPlayer;
				playerArr[0] = startingPlayer;
			}
			else
			{
			 // Put added players in a clockwise order based off the starting player
				int startOrder = SUSPECT_NAMES.get(startingPlayer.getName());
				int playerOrder = SUSPECT_NAMES.get(randPlayer.getName());
				int index = playerOrder > startOrder ? playerOrder - startOrder : playerOrder + startOrder;
				playerArr[index] = randPlayer;
			}
			
		}
		List<Player> players = new ArrayList<Player>();
		//Remove all null references in the array to put in the list
		for(Player sortPlayer : playerArr)
		{
			if(sortPlayer != null)
			{
				players.add(sortPlayer);
			}
		}
		return players;
	}
	/**
	 * Create the weapons in the Cluedo Game
	 * @param weaponTokens 
	 * @return All the weapons in the Cluedo Game
	 */
	private List<Weapon> createWeapons(List<Piece> weaponTokens)
	{
		List<Weapon> weapons = new ArrayList<Weapon>();
		for(int i = 0; i < NUM_WEAPONS; i++)
		{
			Weapon w = new Weapon(WEAPON_NAMES[i],weaponTokens.get(i));
			weapons.add(w);
		}
		return weapons;
	}
	/**
	 * Create the weapon cards in the Cluedo Game
	 * @param weaponCardFaces
	 * @return All the weapon cards in the Cluedo Game
	 */
	private Set<WeaponCard> createWeaponCards(List<Displayable> weaponCardFaces)
	{
		Set<WeaponCard> weaponCards = new TreeSet<WeaponCard>();
		for(int i = 0; i < NUM_WEAPONS; i++)
		{
			WeaponCard card = new WeaponCard(WEAPON_NAMES[i],weaponCardFaces.get(i));
			weaponCards.add(card);
		}
		return weaponCards;
	}
	/**
	 * Create the suspect cards in the Cluedo Game
	 * @param suspectCardFaces
	 * @return All the suspect cards in the Cluedo Game
	 */
	private Set<SuspectCard> createSuspectCards(List<Displayable> suspectCardFaces)
	{
		Set<SuspectCard> suspectCards = new TreeSet<SuspectCard>();
		int i = 0;
		for(String suspectName : SUSPECT_NAMES.keySet())
		{
			assert i < MAX_PLAYERS : "Exceeded the total number of suspects";
			SuspectCard p = new SuspectCard(suspectName,suspectCardFaces.get(i));
			suspectCards.add(p);
			i++;
		}
		return suspectCards;
	}
	/**
	 * Create the room cards in the Cluedo Game
	 * @param roomCardFaces
	 * @return All the room cards in the Cluedo Game
	 */
	private Set<RoomCard> createRoomCards(List<Displayable> roomCardFaces)
	{
		Set<RoomCard> roomCards = new TreeSet<RoomCard>();
		for(int i = 0; i < NUM_ROOMS; i++)
		{
			RoomCard card = new RoomCard(ROOM_NAMES[i],roomCardFaces.get(i));
			roomCards.add(card);
		}
		return roomCards;
	}
	/**
	 * Creates a CaseFile for each human player
	 * and the answer CaseFile
	 * @param suspectCards - all the suspect cards
	 * @param weaponCards - all the weapon cards
	 * @param roomCards - all the room cards
	 * @return The CaseFile for the answer of the game
	 */
	private CaseFile createCaseFiles(Set<SuspectCard> suspectCards,Set<WeaponCard> weaponCards,Set<RoomCard> roomCards)
	{
		for(Player player : activeHumanPlayers)
		{
			playerToCasefile.put(player, new CaseFile(suspectCards,weaponCards,roomCards));
		}
		SuspectCard answerSuspect = null;
		for(SuspectCard suspect : suspectCards)
		{
			answerSuspect = suspect;
			suspectCards.remove(suspect);
			break;
		}
		WeaponCard answerWeapon = null;
		for(WeaponCard weapon : weaponCards)
		{
			answerWeapon = weapon;
			weaponCards.remove(weapon);
			break;
		}
		RoomCard answerRoom = null;
		for(RoomCard room : roomCards)
		{
			answerRoom = room;
			roomCards.remove(room);
			break;
		}
		return new CaseFile(answerSuspect, answerWeapon, answerRoom);
	}
	
	/**
	 * Distribute the remaining cards (all the cards except the answer cards)
	 * to the players.
	 * The cards that each player has is removed from their CaseFile.
	 * 
	 * @param suspectCards - all the suspect cards
	 * @param weaponCards - all the weapon cards
	 * @param roomCards - all the room cards
	 * @return The cards that were leftover after evenly distributing the cards.
	 */
	private Set<Card> distributeCards(Set<SuspectCard> suspectCards,Set<WeaponCard> weaponCards,Set<RoomCard> roomCards)
	{
		Set<Card> extra = new TreeSet<Card>();
		Set<Card> allCards = new TreeSet<Card>();
		allCards.addAll(suspectCards);
		allCards.addAll(weaponCards);
		allCards.addAll(roomCards);
		int numPlayers = activeHumanPlayers.size();
		int numExtra = allCards.size() % numPlayers;
		int numCards = (allCards.size() - numExtra) / numPlayers; //Number of cards each player will get
		Set<Card> cardsForPlayer = new TreeSet<Card>();
		for(Card card : allCards)
		{
			//All cards evenly distributed, put the rest of the cards in extra
			if(numPlayers == 0){
				extra.add(card);
				continue;
			}
			//Remove the card from the player's CaseFile
			Player player = activeHumanPlayers.get(numPlayers-1);
			CaseFile caseFile = playerToCasefile.get(player);
			caseFile.removeCard(card);
			cardsForPlayer.add(card);
			numCards--;
			if(numCards == 0)
			{
				//Put the cards in for a human player
				playerHand.put(player, cardsForPlayer);
				//Go to the next player
				numPlayers--;
				cardsForPlayer = new TreeSet<Card>();
			}
		}
		return extra;
		
	}
	//TODO implement Game - takeExit.
	public Cell takeExit(Cell c)
	{
		return null;
	}
	
	/**
	 * Checks if the player can move by calling the move
	 * method in the Board class. 
	 * Assigns new player’s moves using rollDice()
	 * @param direction
	 * @return
	 */
	public Cell move(Direction direction)
	{
		if(remainingMoves <= 0)
		{
			if(firstTimeMoving)
			{
				rollDice();
				firstTimeMoving = false;
			}
			else
			{
				/*throw new InvalidMoveException();*/
			}
		}
		Cell newPos = board.move(currentPlayer.getPiece(),direction);
		remainingMoves--;
		return newPos;
	}
	
	/**
	 * A suggestion is when a player suggests what the murder weapon, murderer and 
	 * murder room is.
	 * This is so that the player can determine what cards were part of the murder
	 * by a process of elimination.
	 * The current player can make a suggestion when they are in a room.
	 * The room the player is in is part of the suggestion.
	 * 
	 * Players in a clockwise order to the current player must disprove the suggestion
	 * if they have at least one of the cards in their hand. The suggestion ends
	 * when one or none of the players can disprove the suggestion
	 * 
	 * @param weaponCard that is suggested is part of the murder (in the answer CaseFile)
	 * @param suspectCard that is suggested is part of the murder (in the answer CaseFile)
	 * @return One player with the card or cards they have that can disprove the suggestion
	 */
	public Map<Player,Set<Card>> makeSuggestion(WeaponCard weaponCard, SuspectCard suspectCard)
	{
		allHumanIterator = new Turn<Player>(allHumanIterator.getList(),turn.getPos());
		Player player = allHumanIterator.next();
		Map<Player,Set<Card>> disprover = new HashMap<Player,Set<Card>>();
		while(player != currentPlayer)
		{
			Set<Card> cards = playerHand.get(player);
			if(cards.contains(weaponCard))
			{
				cards.add(weaponCard);
			}
			if(cards.contains(suspectCard))
			{
				cards.add(suspectCard);
			}
			if(!cards.isEmpty()){
				disprover.put(player,cards);
				return disprover;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * An accusation is when the player guesses what the 
	 * murder weapon, murderer and the murder room is.
	 * Any player can make an accusation.
	 * The cards must correctly match the cards in the answer CaseFile
	 * If the player is incorrect, the player is eliminated from the game
	 * but their cards are still hidden from active players and are still used
	 * during suggestions.
	 * 
	 * @param player - The player making the accusation
	 * @param weaponCard - The suspected murder weapon
	 * @param roomCard - The suspected room of the murder
	 * @param suspectCard - The suspected murderer
	 * @return Whether the player was successful in making their accusation
	 */
	public boolean makeAccusation(Player player, WeaponCard weaponCard, RoomCard roomCard, SuspectCard suspectCard)
	{
		List<Player> players = getActivePlayers();
		CaseFile accusation = new CaseFile(suspectCard,weaponCard,roomCard);
		if(accusation.equals(answer))
		{//Game over, the player won!
			return true;
		}
		else if(players.size() == 1)
		{//Last player in the game failed.
			return false;
		}
		else
		{
			//Remove player from the game
			players.remove(player);
			int pos = turn.getPos();
			int currentPlayerPos = SUSPECT_NAMES.get(currentPlayer.getName());
			int removedPos = SUSPECT_NAMES.get(player.getName());
			if(removedPos <= currentPlayerPos)
			{
				pos--;
				if(removedPos == currentPlayerPos)
				{
					//Go to next player if it was the current player who failed to make the accusation
					nextTurn();
					//Remove player from the active players
					turn = new Turn<Player>(players,pos);
				}
			}
			return false;
		}
	}
	
	public Player nextTurn()
	{
		if(remainingMoves != 0){
			//throw new CurrentPlayerHasRemainingMovesException();
		}
		currentPlayer = turn.next();
		firstTimeMoving = true;
		return currentPlayer;
	}
	
	
	/**
	 * Checks if the current player is in the room
	 * @return true if the current player is in a room, false otherwise
	 */
	public boolean isInRoom()
	{
		return playerToRoom.get(currentPlayer) != null;
	}
	/**
	 * Generate the number of moves for the current player 
	 * (when they first decide to move)
	 * The number of moves is between 2 to 12
	 */
	private void rollDice()
	{
		remainingMoves = dice.roll();
	}
	//Getters
	
	public List<Player> getActivePlayers()
	{
		return Collections.unmodifiableList(turn.list);
	}
	
	//TODO implement Game - getAvailable exits
		public List<Cell> getAvailableExits()
		{
			//Check if player is in a room
			 Room room = playerToRoom.get(currentPlayer);
			 if(room == null)
			 {
			 	//throw new InvalidMethodCall
			 }
			//Get all the cells in the room 
			 //Need to return unmodifiable list of cells
			 //return room.getExitCells();
			return null;
		}
	/**
	 * The number of moves the current player has left
	 * to make in their turn
	 * @return Remaining moves the current player can make
	 */
	public int getRemainingMoves() 
	{
		return remainingMoves;
	}
	
	/**
	 * Convenience method for the UI
	 * for example to print out whether a player
	 * entered a room
	 * @param cell - A room cell
	 * @return The room the cell is in
	 */
	public Room getRoom(Cell cell)
	{
		if(!cellToRoom.containsKey(cell))
		{
			throw new IllegalArgumentException("Cell must be in a room");
		}
		return cellToRoom.get(cell);
	}
	
	//TODO do description for Game - getPosition
	/**
	 * Used by the UI to read
	 * the state of pieces in the game
	 * @param piece
	 * @return The cell the piece is in 
	 */
	public Cell getPosition(Piece piece)
	{
		return board.getPosition(piece);
	}
	
	public List<Room> getRooms()
	{
		return null;
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
	public Set<Card> getExtraCards()
	{
		return Collections.unmodifiableSet(extraCards);
	}
	
	/**
	 * @return The weapon cards in the current player's CaseFile
	 */
	public Set<WeaponCard> getPlayerWeaponCards()
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableSet(casefile.getWeaponCards());
	}
	
	/**
	 * @return The room cards in the current player's CaseFile
	 */
	public Set<RoomCard> getPlayerRoomCards()
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableSet(casefile.getRoomCards());
	}
	
	/**
	 * @return The suspect cards in the current player's CaseFile
	 */
	public Set<SuspectCard> getPlayerSuspectCards()
	{
		CaseFile casefile = playerToCasefile.get(currentPlayer);
		return Collections.unmodifiableSet(casefile.getSuspectCards());
	}

	/**
	 * A CaseFile is either an answer case file, 
	 * or a player’s case file, although there is no technical distinction.
	 * Cards may be removed from a player's CaseFile throughout the game
	 * 
	 */
	private class CaseFile
	{
		private Set<SuspectCard> suspectCards;
		private Set<WeaponCard> weaponCards;
		private Set<RoomCard> roomCards;
		public CaseFile(Set<SuspectCard> suspectCards, Set<WeaponCard> weaponCards, Set<RoomCard> roomCards) 
		{
			if(suspectCards.size() == 0 || weaponCards.size() == 0 || roomCards.size() == 0)
			{
				throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
			}
			this.roomCards = roomCards;
			this.suspectCards = suspectCards;
			this.weaponCards = weaponCards;
		}
		public CaseFile(SuspectCard suspectC, WeaponCard weaponC, RoomCard roomC)
		{
			if(suspectC == null || weaponC == null || roomC == null)
			{
				throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
			}
			suspectCards = new TreeSet<SuspectCard>();
			suspectCards.add(suspectC);
			weaponCards = new TreeSet<WeaponCard>();
			weaponCards.add(weaponC);
			roomCards = new TreeSet<RoomCard>();
			roomCards.add(roomC);
		}
	
		public void removeCard(Card card)
		{	if(card instanceof SuspectCard){
				suspectCards.remove(card);
			}
			else if(card instanceof RoomCard)
			{
				roomCards.remove(card);
			}
			else if(card instanceof WeaponCard){
				weaponCards.remove(card);
			}
		}

		public Set<SuspectCard> getSuspectCards() {
			return suspectCards;
		}

		public Set<WeaponCard> getWeaponCards() {
			return weaponCards;
		}

		public Set<RoomCard> getRoomCards() {
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
	
	/**
	 * The dice used to roll moves in Cluedo	
	 * @author Janice
	 *
	 */
	private class Dice
	{
		/**
		 * Simulates the roll of two six-sided die
		 * by generating a number between 2 to 12
		 * @return The dice roll
		 */
		public int roll()
		{
			int d1 = (int)(Math.random() * 7 + 1);
			int d2 = (int)(Math.random() * 7 + 1);
			return d1 + d2;
		}
	}
	
	/**
	 * Class modified from Stack Overflow: https://stackoverflow.com/questions/20343265/looping-data-structure-in-java
	 * Iterator that loops around in cycles so that it will never end
	 * @param <E>
	 */
	private class Turn<E> implements Iterator<E>, Cloneable
	{
	    private final List<E> list;
	    private int pos;

	    public Turn(List<E> list)
	    {
	    	ensureNotNull(list);
	    	ensureNotEmpty(list); // A turn must have at least one player.
	    	ensureNotContainNullItem(list); // Turns may not contain null players.
	    	
	        this.list = list;
	        pos = 0;
	    }
	    public Turn(List<E> list, int pos)
	    {
	    	ensureNotNull(list);
	    	ensureNotEmpty(list); // A turn must have at least one player.
	    	ensureNotContainNullItem(list); // Turns may not contain null players.
	    	
	        this.list = list;
	        this.pos = pos;
	    }

		public boolean hasNext()
	    {
	    	return list.size() >= 1;
	    }

	    public E next()
	    {
	    	if (!hasNext())
	    	{
	    		throw new IllegalStateException("No next item");
	    	}
	        E nextItem = list.get(pos);
	        pos = (pos + 1) % list.size();
	        return nextItem;
	    }
	   
	    public void remove()
	    {
	         throw new RuntimeException("Cannot remove items from iterator");
	    }
	    

	    // These methods throw illegal argument exceptions if their respective conditions aren't met.
	    // The order of calls are important, as (e.g. ensureNotEmpty(list) assumes that the list is non null).
	    private void ensureNotNull(List<E> list)
		{
			if (list == null)
			{
				throw new IllegalArgumentException("List must be non null");
			}
		}
	    
	    private void ensureNotEmpty(List<E> list)
		{
			if (list.size() <= 0)
			{
				throw new IllegalArgumentException("List must be non null, with at least one item");
			}
		}
	    
	    private void ensureNotContainNullItem(List<E> list)
		{
	    	for (E item : list) 
	    	{
	    		if (item == null)
	    		{
	    			throw new IllegalArgumentException("List may not contain null items");
	    		}
	    	}
		}

		public int getPos() {
			return pos;
		}
		public List<E> getList() {
			return list;
		}
	}
}