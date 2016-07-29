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
		this.roomCards = roomCards;
		this.suspectCards = suspectCards;
		this.weaponCards = weaponCards;
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
		} else if (card instanceof RoomCard) 
		{
			roomCards.remove(card);
		} else if (card instanceof WeaponCard) 
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

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomCards == null) ? 0 : roomCards.hashCode());
		result = prime * result + ((suspectCards == null) ? 0 : suspectCards.hashCode());
		result = prime * result + ((weaponCards == null) ? 0 : weaponCards.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CaseFile other = (CaseFile) obj;
		if (roomCards == null)
		{
			if (other.roomCards != null)
				return false;
		} 
		else if (!roomCards.equals(other.roomCards))
			return false;
		if (suspectCards == null) 
		{
			if (other.suspectCards != null)
				return false;
		} 
		else if (!suspectCards.equals(other.suspectCards))
			return false;
		if (weaponCards == null) 
		{
			if (other.weaponCards != null)
				return false;
		} 
		else if (!weaponCards.equals(other.weaponCards))
			return false;
		return true;
	}

	
}
