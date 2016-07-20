package cluedo.model;

/**
 * A token on the Board.
 * May represent any Displayable aspect that is placed on the Board.
 * Displayable aspects that are not placed on the Board should extend Displayable directly.
 * Each Piece has a final unique ID which can be used to distinguish one Piece from another.
 */
public abstract class Piece implements Displayable
{
	private static int generateID = 0;
	private final int id; // This Piece's unique ID.

	public Piece()
	{
		id = generateID;
		generateID++;
	}

	/**
	 * The unique ID associated with this Piece.
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Two Pieces are considered equal iff their IDs are equal.
	 */
	public boolean equals(Object o)
	{
		boolean isEqual = false;
		
		if (o instanceof Piece)
		{
			Piece p = (Piece) o;
			isEqual = this.getID() == p.getID();
		}
		
		return isEqual;
	}
}