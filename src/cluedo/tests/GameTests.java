package cluedo.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cluedo.game.Game;
import cluedo.model.Displayable;
import cluedo.model.Piece;
import cluedo.model.Player;

public class GameTests {

	private Game game;
	private Map<String, Integer> SUSPECT_NAMES;
		// Static initializer
	 public void setupSuspectNames(){
		 SUSPECT_NAMES = new HashMap<String, Integer>();
		 SUSPECT_NAMES.put("Miss Scarlett", 0);
		 SUSPECT_NAMES.put("Colonel Mustard", 1);
		 SUSPECT_NAMES.put("Mrs. White", 2);
		 SUSPECT_NAMES.put("Reverend Green", 3);
		 SUSPECT_NAMES.put("Mrs. Peacock", 4);
		 SUSPECT_NAMES.put("Professor Plum", 5);
	 }
	
	@Before
	public void setup()
	{
	 setupGame();
	}
	public void setupGame()
	{
		setupSuspectNames();
		List<Piece> playerTokens = createPlayerTokens();
		List<Piece> weaponTokens = createWeaponTokens();
		List<Displayable> suspectCardFaces = createSuspectCards();
		List<Displayable> weaponCardFaces = createWeaponCards();
		List<Displayable> roomCardFaces = createRoomCards();
		game = new Game(6,playerTokens,weaponTokens,suspectCardFaces,weaponCardFaces,roomCardFaces);
	}
	private List<Piece> createPlayerTokens()
	{
		List<Piece> players = new ArrayList<Piece>();

		for (int player = 0; player < Game.MAX_HUMAN_PLAYERS; player++)
		{
			players.add(() -> {});
		}

		return players;
	}

	private List<Piece> createWeaponTokens()
	{
		List<Piece> weapons = new ArrayList<Piece>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createRoomCards()
	{
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.NUM_ROOMS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createWeaponCards()
	{
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.NUM_WEAPONS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}

	private List<Displayable> createSuspectCards()
	{
		List<Displayable> weapons = new ArrayList<Displayable>();

		for (int weapon = 0; weapon < Game.MAX_PLAYERS; weapon++)
		{
			weapons.add(() -> {});
		}

		return weapons;
	}
	@Test
	public void testPlayerOrder() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Player startPlayer = game.nextTurn();
		assertNotNull("Should not be given null startPlayer",startPlayer);
		int startOrderNum = SUSPECT_NAMES.get(startPlayer.getName());
		resetRemainingMoves();
		Player currentPlayer = null;
		while(currentPlayer != startPlayer)
		{
			startOrderNum++;
			if(startOrderNum >= Game.MAX_HUMAN_PLAYERS){
				startOrderNum = 0;
			}
			resetRemainingMoves();
			currentPlayer = game.nextTurn();
			if(SUSPECT_NAMES.get(currentPlayer.getName()) != startOrderNum)
			{
				fail("Player order not enforced");
			}
		}
	}
	/**
	 * Using reflection to set remainingMoves to zero each time
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void resetRemainingMoves() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field remainingMoves = Game.class.getDeclaredField("remainingMoves");
		remainingMoves.setAccessible(true);
		remainingMoves.set(game, 0);
	}

}
