package cluedo.model;

/**
 * A Room is an area where Players can gather, and the location of potential murder Weapons.
 * Certain actions, such as Suggestions, are tied to Rooms and as such may only be initiated within a Room. 
 * 
 * Note that Rooms do not contain mutable state, as they are passed to the UI, so the location of
 * Weapons, Players, and the Cells within a room are handled by the Game.
 */
public class Room
{
	/**
	 * The human readable name of the Room.
	 * May not be empty.
	 * May not be null.
	 * Example: Library
	 */
	private final String name;

	/**
	 * @param name The human readable name of the Room. May not be empty, or null.
	 * @throws IllegalArgumentException if any of the above conditions are not met.
	 */
	public Room(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Arguments may not be null");
		}
		
		if (name.equals(""))
		{
			throw new IllegalArgumentException("Name must be a non empty string");
		}		
		
		this.name = name;
	}
	
	/**
	 * The name of a Room is the human readable representation of the Room.
	 * Example: Library
	 * @return Non null string representing this Room's name.
	 */
	public String getName()
	{
		return name;
	}
}