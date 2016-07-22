package cluedo.model;

/**
 * A character in the game of Cluedo.
 * Note that this class does not refer to the human players that play the game.
 */
public class Player
{
	/**
	 * The human readable name of this character.
	 * May not be empty.
	 * May not be null.
	 * Example: Miss Scarlett
	 */
	private final String name;
	
	/**
	 * The Piece that represents the Player on the Board.
	 * May not be null.
	 */
	private final Piece piece;

	public Player(String name, Piece piece)
	{
		if (name == null || piece == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}
		
		if (name.equals(""))
		{
			throw new IllegalArgumentException("Name must be a non empty string");
		}

		this.name = name;
		this.piece = piece;
	}

	/**
	 * The name associated with this Player.
	 * Will not be null.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The Piece associated with this Player.
	 * Will not be null.
	 */
	public Piece getPiece()
	{
		return piece;
	}

	/**
	 * Two players are considered equal iff their names are equal, and their Pieces are equal.
	 */
	@Override
	public boolean equals(Object o)
	{
		boolean isEqual = false;
		
		if (o instanceof Player)
		{
			Player p = (Player) o;
			isEqual = this.getName().equals(p.getName())
					&& this.getPiece().equals(p.getPiece());
		}
		
		return isEqual;
	}
	
	/**
	 * Default hashcode as generated by Eclipse.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((piece == null) ? 0 : piece.hashCode());
		return result;
	}
}