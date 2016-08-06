package cluedo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluedo.model.Player;
import cluedo.model.Weapon;
import cluedo.model.cards.CaseFile;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;

/**
 * Creates the components for the Game class
 * These components include:
 * All players, human players, weapons, all cards (weapon, suspect, room)
 * and casefiles.
 *
 */
 class GameBuilder {
	
	 //Constants
	 public static final String[] ROOM_NAMES = new String[] { "Ballroom", "Billiard Room", "Conservatory",
				"Dining Room", "Hall", "Kitchen", "Library", "Lounge", "Study" };
	/**
	 * A map of the suspect names mapped to the order the player is according to
	 * the clockwise order of the player's starting position in the Cluedo game
	 */
	 public static final String[] SUSPECT_NAMES = new String[]{ "Miss Scarlett","Colonel Mustard",
			 "Mrs. White","Reverend Green","Mrs. Peacock","Professor Plum"};
	 public static final Map<String, Integer> SUSPECT_ORDER = new HashMap<String, Integer>();
	 static{
			SUSPECT_ORDER.put("Miss Scarlett", 0);
			SUSPECT_ORDER.put("Colonel Mustard", 1);
			SUSPECT_ORDER.put("Mrs. White", 2);
			SUSPECT_ORDER.put("Reverend Green", 3);
			SUSPECT_ORDER.put("Mrs. Peacock", 4);
			SUSPECT_ORDER.put("Professor Plum", 5);
	}
	
	private static final String[] WEAPON_NAMES = new String[] { "Dagger", "Candlestick", "Revolver", "Rope",
				"Lead Pipe", "Spanner" };
	/**
	 * Create all the players in the Cluedo game
	 * 
	 * @return All the players in the Cluedo game
	 */
	static List<Player> createPlayers() 
	{
		List<Player> players = new ArrayList<Player>();
		int i = 0;
		for (String playerName : SUSPECT_NAMES) 
		{
			assert i < Game.MAX_PLAYERS : "Exceeded the total number of players";
			Player p = new Player(playerName);
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
		List<Player> allRandomPlayers = new ArrayList<Player>(allPlayers);
		Collections.shuffle(allRandomPlayers);
		Player[] playerArr = new Player[Game.MAX_HUMAN_PLAYERS];
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
				/*
				 * Put added players in a clockwise order based off the starting
				 * player's position on the board
				 */
				int startOrder = SUSPECT_ORDER.get(startingPlayer.getName());
				int playerOrder = SUSPECT_ORDER.get(randPlayer.getName());
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
	 * @return All the weapons in the Cluedo Game
	 */
	static List<Weapon> createWeapons() 
	{
		List<Weapon> weapons = new ArrayList<Weapon>();
		for (int i = 0; i < Game.NUM_WEAPONS; i++) 
		{
			Weapon w = new Weapon(WEAPON_NAMES[i]);
			weapons.add(w);
		}
		return weapons;
	}

	/**
	 * Create the weapon cards in the Cluedo Game
	 * 
	 * @return All the weapon cards in the Cluedo Game
	 */
	static List<WeaponCard> createWeaponCards() 
	{
		List<WeaponCard> weaponCards = new ArrayList<WeaponCard>();
		for (int i = 0; i < Game.NUM_WEAPONS; i++)
		{
			WeaponCard card = new WeaponCard(WEAPON_NAMES[i]);
			weaponCards.add(card);
		}
		return weaponCards;
	}

	/**
	 * Create the suspect cards in the Cluedo Game
	 * 
	 * @return All the suspect cards in the Cluedo Game
	 */
	static List<SuspectCard> createSuspectCards() 
	{
		List<SuspectCard> suspectCards = new ArrayList<SuspectCard>();
		int i = 0;
		for (String suspectName : SUSPECT_NAMES) 
		{
			assert i < Game.MAX_PLAYERS : "Exceeded the total number of suspects";
			SuspectCard p = new SuspectCard(suspectName);
			suspectCards.add(p);
			i++;
		}
		return suspectCards;
	}

	/**
	 * Create the room cards in the Cluedo Game
	 * 
	 * @return All the room cards in the Cluedo Game
	 */
	static List<RoomCard> createRoomCards() 
	{
		List<RoomCard> roomCards = new ArrayList<RoomCard>();
		for (int i = 0; i < Game.NUM_ROOMS; i++) 
		{
			RoomCard card = new RoomCard(ROOM_NAMES[i]);
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
		List<SuspectCard> suspects = new ArrayList<SuspectCard>(suspectCards);
		Collections.shuffle(suspects);
		SuspectCard answerSuspect = suspects.get(0);
		List<WeaponCard> weapons = new ArrayList<WeaponCard>(weaponCards);
		Collections.shuffle(weapons);
		WeaponCard answerWeapon = weapons.get(0);
		List<RoomCard> rooms = new ArrayList<RoomCard>(roomCards);
		Collections.shuffle(rooms);
		RoomCard answerRoom = rooms.get(0);
		return new CaseFile(answerSuspect, answerWeapon, answerRoom);
	}
	
}
