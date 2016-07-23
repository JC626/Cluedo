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

public class Game
{
	public static void main(String args[]){
		
	}
	//Constants
	public static final int MIN_HUMAN_PLAYERS = 3;
	public static final int MAX_HUMAN_PLAYERS = 6;
	public static final int MAX_PLAYERS = 6;
	public static final int NUM_WEAPONS = 6;
	public static final int NUM_ROOMS = 6;
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
	private final Turn<Player> turn;
	
	private Set<Player> allPlayers;
	private List<Player> humanPlayers;
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

	private List<Weapon> weapons;
	//FIXME should Game have a list of cells?
	//private List<Cell> cells;
	
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
	//TODO Game - create cells
	public Game(int numPlayers, List<Piece> playerTokens, List<Piece> weaponTokens, List<Displayable> cellDisplayables, 
			List<Displayable> suspectCardFaces, List<Displayable> weaponCardFaces, List<Displayable> roomCardFaces)
	{
		if(numPlayers < MIN_HUMAN_PLAYERS || numPlayers > MAX_HUMAN_PLAYERS)
		{
			throw new IllegalArgumentException("Must have between: " + MIN_HUMAN_PLAYERS + " and " + MAX_HUMAN_PLAYERS + " human players");
		}
		this.allPlayers = createPlayers(playerTokens);
		this.humanPlayers = createHumanPlayers(numPlayers);
		turn = new Turn<Player>(humanPlayers);
		this.weapons = createWeapons(weaponTokens);
		//Cards
		Set<SuspectCard> suspectCards = createSuspectCards(suspectCardFaces);
		Set<WeaponCard> weaponCards = createWeaponCards(weaponCardFaces);
		Set<RoomCard> roomCards = createRoomCards(roomCardFaces);
		answer = createCaseFiles(suspectCards, weaponCards, roomCards);
		
	}
	
	/**
	 * Create all the players in the Cluedo game
	 * @param playerTokens
	 * @return all the players in the Cluedo game
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
	 * @return all the weapons in the Cluedo Game
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
	 * @return all the weapon cards in the Cluedo Game
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
	 * @return all the suspect cards in the Cluedo Game
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
	 * @return all the room cards in the Cluedo Game
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
	 * and the answer Casefile
	 * @param suspectCards - all the suspect cards
	 * @param weaponCards - all the weapon cards
	 * @param roomCards - all the room cards
	 * @return the Casefile for the answer of the game
	 */
	private CaseFile createCaseFiles(Set<SuspectCard> suspectCards,Set<WeaponCard> weaponCards,Set<RoomCard> roomCards)
	{
		for(Player player : humanPlayers)
		{
			playerToCasefile.put(player, new CaseFile(suspectCards,weaponCards,roomCards));
		}
		Set<SuspectCard> answerSuspect = new TreeSet<SuspectCard>();
		for(SuspectCard suspect : suspectCards)
		{
			answerSuspect.add(suspect);
			suspectCards.remove(suspect);
			break;
		}
		Set<WeaponCard> answerWeapon = new TreeSet<WeaponCard>();
		for(WeaponCard weapon : weaponCards)
		{
			answerWeapon.add(weapon);
			weaponCards.remove(weapon);
			break;
		}
		Set<RoomCard> answerRoom = new TreeSet<RoomCard>();
		for(RoomCard room : roomCards)
		{
			answerRoom.add(room);
			roomCards.remove(room);
			break;
		}
		return new CaseFile(answerSuspect, answerWeapon, answerRoom);
	}
	
	//TODO implement Game - takeExit.
	public Cell takeExit(Cell c)
	{
		return null;
	}
	
	public Cell move(Direction dir)
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
		//Cell newPos = board.move(currentPlayer.getPiece(),dir);
		remainingMoves--;
		return null;
		//return newPos;
		
	}
	/*public Map<Player,List<Card>> makeSuggestion(WeaponCard weaponC, SuspectCard suspectC)
	{
		
	}*/
	
	/*public boolean makeAccusation(Player player, WeaponCard weaponC, RoomCard roomC, SuspectCard suspectC)
	{
		List<Player> players = getActivePlayers();
		CaseFile accusation = new CaseFile(weaponC,roomC,suspectC)
		if(accusation.equals(answer))
		{
			//game over
			return true;
		}
		else
		{
			//remove player from rotation
			if(currentPlayer.equals(player))
			{
				//FIXME go to next player if it was the currentPlayer making the accusation?
				nextTurn();
			}
			return false;
		}
	}*/
	
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
		//TODO figure out which human players are still active
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
	 * @return remaining moves the current player can make
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
	 * @return the cell the piece is in 
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
	 * @return all the weapons in the Cluedo game
	 */
	public List<Weapon> getWeapons()
	{
		return weapons;
	}
	public List<Cell> getCells()
	{
		return null;
	}
	/**
	 * @return the cards not distributed to players or in the answer.
	 */
	public Set<Card> getExtraCards()
	{
		return extraCards;
	}
	/*public List<WeaponCard> getPlayerWeaponCards()
	{
		return null;
	}
	public List<RoomCard> getPlayerRoomCards()
	{
		return null;
	}
	public List<SuspectCard> getPlayerSuspectCards()
	{
		return null;
	}*/

	/**
	 * A Casefile is either an answer case file, 
	 * or a player’s case file, although there is no technical distinction.
	 * Cards may be added to a player's casefile throughout the game
	 *
	 * 
	 */
	private class CaseFile
	{
		private Set<SuspectCard> suspectCards;
		private Set<WeaponCard> weaponCards;
		private Set<RoomCard> roomCards;
		public CaseFile(Set<SuspectCard> suspectCards, Set<WeaponCard> weaponCards, Set<RoomCard> roomCards) {
			if(roomCards.size() == 0 || suspectCards.size() == 0 || weaponCards.size() == 0)
			{
				throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
			}
			this.roomCards = roomCards;
			this.suspectCards = suspectCards;
			this.weaponCards = weaponCards;
		}
		public void removeRoomCard(RoomCard card)
		{
			roomCards.remove(card);
		}
		public void removeWeaponCard(WeaponCard card)
		{
			weaponCards.remove(card);
		}
		public void removeSuspectCard(SuspectCard card)
		{
			suspectCards.remove(card);
		}
		
	}
	
	//TODO check Dice
	
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
	
	
	//TODO check Turn class
	// Class modified from Stack Overflow: https://stackoverflow.com/questions/20343265/looping-data-structure-in-java
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
	}
}