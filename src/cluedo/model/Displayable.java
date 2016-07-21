package cluedo.model;

/**
 * Classes that implement this interface are to be made by the user interface.
 * Although this method could be called by the Game or Board, it should never be done so
 * as it is the responsibility of the UI to display information to the user.
 * Gives a visual clue that this class will be implemented by the UI.
 */
public interface Displayable
{
	/**
	 * Output information about the current state of the Displayable object.
	 * Example: The output may be text, an image, or sound. There is no restriction to where the output goes - however it will usually be a monitor.
	 */
	public void display();
}