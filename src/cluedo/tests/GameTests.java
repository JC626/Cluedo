package cluedo.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cluedo.game.Game;
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

public class GameTests {

	private Game game;
	private final int[] STARTINGPOSITION = new int[]{
			7, 24, 0, 17, 9, 0, 14, 0, 23, 6, 23, 19};
	private final String[] SUSPECT_NAMES = new String[]{ "Miss Scarlett","Colonel Mustard",
			 "Mrs. White","Reverend Green","Mrs. Peacock","Professor Plum"};
	private Map<String, Integer> SUSPECT_ORDER;
	private List<Piece> playerTokens;
	private List<Piece> weaponTokens;
	private List<Displayable> suspectCardFaces;
	private List<Displayable> weaponCardFaces;
	private List<Displayable> roomCardFaces;
	 public void setupSuspectOrder(){
		 SUSPECT_ORDER = new HashMap<String, Integer>();
		 SUSPECT_ORDER.put("Miss Scarlett", 0);
		 SUSPECT_ORDER.put("Colonel Mustard", 1);
		 SUSPECT_ORDER.put("Mrs. White", 2);
		 SUSPECT_ORDER.put("Reverend Green", 3);
		 SUSPECT_ORDER.put("Mrs. Peacock", 4);
		 SUSPECT_ORDER.put("Professor Plum", 5);
	 }
	
	@Before
	public void setup()
	{
		setupSuspectOrder();
		playerTokens = createPlayerTokens();
		weaponTokens = createWeaponTokens();
		suspectCardFaces = createSuspectCards();
		weaponCardFaces = createWeaponCards();
		roomCardFaces = createRoomCards();
	 	setupGame(6);
	}
	public void setupGame(int numPlayers)
	{
		game = new Game(numPlayers,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
	}
	private List<Piece> createPlayerTokens()
	{
		List<Piece> players = new ArrayList<Piece>();

		for (int player = 0; player < Game.MAX_HUMAN_PLAYERS; player++)
		{
			Piece p = new Piece(){
				public void display(){
					
				}
			};
			players.add(p);
		}

		return players;
	}

	private List<Piece> createWeaponTokens()
	{
		List<Piece> weapons = new ArrayList<Piece>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			Piece p = new Piece(){
				public void display(){
					
				}
			};
			weapons.add(p);
		}

		return weapons;
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
	/**
	 * Using reflection to set remainingMoves to zero each time
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void resetRemainingMoves()
	{
		Field remainingMoves = null;
		try {
			remainingMoves = Game.class.getDeclaredField("remainingMoves");
			remainingMoves.setAccessible(true);
			remainingMoves.set(game, 0);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private Player nextPlayer()
	{
		resetRemainingMoves();
		return game.nextTurn();
	}

	@Test
	public void testStartingPositionWeaponsInDifferentRooms()
	{
		Set<Room> rooms = new HashSet<Room>();
		List<Weapon> weapons = game.getWeapons();
		for(Weapon w : weapons)
		{
			Cell cell = game.getPosition(w.getPiece());
			assertNotNull("Weapon piece must be on a cell",cell);
			try
			{
				Room room = game.getRoom(cell);
				if(rooms.contains(room))
				{
					fail("Cannot have a weapon in the same room as another weapon");
				}
				rooms.add(room);
			}
			catch(IllegalArgumentException e)
			{
				fail("Weapon piece must be in a room");
			}
		}
	}
	@Test 
	public void testStartingPlayerPosition()
	{
		List<Player> players = game.getAllPlayers();
		Cell[][] cells = game.getCells();
		int pCount = 0;
		for(int i = 0; i < STARTINGPOSITION.length;i+=2)
		{
			int x = STARTINGPOSITION[i];
			int y = STARTINGPOSITION[i+1];
			Player player = players.get(pCount);
			assertEquals("Player should match order",player.getName(), SUSPECT_NAMES[pCount]);
			Cell cell = game.getPosition(player.getPiece());
			assertEquals(cell, cells[x][y]);
			pCount++;
		}
	} 
	@Test
	public void testPlayerOrder() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		//6 players
		Player startPlayer = game.nextTurn();
		assertNotNull("Should not be given null startPlayer",startPlayer);
		int startOrderNum = SUSPECT_ORDER.get(startPlayer.getName());
		resetRemainingMoves();
		Player currentPlayer = null;
		while(currentPlayer != startPlayer)
		{
			startOrderNum++;
			if(startOrderNum >= Game.MAX_HUMAN_PLAYERS)
			{
				startOrderNum = 0;
			}
			resetRemainingMoves();
			currentPlayer = game.nextTurn();
			if(SUSPECT_ORDER.get(currentPlayer.getName()) != startOrderNum)
			{
				fail("Player order not enforced");
			}

		}
	}
	@Test 
	public void invalidNumberPlayers()
	{
		try{
			game = new Game(2,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
		}
		catch(IllegalArgumentException e){}
		try{
			game = new Game(7,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
		}
		catch(IllegalArgumentException e){}
	}
	
	@Test
	public void testPlayerHand()
	{
		Player startPlayer = null;
		Player currentPlayer = nextPlayer();
		Set<Card> allCards = new HashSet<Card>();
		while(currentPlayer != startPlayer)
		{
			if(startPlayer == null)
			{
				startPlayer = currentPlayer;
			}
			List<RoomCard> roomCards = game.getPlayerRoomCards();
			List<SuspectCard> suspectCards = game.getPlayerSuspectCards();
			List<WeaponCard> weaponCards = game.getPlayerWeaponCards();
			allCards.addAll(roomCards);
			allCards.addAll(suspectCards);
			allCards.addAll(weaponCards);
			int sizeAllTogether = roomCards.size()+ suspectCards.size() + weaponCards.size();
			assertEquals("RoomSize " + roomCards.size() +  "SuspectSize " + suspectCards.size() + "WeaponSize " +  weaponCards.size(), 18, sizeAllTogether);
			currentPlayer = nextPlayer();
		}
		int numCards =  Game.NUM_WEAPONS + Game.NUM_ROOMS + Game.MAX_PLAYERS;
		assertEquals(numCards,allCards.size());
	}
	@Test
	public void testExtraCards()
	{
		setupGame(5);
		assertEquals(3,game.getExtraCards().size());
		setupGame(4);
		assertEquals(2,game.getExtraCards().size());

	}
}
