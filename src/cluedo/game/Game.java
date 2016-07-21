package cluedo.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cluedo.board.Board;
import cluedo.model.Cell;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.model.Room;
import cluedo.model.Weapon;
import cluedo.utility.Heading.Direction;

public class Game
{
	//TODO make Turn and Dice class
	public static final int MIN_HUMAN_PLAYERS = 3;
	public static final int MAX_HUMAN_PLAYERS = 6;
	
	private boolean firstTimeMoving;
	private int remainingMoves;
	private Player currentPlayer;
	
	private Board board;
	private final Dice dice = new Dice();
	//private final Turn<Player> turn;
	private Map<Player,Boolean> isTransferred;
	/*private Map<Player,CaseFile> playerToCasefile;
	private Map<Player,Set<Card>> playerHand;
	private CaseFile answer;*/
	private Map<Cell,Room> cellToRoom;
	private Map<Player,Room> playerToRoom; 
	
	//TODO Game class methods
	//TODO Game constructor	
	public Game()
	{
		//turn = new Turn<Player>(null);
	}
	
	//TODO implement Game - takeExit
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
		//Cell newPos = board.move(dir,currentPlayer.getPiece());
		remainingMoves--;
		return null;
		//return newPos;
		
	}
	/*public Map<Player,Iterator<Card>> makeSuggestion(WeaponCard weaponC, SuspectCard suspectC)
	{
		
	}*/
	/*public boolean makeAccusation(Player player, WeaponCard weaponC, RoomCard roomC, SuspectCard suspectC)
	{
		Iterator<Player> players = getActivePlayers();
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
	
	/*public Player nextTurn()
	{
		if(remainingMoves != 0){
			throw new CurrentPlayerHasRemainingMovesException();
		}
		currentPlayer = turn.next();
		firstTimeMoving = true;
		return null;
	}
	*/
	
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
	
	public Iterator<Player> getActivePlayers()
	{
		return null;
		//return new Turn<Player>(players); 
	}
	//TODO implement Game - getAvailable exits
		public Iterator<Cell> getAvailableExits()
		{
			//Check if player is in a room
			 Room r = playerToRoom.get(currentPlayer);
			 if(r == null)
			 {
			 	//throw new InvalidMethodCall
			 }
			//Get all the cells in the room 
			return null;
		}
	
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
	
	public Iterator<Room> getRooms()
	{
		return null;
	}
	public Iterator<Weapon> getWeapons()
	{
		return null;
	}
	public Iterator<Cell> getCells()
	{
		return null;
	}
	/*public Iterator<Card> getPlayer()
	{
		return null;
	}*/


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