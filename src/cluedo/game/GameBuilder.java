package cluedo.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import cluedo.model.Displayable;
import cluedo.model.Piece;
import cluedo.model.Player;
import cluedo.model.Weapon;
import cluedo.model.cards.CaseFile;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;

//TODO better description for GameBuilder
/**
 * Creates the components for the Game class
 * These components include:
 * All players, human players, weapons, all cards (weapon, suspect, room)
 * and casefiles.
 *
 */
 class GameBuilder {
	
	 public static final String[] ROOM_NAMES = new String[] { "Ballroom", "Billiard Room", "Conservatory",
				"Dining Room", "Hall", "Kitchen", "Library", "Lounge", "Study" };
	 	/**
		 * A map of the suspect names mapped to the order the player is according to
		 * the clockwise order of the player's starting position in the Cluedo game
		 */
	 public static final Map<String, Integer> SUSPECT_NAMES = new HashMap<String, Integer>();
	// Static initializer
		{
			SUSPECT_NAMES.put("Miss Scarlett", 0);
			SUSPECT_NAMES.put("Colonel Mustard", 1);
			SUSPECT_NAMES.put("Mrs. White", 2);
			SUSPECT_NAMES.put("Reverend Green", 3);
			SUSPECT_NAMES.put("Mrs. Peacock", 4);
			SUSPECT_NAMES.put("Professor Plum", 5);
	}
	
	private static final String[] WEAPON_NAMES = new String[] { "Dagger", "Candlestick", "Revolver", "Rope",
				"Lead Pipe", "Spanner" };

		
	GameBuilder()
	{
	}	
	
	/**
	 * Create all the players in the Cluedo game
	 * 
	 * @param playerTokens
	 * @return All the players in the Cluedo game
	 */
	static List<Player> createPlayers(List<Piece> playerTokens) 
	{
		List<Player> players = new ArrayList<Player>();
		int i = 0;
		for (String playerName : SUSPECT_NAMES.keySet()) 
		{
			assert i < Game.MAX_PLAYERS : "Exceeded the total number of players";
			Player p = new Player(playerName, playerTokens.get(i));
			players.add(p);
			i++;
		}
		return players;
	}

	/**
	 * Randomly select the characters the human players will play as. The
	 * starting character (and therefore player) is selected randomly. The order
	 * of play is then based off the starting position of the other characters
	 * in a clockwise order. 
	 * This order is: Miss Scarlett, Colonel Mustard, Mrs.
	 * White, Reverend Green, Mrs. Peacock, Professor Plum
	 * 
	 * @param numPlayers
	 *            - The number of players playing Cluedo
	 * @return The characters that the human players will play as
	 */
	static List<Player> createHumanPlayers(int numPlayers, List<Player> allPlayers) 
	{
		assert allPlayers != null : "Must create all player objects first";
		assert allPlayers.size() == Game.MAX_PLAYERS : "Must contain all players in the game";
		Set<Player> allRandomPlayers = new HashSet<Player>(allPlayers);
		Player[] playerArr = new Player[Game.MAX_HUMAN_PLAYERS];
		//FIXME currently the game is starting on the 2nd player and not this starting player (due to nextTurn). To fix?
		Player startingPlayer = null; 
		// Generate random players
		for (Player randPlayer : allRandomPlayers) 
		{
			if (numPlayers == 0) 
			{
				break;
			}
			if (startingPlayer == null) 
			{
				startingPlayer = randPlayer;
				playerArr[0] = startingPlayer;
			} 
			else 
			{
				// Put added players in a clockwise order based off the starting
				// player's position on the board
				int startOrder = SUSPECT_NAMES.get(startingPlayer.getName());
				int playerOrder = SUSPECT_NAMES.get(randPlayer.getName());
				int index = playerOrder > startOrder ? playerOrder - startOrder : Game.MAX_HUMAN_PLAYERS + (playerOrder - startOrder);
				playerArr[index] = randPlayer;
			}
			numPlayers--;

		}
		List<Player> players = new ArrayList<Player>();
		// Remove all null references in the array to put in the list
		for (Player sortPlayer : playerArr) 
		{
			if (sortPlayer != null) 
			{
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
	static List<Weapon> createWeapons(List<Piece> weaponTokens) 
	{
		List<Weapon> weapons = new ArrayList<Weapon>();
		for (int i = 0; i < Game.NUM_WEAPONS; i++) 
		{
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
	static List<WeaponCard> createWeaponCards(List<Displayable> weaponCardFaces) 
	{
		List<WeaponCard> weaponCards = new ArrayList<WeaponCard>();
		for (int i = 0; i < Game.NUM_WEAPONS; i++)
		{
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
	static List<SuspectCard> createSuspectCards(List<Displayable> suspectCardFaces) 
	{
		List<SuspectCard> suspectCards = new ArrayList<SuspectCard>();
		int i = 0;
		for (String suspectName : SUSPECT_NAMES.keySet()) 
		{
			assert i < Game.MAX_PLAYERS : "Exceeded the total number of suspects";
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
	static List<RoomCard> createRoomCards(List<Displayable> roomCardFaces) 
	{
		List<RoomCard> roomCards = new ArrayList<RoomCard>();
		for (int i = 0; i < Game.NUM_ROOMS; i++) 
		{
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
	 * @param playerToCasefile
	 * @param activeHumanPlayers
	 * @return The CaseFile for the answer of the game
	 */
	static CaseFile createCaseFiles(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards,
			List<RoomCard> roomCards, Map<Player, CaseFile>  playerToCasefile, List<Player> activeHumanPlayers) 
	{
		for (Player player : activeHumanPlayers) 
		{
			playerToCasefile.put(player, new CaseFile(suspectCards, weaponCards, roomCards));
		}
		SuspectCard answerSuspect = null;
		for (SuspectCard suspect : suspectCards) 
		{
			answerSuspect = suspect;
			suspectCards.remove(suspect);
			break;
		}
		WeaponCard answerWeapon = null;
		for (WeaponCard weapon : weaponCards) 
		{
			answerWeapon = weapon;
			weaponCards.remove(weapon);
			break;
		}
		RoomCard answerRoom = null;
		for (RoomCard room : roomCards) 
		{
			answerRoom = room;
			roomCards.remove(room);
			break;
		}
		return new CaseFile(answerSuspect, answerWeapon, answerRoom);
	}
	
}
