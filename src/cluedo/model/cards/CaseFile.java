package cluedo.model.cards;

import java.util.ArrayList;
import java.util.List;


/**
 * A CaseFile is either an answer case file, or a player’s case file,
 * although there is no technical distinction. 
 * For a player's CaseFile, it contains the cards that have not been eliminated
 * from the case and could therefore could still be part of the answer
 * Cards may be removed from a player's CaseFile throughout the game.
 * 
 */
public class CaseFile 
{
	private List<SuspectCard> suspectCards;
	private List<WeaponCard> weaponCards;
	private List<RoomCard> roomCards;

	public CaseFile(List<SuspectCard> suspectCards, List<WeaponCard> weaponCards, List<RoomCard> roomCards) 
	{
		if (suspectCards.size() == 0 || weaponCards.size() == 0 || roomCards.size() == 0) 
		{
			throw new IllegalArgumentException("CaseFile must have at least one of each type of card");
		}
		this.roomCards = new ArrayList<RoomCard>(roomCards);
		this.suspectCards = new ArrayList<SuspectCard>(suspectCards);
		this.weaponCards = new ArrayList<WeaponCard>(weaponCards);
	}

	public CaseFile(SuspectCard suspectC, WeaponCard weaponC, RoomCard roomC) 
	{
		if (suspectC == null || weaponC == null || roomC == null) 
		{
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
	 * @param card - The card to remove from the casefile
	 */
	public void removeCard(Card card) 
	{
		if (card instanceof SuspectCard) 
		{
			suspectCards.remove(card);
		} 
		else if (card instanceof RoomCard) 
		{
			roomCards.remove(card);
		} 
		else if (card instanceof WeaponCard) 
		{
			weaponCards.remove(card);
		}
	}

	public List<SuspectCard> getSuspectCards() 
	{
		return suspectCards;
	}

	public List<WeaponCard> getWeaponCards() 
	{
		return weaponCards;
	}

	public List<RoomCard> getRoomCards() 
	{
		return roomCards;
	}
	public boolean containsCard(Card card)
	{
		if (card instanceof SuspectCard) 
		{
			return suspectCards.contains(card);
		} 
		else if (card instanceof RoomCard) 
		{
			return roomCards.contains(card);
		} 
		else if (card instanceof WeaponCard) 
		{
			return weaponCards.contains(card);
		}
		return false;
	}
	public boolean containsSuspectCard(SuspectCard card)
	{
		return suspectCards.contains(card);
	}
	public boolean containsWeaponCard(WeaponCard card)
	{
		return weaponCards.contains(card);
	}
	public boolean containsRoomCard(RoomCard card)
	{
		return roomCards.contains(card);
	}

	
}
