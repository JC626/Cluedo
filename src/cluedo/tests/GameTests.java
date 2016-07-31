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

import cluedo.board.Board;
import cluedo.exceptions.HasRemainingMovesException;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.exceptions.NoAvailableExitException;
import cluedo.game.Game;
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
	 * Using reflection to set remainingMoves to zero for testing purposes
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
	/**
	 * Using reflection to set remainingMoves to a specific number for testing purposes
	 */
	private void setRemainingMoves(int moveNum)
	{
		Field remainingMoves = null;
		try {
			remainingMoves = Game.class.getDeclaredField("remainingMoves");
			remainingMoves.setAccessible(true);
			remainingMoves.set(game, moveNum);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Use reflection to set gameOver to true
	 * for testing purposes
	 */
	private void causeGameOver()
	{
		Field gameOver = null;
		try {
			gameOver = Game.class.getDeclaredField("gameOver");
			gameOver.setAccessible(true);
			gameOver.set(game, true);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @return The next player in the turn rotation, regardless of remaining moves
	 */
	private Player nextPlayer()
	{
		resetRemainingMoves();
		return game.nextTurn();
	}
	/**
	 * Switch the current player in the game to this character
	 * @param playerName - Player wanted as the current player
	 * @return The current player
	 */
	private Player getSpecificPlayer(String playerName)
	{
		if(game.isGameOver())
		{
			return null;
		}
		Player currentPlayer = game.getCurrentPlayer();
		if(!SUSPECT_ORDER.containsKey(playerName))
		{
			fail("Invalid playerName put into getSpecificPlayer name method. Fix corresponding test");
		}
		int pCount = 0;
		while(!currentPlayer.getName().equals(playerName))
		{
			if(pCount > 6)
			{
				System.out.println("Player not there");
				return null;
			}
			currentPlayer = nextPlayer();
			pCount++;
		}
		return currentPlayer;
	}
	
	/**
	 * Put Mrs. Peacock in a room from her
	 *  starting position
	 * @throws InvalidMoveException 
	 */
	private void putPeacockInRoom() throws InvalidMoveException
	{
		getSpecificPlayer("Mrs. Peacock");
		setRemainingMoves(12);
		game.move(Direction.West);
		game.move(Direction.West);
		game.move(Direction.West);
		game.move(Direction.West);
		game.move(Direction.West);
		game.move(Direction.North);
		game.move(Direction.North);
	}
	/**
	 * Used to ensure that the accusation will fail by 
	 * NOT picking this card
	 * @return The roomCard from the answer
	 */
	private RoomCard getAnswerRoomCard()
	{
		Field answerField = null;
		CaseFile answer = null;
		try {
			answerField = Game.class.getDeclaredField("answer");
			answerField.setAccessible(true);
			answer = (CaseFile) answerField.get(game);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("Field access error");
		}
		assert answer != null;
		return answer.getRoomCards().get(0);
	}
	/**
	 * All weapon pieces start in different rooms
	 */
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
	/**
	 * All players are in their correct starting positions
	 */
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
	/**
	 * The order of player's is correct based on the
	 * the starting position of player's in a clockwise order.
	 */
	@Test
	public void testPlayerOrder() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		//6 players
		Player startPlayer = game.getCurrentPlayer();
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
			currentPlayer = nextPlayer();
			assertFalse(game.isInRoom());
			if(SUSPECT_ORDER.get(currentPlayer.getName()) != startOrderNum)
			{
				fail("Player order not enforced");
			}

		}
	}
	/**
	 * Must only have between MIN_HUMAN_PLAYERS and MAX_HUMAN_PLAYERS
	 * playing Cluedo game
	 */
	@Test 
	public void invalidNumberPlayers()
	{
		try{
			game = new Game(Game.MIN_HUMAN_PLAYERS-1,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
			fail("Cannot have less than " + Game.MIN_HUMAN_PLAYERS + " players");
		}
		catch(IllegalArgumentException e){}
		try{
			game = new Game(Game.MAX_HUMAN_PLAYERS+1,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
			fail("Cannot have over " + Game.MAX_HUMAN_PLAYERS + " players");
		}
		catch(IllegalArgumentException e){}
	}
	/**
	 * Ensure all cards are distributed and each player
	 * has the correct number of cards
	 */
	@Test
	public void testPlayerHand()
	{
		Player startPlayer = null;
		Player currentPlayer = game.getCurrentPlayer();
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
	public void testNumberPlayers()
	{
		setupGame(6);
		assertEquals(6, game.getAllPlayers().size());
		assertEquals(6, game.getActivePlayers().size());
		setupGame(5);
		assertEquals(5, game.getActivePlayers().size());
		setupGame(4);
		assertEquals(4, game.getActivePlayers().size());
		setupGame(3);
		assertEquals(3, game.getActivePlayers().size());
	}
	/**
	 * Ensure that there are extra cards from uneven distribution
	 */
	@Test
	public void testExtraCards()
	{
		setupGame(5);
		assertEquals(3,game.getExtraCards().size());
		setupGame(4);
		assertEquals(2,game.getExtraCards().size());
	}
	
	/**
	 * Cannot go to the next player if there are remaining moves
	 */
	@Test (expected = HasRemainingMovesException.class)
	public void testInvalidHasRemainingMoves()
	{
		assert game.getRemainingMoves() >= 2 && game.getRemainingMoves() <=12 : "remainingMoves should be between 2 and 12" + game.getRemainingMoves();
		game.nextTurn();
		assert game.getRemainingMoves() >= 2 && game.getRemainingMoves() <=12 : "remainingMoves should be between 2 and 12" + game.getRemainingMoves();
	}
	
	/**
	 * Cannot call methods if the game is over
	 */
	@Test
	public void testInvalidGameOver() throws InvalidMoveException, NoAvailableExitException
	{
		Player currentPlayer = game.getCurrentPlayer();
		assertFalse(game.isGameOver());
		causeGameOver();
		assertTrue(game.isGameOver());
		try
		{
			game.nextTurn();
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
		try
		{
			game.move(Direction.East);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
		
		WeaponCard weaponCard = (WeaponCard)game.getWeaponCards().get(0);
		SuspectCard suspectCard = (SuspectCard) game.getSuspectCards().get(0);
		RoomCard roomCard = (RoomCard) game.getRoomCards().get(0);
		
		try
		{
			assertFalse(game.canMakeSuggestion());
			game.makeSuggestion(weaponCard,suspectCard);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
		
		try
		{
			game.makeAccusation(currentPlayer,weaponCard,roomCard,suspectCard);
			fail("Cannot continue playing when the game is over");

		}
		catch(IllegalMethodCallException e){}
		try
		{
			assertFalse(game.canMakeSuggestion());
			game.makeSuggestion(weaponCard,suspectCard);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
		try
		{
			game.takeExit(game.getPosition(playerTokens.get(0)));
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
		try
		{
			game.getAvailableExits();
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e){}
	}
	/**
	 * Moving onto another cell (in the hallway)
	 */
	@Test
	public void testValidMove()
	{
		Player currentPlayer = game.getCurrentPlayer();
		int remainingMoves = game.getRemainingMoves();
		Cell pos = game.getPosition(currentPlayer.getPiece());
		int x = pos.getX();
		int y = pos.getY();
		Direction dir = Direction.West;
		if(x == 0)
		{
			dir = Direction.East;
		}
		else if(y == 0)
		{
			dir = Direction.South;
		}
		else if(y == Board.HEIGHT-1)
		{
			dir = Direction.North;
		}
		try {
			Cell cell = game.move(dir);
			switch(dir)
			{
			case North:
				assertEquals(y-1,cell.getY());
				break;
			case South:
				assertEquals(y+1,cell.getY());
				break;
			case East:
				assertEquals(x+1,cell.getX());
				break;
			case West:
				assertEquals(x-1,cell.getX());
				break;
			}
		} catch (InvalidMoveException e) {
			fail("Could not move " + e.getMessage());
		}
		assertEquals(remainingMoves-1, game.getRemainingMoves());
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidNullDirectionMove() throws InvalidMoveException
	{
		game.move(null);
		fail("Null should not be able to move a direction");
	}
	
	@Test(expected = InvalidMoveException.class)
	public void testInvalidNoRemainingMoves() throws InvalidMoveException
	{
		resetRemainingMoves();
		game.move(Direction.North);
		fail("Cannot move if there are no remaining moves");
	}
	/**
	 * Player moves on a cell they have already been on in the same turn
	 */
	@Test(expected = InvalidMoveException.class)
	public void testInvalidSamePathMove() throws InvalidMoveException
	{
		getSpecificPlayer("Mrs. Peacock");
		game.move(Direction.West);
		game.move(Direction.East);
		fail("Should not be able to reenter the same path");
	}
	
	@Test(expected = InvalidMoveException.class)
	public void testInvalidMoveAgainstWall() throws InvalidMoveException
	{
		getSpecificPlayer("Mrs. Peacock");
		game.move(Direction.North);
		fail("Should not be able to move through a wall");
	}
	@Test
	public void testEnterRoom() throws InvalidMoveException
	{
		putPeacockInRoom();
		Player peacock = getSpecificPlayer("Mrs. Peacock");
		Cell playerPos = game.getPosition(peacock.getPiece());
		assertTrue(game.isInRoom());
		assertEquals(0,game.getRemainingMoves());
		assertEquals("Conservatory",game.getRoom(playerPos).getName());
		assertEquals("Conservatory",game.getCurrentRoom().getName());
	}
	@Test
	public void testExitIntoHallway() throws InvalidMoveException, NoAvailableExitException
	{
		putPeacockInRoom();
		game.nextTurn();
		Player peacock = getSpecificPlayer("Mrs. Peacock");
		List<Cell> exits = game.getAvailableExits();
		assertFalse(exits.isEmpty());
		game.takeExit(exits.get(0)); //Not the secret passage
		assertFalse(game.isInRoom());
		Cell playerPos = game.getPosition(peacock.getPiece());
		assertEquals(18, playerPos.getX());
		assertEquals(5, playerPos.getY());
	}
	
	@Test(expected = InvalidMoveException.class)
	public void testInvalidReenterRoom() throws InvalidMoveException, NoAvailableExitException
	{
		putPeacockInRoom();
		game.nextTurn();
		Player peacock = getSpecificPlayer("Mrs. Peacock");
		List<Cell> exits = game.getAvailableExits();
		assertFalse(exits.isEmpty());
		game.takeExit(exits.get(0)); //Not the secret passage
		assertFalse(game.isInRoom());
		Cell playerPos = game.getPosition(peacock.getPiece());
		assertEquals(18, playerPos.getX());
		assertEquals(5, playerPos.getY());
		game.move(Direction.North);
		fail("Should not be able to reenter a room");
	}
	
	@Test
	public void testSecretPassage() throws InvalidMoveException, NoAvailableExitException
	{
		putPeacockInRoom();
		game.nextTurn();
		Player peacock = getSpecificPlayer("Mrs. Peacock");
		List<Cell> exits = game.getAvailableExits();
		assertFalse(exits.isEmpty());
		game.takeExit(exits.get(1));
		assertTrue(game.isInRoom());
		assertEquals(0, game.getRemainingMoves());
		Cell playerPos = game.getPosition(peacock.getPiece());
		assertEquals("Lounge",game.getRoom(playerPos).getName());
		assertTrue(game.canMakeSuggestion());
	}
	@Test
	public void testAccusationWin()
	{
		Field answerField = null;
		CaseFile answer = null;
		try {
			answerField = Game.class.getDeclaredField("answer");
			answerField.setAccessible(true);
			answer = (CaseFile) answerField.get(game);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("Field access error");
		}
		assert answer != null;
		RoomCard answerRoom = answer.getRoomCards().get(0);
		WeaponCard answerWeapon = answer.getWeaponCards().get(0);
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		assertTrue(game.makeAccusation(game.getCurrentPlayer(), answerWeapon, answerRoom,answerSuspect));
		assertTrue(game.isGameOver());
	}
	
	@Test
	public void testSelfAccusationFail()
	{
		int numPlayers = game.getActivePlayers().size();
		Player player = game.getCurrentPlayer();
		RoomCard answerRoom = getAnswerRoomCard();
		RoomCard guessRoom = (RoomCard) game.getRoomCards().get(0);
		if(guessRoom == answerRoom)
		{
			guessRoom = (RoomCard) game.getRoomCards().get(1);
		}
		WeaponCard guessWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard guessSuspect = (SuspectCard) game.getSuspectCards().get(0);
		assertFalse(game.makeAccusation(player, guessWeapon, guessRoom, guessSuspect));
		assertNotEquals(player,game.getCurrentPlayer());
		assertFalse(game.getActivePlayers().contains(player));
		assertNull(getSpecificPlayer(player.getName())); //Check player removed from turn rotation
		assertEquals(numPlayers - 1, game.getActivePlayers().size());
	}
	@Test
	public void testAccusationFail()
	{
		int numPlayers = game.getActivePlayers().size();
		Player player = game.getActivePlayers().get(0);
		if(player == game.getCurrentPlayer())
		{
			player = game.getActivePlayers().get(1);
		}
		RoomCard answerRoom = getAnswerRoomCard();
		RoomCard guessRoom = (RoomCard) game.getRoomCards().get(0);
		if(guessRoom == answerRoom)
		{
			guessRoom = (RoomCard) game.getRoomCards().get(1);
		}
		WeaponCard guessWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard guessSuspect = (SuspectCard) game.getSuspectCards().get(0);
		assertFalse(game.makeAccusation(player, guessWeapon, guessRoom, guessSuspect));
		assertFalse(game.getActivePlayers().contains(player));
		assertNull(getSpecificPlayer(player.getName())); //Check player removed from turn rotation
		assertEquals(numPlayers - 1, game.getActivePlayers().size());
	}
	
	@Test
	public void testAccusationFailAllPlayers()
	{
		setupGame(3);
		int numPlayers = game.getActivePlayers().size();
		RoomCard answerRoom = getAnswerRoomCard();
		RoomCard guessRoom = (RoomCard) game.getRoomCards().get(0);
		if(guessRoom == answerRoom)
		{
			guessRoom = (RoomCard) game.getRoomCards().get(1);
		}
		WeaponCard guessWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard guessSuspect = (SuspectCard) game.getSuspectCards().get(0);
		
		while(numPlayers > 0)
		{
			Player player = game.getActivePlayers().get(0);
			if(numPlayers > 1 && player == game.getCurrentPlayer())
			{
				player = game.getActivePlayers().get(1);
			}
			assertFalse(game.makeAccusation(player, guessWeapon, guessRoom, guessSuspect));
			assertFalse(game.getActivePlayers().contains(player));
			assertNull(getSpecificPlayer(player.getName())); //Check player removed from turn rotation
			numPlayers--;
			assertEquals(numPlayers, game.getActivePlayers().size());
		}
			assertTrue(game.isGameOver());
	}
	@Test
	public void testSelfAccusationFailAllPlayers()
	{
		setupGame(3);
		int numPlayers = game.getActivePlayers().size();
		RoomCard answerRoom = getAnswerRoomCard();
		RoomCard guessRoom = (RoomCard) game.getRoomCards().get(0);
		if(guessRoom == answerRoom)
		{
			guessRoom = (RoomCard) game.getRoomCards().get(1);
		}
		WeaponCard guessWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard guessSuspect = (SuspectCard) game.getSuspectCards().get(0);
		
		while(numPlayers > 0)
		{
			Player player = game.getCurrentPlayer();
			assertFalse(game.makeAccusation(player, guessWeapon, guessRoom, guessSuspect));
			assertFalse(game.getActivePlayers().contains(player));
			assertNull(getSpecificPlayer(player.getName())); //Check player removed from turn rotation
			numPlayers--;
			assertEquals(numPlayers, game.getActivePlayers().size());
		}
			assertTrue(game.isGameOver());
	}
	//TODO exits are blocked
	//TODO players enter same square
	//TODO multiple players can be in the same room
	//TODO suggestionOneCard
	//TODO suggestionMultipleCard
	//TODO suggestion secret passage
	//TODO suggestion when transferred
	//TODO player is actually transferred to the room

	@Test
	public void testSuggestionNoDisprovers() throws InvalidMoveException
	{
		RoomCard answerRoom = null;
		Field answerField = null;
		CaseFile answer = null;
		while(true){
			try {
				answerField = Game.class.getDeclaredField("answer");
				answerField.setAccessible(true);
				answer = (CaseFile) answerField.get(game);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				fail("Field access error");
			}
			assert answer != null;
			answerRoom = answer.getRoomCards().get(0);
			if(answerRoom.getName().equals("Conservatory"))
			{
				break;
			}
			setupGame(6);
		}
		assert answer != null;
		WeaponCard answerWeapon = answer.getWeaponCards().get(0);
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		putPeacockInRoom();
		Map<Player, Set<Card>> disprover = game.makeSuggestion(answerWeapon, answerSuspect);
		assertEquals(0,disprover.size());
	}
	
	@Test (expected = IllegalMethodCallException.class)
	public void testInvalidSuggestionOutsideRoom()
	{
		assertFalse(game.canMakeSuggestion());
		WeaponCard suggestWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard suggestSuspect = (SuspectCard) game.getSuspectCards().get(0);
		game.makeSuggestion(suggestWeapon, suggestSuspect);
		fail("Should not be allowed to make a suggestion outside a room");
	}
	
	@Test (expected = IllegalMethodCallException.class)
	public void testInvalidMultipleSuggestion() throws InvalidMoveException
	{
		putPeacockInRoom();
		assertTrue(game.canMakeSuggestion());
		WeaponCard suggestWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard suggestSuspect = (SuspectCard) game.getSuspectCards().get(0);
		game.makeSuggestion(suggestWeapon, suggestSuspect);
		assertFalse(game.canMakeSuggestion());
		game.makeSuggestion(suggestWeapon, suggestSuspect);
		fail("Should not be allowed to make a suggestion twice in one turn");
	}
	
}
