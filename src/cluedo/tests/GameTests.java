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
	 	setupGame(6);
	}
	public void setupGame(int numPlayers)
	{
		List<Player> activePlayers = new ArrayList<Player>(Game.allPlayers);
		List<String> playerNames = new ArrayList<String>();
		
		for(int i = Game.MAX_HUMAN_PLAYERS; i > numPlayers; i--)
		{
			activePlayers.remove(0);
		}
		
		for (int i = 0; i < activePlayers.size(); i++)
		{
			playerNames.add(Integer.toString(i));
		}
		
		game = new Game(activePlayers, playerNames);
	}
	/**
	 * Using reflection to set remainingMoves to zero for testing purposes
	 */
	private void resetRemainingMoves()
	{
		Field remainingMoves = null;
		try 
		{
			remainingMoves = Game.class.getDeclaredField("remainingMoves");
			remainingMoves.setAccessible(true);
			remainingMoves.set(game, 0);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field cannot be accessed");
		}
	}
	/**
	 * Using reflection to set remainingMoves to a specific number for testing purposes
	 */
	private void setRemainingMoves(int moveNum)
	{
		Field remainingMoves = null;
		try 
		{
			remainingMoves = Game.class.getDeclaredField("remainingMoves");
			remainingMoves.setAccessible(true);
			remainingMoves.set(game, moveNum);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field cannot be accessed");
		}
	}
	/**
	 * Use reflection to set gameOver to true
	 * for testing purposes
	 */
	private void causeGameOver()
	{
		Field gameOver = null;
		try 
		{
			gameOver = Game.class.getDeclaredField("gameOver");
			gameOver.setAccessible(true);
			gameOver.set(game, true);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field cannot be accessed");
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
		try 
		{
			answerField = Game.class.getDeclaredField("answer");
			answerField.setAccessible(true);
			answer = (CaseFile) answerField.get(game);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field access error");
		}
		assert answer != null;
		return answer.getRoomCards().get(0);
	}
	
	/**
	 * Used to move players to a particular cell
	 * using reflection.
	 * @param player
	 * @param x
	 * @param y
	 */
	private void teleportPlayer(Player player,int x, int y)
	{
		Field boardField;
		try 
		{
			boardField = Game.class.getDeclaredField("board");
			boardField.setAccessible(true);
			Board board = (Board) boardField.get(game);
			board.setPosition(player, x, y);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field not accessible");
		}
	
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
			Cell cell = game.getPosition(w);
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
		List<Player> players = Game.allPlayers;
		Cell[][] cells = game.getCells();
		int pCount = 0;
		for(int i = 0; i < STARTINGPOSITION.length;i+=2)
		{
			int x = STARTINGPOSITION[i];
			int y = STARTINGPOSITION[i+1];
			Player player = players.get(pCount);
			assertEquals("Player should match order",player.getName(), SUSPECT_NAMES[pCount]);
			Cell cell = game.getPosition(player);
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
		try
		{
			setupGame(Game.MIN_HUMAN_PLAYERS-1);
		}
		catch(IllegalArgumentException e)
		{	
		}
		/*try
		{
			setupGame(Game.MAX_HUMAN_PLAYERS+1);
			fail("Cannot have over " + Game.MAX_HUMAN_PLAYERS + " players");
		}
		catch(IllegalArgumentException e)
		{
		}*/
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
	
	/**
	 * Test game creation for a different number of players
	 */
	@Test 
	public void testNumberPlayers()
	{
		setupGame(6);
		assertEquals(6, Game.allPlayers.size());
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
		catch(IllegalMethodCallException e)
		{
		}
		try
		{
			game.move(Direction.East);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e)
		{	
		}
		
		WeaponCard weaponCard = (WeaponCard)game.getWeaponCards().get(0);
		SuspectCard suspectCard = (SuspectCard) game.getSuspectCards().get(0);
		RoomCard roomCard = (RoomCard) game.getRoomCards().get(0);
		
		try
		{
			assertFalse(game.canMakeSuggestion());
			game.makeSuggestion(weaponCard,suspectCard);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e)
		{
		}
		try
		{
			game.makeAccusation(currentPlayer,weaponCard,roomCard,suspectCard);
			fail("Cannot continue playing when the game is over");

		}
		catch(IllegalMethodCallException e)
		{
		}
		try
		{
			assertFalse(game.canMakeSuggestion());
			game.makeSuggestion(weaponCard,suspectCard);
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e)
		{
		}
		try
		{
			game.takeExit(game.getPosition(game.getActivePlayers().get(0)));
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e)
		{
		}
		try
		{
			game.getAvailableExits();
			fail("Cannot continue playing when the game is over");
		}
		catch(IllegalMethodCallException e)
		{
		}
	}
	/**
	 * Moving onto another cell (in the hallway)
	 */
	@Test
	public void testValidMove()
	{
		Player currentPlayer = game.getCurrentPlayer();
		int remainingMoves = game.getRemainingMoves();
		Cell pos = game.getPosition(currentPlayer);
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
		try 
		{
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
		} 
		catch (InvalidMoveException e) 
		{
			fail("Could not move " + e.getMessage());
		}
		assertEquals(remainingMoves-1, game.getRemainingMoves());
	}
	
	/**
	 * Cannot give a null direction
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidNullDirectionMove() throws InvalidMoveException
	{
		game.move(null);
		fail("Null should not be able to move a direction");
	}
	
	/**
	 * Cannot move as no more moves left
	 */
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
	
	/**
	 * Cannot move through a wall
	 */
	@Test(expected = InvalidMoveException.class)
	public void testInvalidMoveAgainstWall() throws InvalidMoveException
	{
		getSpecificPlayer("Mrs. Peacock");
		game.move(Direction.North);
		fail("Should not be able to move through a wall");
	}
	/**
	 * Players cannot move on a cell that another player is on
	 */
	@Test (expected = InvalidMoveException.class)
	public void testInvalidMoveSameSquare() throws InvalidMoveException
	{
		
		getSpecificPlayer("Mrs. White");
		teleportPlayer(getSpecificPlayer("Mrs. White"),0,7);
		teleportPlayer(getSpecificPlayer("Mrs. Peacock"),0,8);
		getSpecificPlayer("Mrs. White");
		game.move(Direction.South);
	}
	
	/**
	 * Cannot move as the path is blocked by another player
	 */
	@Test (expected = InvalidMoveException.class)
	public void testInvalidMovePathBlocked() throws InvalidMoveException
	{
		getSpecificPlayer("Mrs. Peacock");
		assertTrue(game.canMove());
		teleportPlayer(getSpecificPlayer("Mrs. White"),22,6);
		getSpecificPlayer("Mrs. Peacock");
		assertFalse(game.canMove());
		game.move(Direction.West);
	}
	
	/**
	 * Entering a room
	 */
	@Test
	public void testEnterRoom() throws InvalidMoveException
	{
		putPeacockInRoom();
		Player peacock = getSpecificPlayer("Mrs. Peacock");
		Cell playerPos = game.getPosition(peacock);
		assertTrue(game.isInRoom());
		assertEquals(0,game.getRemainingMoves());
		assertEquals("Conservatory",game.getRoom(playerPos).getName());
		assertEquals("Conservatory",game.getCurrentRoom().getName());
	}
	
	/**
	 * Exiting a room into the hallway
	 */
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
		Cell playerPos = game.getPosition(peacock);
		assertEquals(18, playerPos.getX());
		assertEquals(5, playerPos.getY());
	}
	
	/**
	 * Cannot exit room as player's are blocking the way
	 */
	@Test
	public void testExitsBlocked() throws InvalidMoveException
	{
		teleportPlayer(getSpecificPlayer("Mrs. Peacock"), 17, 9);
		game.move(Direction.East);
		Player white = getSpecificPlayer("Mrs. White");
		Player plum = getSpecificPlayer("Professor Plum");
		teleportPlayer(white, 22, 13);
		teleportPlayer(plum, 17, 9);
		getSpecificPlayer("Mrs. Peacock");
		try 
		{
			List<Cell> exits = game.getAvailableExits();
			fail("All exits should be blocked");
		} 
		catch (NoAvailableExitException e) 
		{
		}
		Cell exitOne = game.getPosition(white);
		Cell exitTwo = game.getPosition(plum);
		try
		{
			game.takeExit(exitOne);
			fail("Should not be able to take an exit that's blocked");
		}
		catch(InvalidMoveException e)
		{
		}
		try
		{
			game.takeExit(exitTwo);
			fail("Should not be able to take an exit that's blocked");
		}
		catch(InvalidMoveException e)
		{
		}
	}
	
	/**
	 * Player tries to reenter a room they have exited
	 * on the same turn
	 */
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
		Cell playerPos = game.getPosition(peacock);
		assertEquals(18, playerPos.getX());
		assertEquals(5, playerPos.getY());
		game.move(Direction.North);
		fail("Should not be able to reenter a room");
	}
	
	/**
	 * Player uses a secret passage on one of the corner rooms
	 */
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
		Cell playerPos = game.getPosition(peacock);
		assertEquals("Lounge",game.getRoom(playerPos).getName());
		assertTrue(game.canMakeSuggestion());
		WeaponCard guessWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard guessSuspect = (SuspectCard) game.getSuspectCards().get(0);
		assertTrue(game.canMakeSuggestion());
		game.makeSuggestion(guessWeapon,guessSuspect);
	}
	
	/**
	 * A player made an accusation and won the game
	 */
	@Test
	public void testAccusationWin()
	{
		Field answerField = null;
		CaseFile answer = null;
		try 
		{
			answerField = Game.class.getDeclaredField("answer");
			answerField.setAccessible(true);
			answer = (CaseFile) answerField.get(game);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field access error");
		}
		assert answer != null;
		RoomCard answerRoom = answer.getRoomCards().get(0);
		WeaponCard answerWeapon = answer.getWeaponCards().get(0);
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		assertTrue(game.makeAccusation(game.getCurrentPlayer(), answerWeapon, answerRoom,answerSuspect));
		assertTrue(game.isGameOver());
	}
	
	/**
	 * A player made an accusation on their corresponding turn and failed
	 */
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
		assertFalse(game.isGameOver());
	}
	
	/**
	 * A player made an accusation on another player's
	 * turn and failed
	 */
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
		assertFalse(game.isGameOver());
	}
	
	/**
	 * A player made an accusation on another player's
	 * turn and failed. This happens until there are no more players
	 */
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
	
	/**
	 * All players made an accusation on their corresponding turn
	 * and failed
	 */
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
	
	/**
	 * Suggestion where a player who is disproving the suggestion
	 * has more than one card that was suggested
	 */
	@Test
	public void testSuggestionMultipleCard() throws InvalidMoveException
	{
		Player disprovingPlayer = null;
		WeaponCard guessWeapon = null;
		SuspectCard guessSuspect = null;
		boolean completeSetUp = false;
		/*
		 * Must get a player who has both a weapon and a suspect card to do the test
		 * Otherwise reset the game
		 */
		while(!completeSetUp)
		{
			//Remove the roomCard as a variable
			while(true)
			{
				CaseFile answer = null;
				try 
				{
					Field answerField = Game.class.getDeclaredField("answer");
					answerField.setAccessible(true);
					answer = (CaseFile) answerField.get(game);
				} 
				catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
				{
					fail("Field access error");
				}
				assert answer != null;
				RoomCard answerRoom = answer.getRoomCards().get(0);
				if(answerRoom.getName().equals("Conservatory"))
				{
					break;
				}
				setupGame(6);
			}

			getSpecificPlayer("Mrs. Peacock");
			disprovingPlayer = nextPlayer();
			while(!disprovingPlayer.getName().equals("Mrs. Peacock"))
			{
				try 
				{
					Field playerHandField = Game.class.getDeclaredField("playerHand");
					playerHandField .setAccessible(true);
					Map<Player,List<Card>> allHands = (Map<Player,List<Card>>) playerHandField .get(game);
					for(Card card : allHands.get(disprovingPlayer))
					{
						if(card instanceof WeaponCard)
						{
							guessWeapon = (WeaponCard) card;
						}
						else if(card instanceof SuspectCard)
						{
							guessSuspect = (SuspectCard) card;
						}
					}
				} 
				catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
				{
					fail("Field access error");
				}
				if(guessWeapon != null && guessSuspect != null)
				{
					completeSetUp = true;
					break;
				}
				else
				{
					guessWeapon = null;
					guessSuspect = null;
					disprovingPlayer = nextPlayer();
				}
			}
		}
		putPeacockInRoom();
		Map<Player,Set<Card>> disproving = game.makeSuggestion(guessWeapon, guessSuspect);
		assertEquals(1,disproving.size());
		assertTrue(disproving.containsKey(disprovingPlayer));
		Set<Card> cards = disproving.get(disprovingPlayer);
		assertEquals(2,cards.size());
		assertTrue(cards.contains(guessWeapon));
		assertTrue(cards.contains(guessSuspect));
	}
	
	/**
	 * Suggestion with one player who has one of the cards to disprove
	 * the suggestion
	 */
	@Test
	public void testSuggestionOneCard() throws InvalidMoveException
	{
		RoomCard answerRoom = null;
		Field answerField = null;
		CaseFile answer = null;
		//Remove the roomCard as a variable
		while(true){
			try 
			{
				answerField = Game.class.getDeclaredField("answer");
				answerField.setAccessible(true);
				answer = (CaseFile) answerField.get(game);
			} 
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
			{
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
		WeaponCard guessWeapon = null;
		Field playerHandField = null;
		Map<Player,List<Card>> allHands = null;
		try 
		{
			playerHandField = Game.class.getDeclaredField("playerHand");
			playerHandField.setAccessible(true);
			allHands = (Map<Player,List<Card>>) playerHandField.get(game);
			for(Map.Entry<Player, List<Card>> hands : allHands.entrySet())
			{
				if(hands.getKey().getName().equals("Mrs. Peacock"))
				{
					continue;
				}
				for(Card card : hands.getValue())
				{
					if(card instanceof WeaponCard)
					{
						guessWeapon = (WeaponCard) card;
						break;
					}
				}
				if(guessWeapon != null)
				{
					break;
				}
			}
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			fail("Field access error");
		}
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		putPeacockInRoom();
		Map<Player, Set<Card>> disprover = game.makeSuggestion(guessWeapon, answerSuspect);
		assertEquals(1,disprover.size());
		Map<Player,Card> toRemove = new HashMap<Player,Card>();
		for(Map.Entry<Player, Set<Card>> suggestion: disprover.entrySet())
		{
			Player player = suggestion.getKey();
			assertEquals(1,suggestion.getValue().size());
			assertTrue(suggestion.getValue().contains(guessWeapon));
			assert allHands != null;
			assertTrue(allHands.get(player).contains(guessWeapon)); //Check player actually has the card
			//Check card is removed from current player, peacock's casefile
			toRemove.put(player,guessWeapon);
			game.removeCard(toRemove);
			assertFalse(game.getPlayerWeaponCards().contains(guessWeapon));
		}
	}
	
	/**
	 * Suggestions with no players disproving
	 */
	@Test
	public void testSuggestionNoDisprovers() throws InvalidMoveException
	{
		RoomCard answerRoom = null;
		Field answerField = null;
		CaseFile answer = null;
		while(true){
			try 
			{
				answerField = Game.class.getDeclaredField("answer");
				answerField.setAccessible(true);
				answer = (CaseFile) answerField.get(game);
			} 
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
			{
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
	
	/**
	 * Transferred player can make a suggestion
	 */
	@Test
	public void testSuggestionTransferred() throws InvalidMoveException
	{
		Field answerField = null;
		CaseFile answer = null;
			try 
			{
				answerField = Game.class.getDeclaredField("answer");
				answerField.setAccessible(true);
				answer = (CaseFile) answerField.get(game);
			} 
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
			{
				fail("Field access error");
			}
		assert answer != null;
		WeaponCard answerWeapon = answer.getWeaponCards().get(0);
		SuspectCard answerSuspect = answer.getSuspectCards().get(0);
		putPeacockInRoom();
		game.makeSuggestion(answerWeapon, answerSuspect);
		nextPlayer();
		getSpecificPlayer(answerSuspect.getName());
		//Multiple players can be in the same room
		//Check transferred player is actually in the room
		assertTrue(game.isInRoom());
		assertEquals("Conservatory",game.getCurrentRoom().getName());
		/*
		 * Cannot make a suggestion if player suggested a suspect card
		 * with piece's face as they would not have transferred rooms
		 */
		if(answerSuspect.getName().equals("Mrs. Peacock"))
		{
			assertFalse(game.canMakeSuggestion());
			try{
				
				game.makeSuggestion(answerWeapon, answerSuspect);
			}
			catch(IllegalArgumentException e)
			{
			}
		}
		//Transferred player can make a suggestion in the room
		else
		{
			assertTrue(game.canMakeSuggestion());
			game.makeSuggestion(answerWeapon, answerSuspect);
		}
	}
	
	/**
	 * Cannot make a suggestion outside a room
	 */
	@Test (expected = IllegalMethodCallException.class)
	public void testInvalidSuggestionOutsideRoom()
	{
		assertFalse(game.canMakeSuggestion());
		WeaponCard suggestWeapon = (WeaponCard) game.getWeaponCards().get(0);
		SuspectCard suggestSuspect = (SuspectCard) game.getSuspectCards().get(0);
		game.makeSuggestion(suggestWeapon, suggestSuspect);
		fail("Should not be allowed to make a suggestion outside a room");
	}
	
	/**
	 * Cannot make multiple suggestions in one turn
	 */
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
