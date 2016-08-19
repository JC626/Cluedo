package cluedo.userinterface;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cluedo.model.Cell;
import cluedo.utility.ErrorChecking;

public class GraphicalUserInterface extends JFrame
{
	private JPanel mainMenu;

	private JLabel title = new JLabel("Cluedo");

	private JButton newGame = new JButton("New Game");
	private JButton quit = new JButton("Quit");

	private BoardFrame boardDisplay;

	private final ActionListener actionOnCloseButton;

	/**
	 * Create a new GUI.
	 * @param actionOnCloseButton The actions to take on clicking the close button.
	 * Null will be passed in as the action event.
	 */
	public GraphicalUserInterface(ActionListener actionOnCloseButton)
	{
		super("Cluedo"); // Window title
		
		this.actionOnCloseButton = actionOnCloseButton;

		setupMainMenu();

		addAllPanels();
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionOnCloseButton.actionPerformed(null);
			}
		});
		
		this.setMinimumSize(new Dimension(600, 600));
		
		setResizable(false);
		pack();
		this.setVisible(true);	
	}

	private void addAllPanels()
	{
		this.add(mainMenu);
	}

	private void setupMainMenu()
	{
		createMainMenu();
		setLayoutMainMenu();
		setLayoutSizes();
		addToPanel(mainMenu, getMainMenuComponents());
	}
	
	private void createMainMenu()
	{
		mainMenu = new JPanel();
		mainMenu.add(title);
	}
	
	private void setLayoutMainMenu()
	{
		mainMenu.setLayout(new GridLayout(0,1,0,80));
		mainMenu.setBorder(BorderFactory.createEmptyBorder(20, 75, 50, 75));
	}

	private void setLayoutSizes()
	{
		setFontSize(title, 100);
		setFontSize(newGame, 25);
		setFontSize(quit, 25);
	}
	
	private Iterator<Component> getMainMenuComponents()
	{
		List<Component> list = new ArrayList<Component>();
		list.add(newGame);
		list.add(quit);
		return list.iterator();
	}

	private void addToPanel(JPanel panel, Iterator<Component> toAdd)
	{
		while (toAdd.hasNext())
		{
			panel.add(toAdd.next());
		}
	}
	
	public void dialogInformation(String title, String text)
	{
		MessageDialog.information(title, text);
	}
	
	public void dialogError(String title, String text)
	{
		MessageDialog.error(title, text);
	}
	
	public boolean dialogYesNo(String title, String question)
	{
		return ConfirmationDialog.yesNo(title, question);
	}

	public boolean dialogOkCancel(String title, String question)
	{
		return ConfirmationDialog.okCancel(title, question);
	}

	public void dialogViewHand(String title, List<? extends JRadioButton> buttons, List<ImageIcon> images)
	{
		new ViewHandDialog(this, title).display(buttons, images);
	}

	public Optional<Integer> dialogRadioButtons(String windowTitle, String question, List<String> options, List<Boolean> available)
	{
		ErrorChecking.ensureNonEmpty(options, available);
		
		List<JRadioButton> buttons = RadioButtonDialog.createRadioButtons(options, available);
		
		Optional<Integer> selectedOption = new RadioButtonDialog(this, windowTitle,question).getUserSelection(buttons);
		
		if (selectedOption.isPresent())
		{
			assert selectedOption.get() >= 0 && selectedOption.get() < options.size() : "Selected option was not in options";
			assert available.get(selectedOption.get()) : "Selected an unavailable option";
		}
		 
		return selectedOption;
	}
	
	public Optional<String> dialogTextInput(String windowTitle, String question)
	{
		return new InputTextDialog(this, windowTitle).getUserInput(question);
	}
	
	/**
	 * Change the location of a player's piece
	 * @param piece - The original image of the piece
	 * @param newPos - The new position of the piece
	 */
	public void changePieceLocation(Image piece, Cell newPos)
	{
		boardDisplay.getBoardPane().changePieceLocation(piece, newPos);
	}
	
	public void animatePlayerMove(Image piece, Cell newPos)
	{
		boardDisplay.getBoardPane().animatePlayerMove(piece, newPos);
	}
	
	/**
	 * Changes the dice displayed when the player
	 * starts their turn.
	 * @param leftDie
	 * @param rightDie
	 */
	public void changeDice(Image leftDie, Image rightDie) 
	{
		boardDisplay.getDicePane().changeDice(leftDie, rightDie);
	}
	
	/**
	 * Draws the cells that a player can take 
	 * to exit a room
	 * @param exitCells - The cells that can be taken as exits
	 * @param exitImage - The image of an exit cell
	 */
	public void drawExitCells(List<Cell> exitCells,Image exitImage)
	{
		boardDisplay.getBoardPane().drawExitCells(exitCells, exitImage);
	}

	public void buttonNewGameListener(ActionListener a)
	{
		newGame.addActionListener(a);
	}

	public void buttonQuitListener(ActionListener a)
	{
		quit.addActionListener(a);
	}
	
	public void addHandListener(ActionListener a)
	{
		boardDisplay.addHandListener(a);
	}
	
	public void addCasefileListener(ActionListener a)
	{
		boardDisplay.addCasefileListener(a);
	}
	
	public void addSuggestionListener(ActionListener a)
	{
		boardDisplay.addSuggestionListener(a);
	}
	
	public void addAccusationListener(ActionListener a)
	{
		boardDisplay.addAccusationListener(a);
	}
	
	public void addEndTurnListener(ActionListener a)
	{
		boardDisplay.addEndTurnListener(a);
	}
	
	public void addNewGameListener(ActionListener a)
	{
		boardDisplay.addNewGameListener(a);
	}
	
	public void addQuitListener(ActionListener a)
	{
		boardDisplay.addQuitListener(a);
	}
	
	public void setBoardTitle(String title)
	{
		boardDisplay.setTitle(title);
	}
	
	
	
	
	
	/**
	 * A wrapper method for setFont, which changes the font size of a component.
	 * @param component The component that needs size changing.
	 * @param size The size to change it to.
	 */
	public static void setFontSize(Component component, float size)
	{
		component.setFont(component.getFont().deriveFont(size));
	}
	public static void setToolTip(List<? extends JComponent> components, List<String> componentText)
	{
		assert components.size() == componentText.size();
		for(int i = 0; i < components.size(); i++)
		{
			components.get(i).setToolTipText(componentText.get(i));
		}
	}
	
	public List<JButton> getBoardButtons()
	{
		return boardDisplay.getButtons();
	}

	public void destroyBoard()
	{
		if(boardDisplay!= null)
		{
			boardDisplay.dispose();
		}
		boardDisplay = null;
	}

	public void addBoardKeyListener(KeyListener keyListener)
	{
		boardDisplay.addKeyListener(keyListener);
	}

	public void addBoardMouseListener(MouseListener mouseListener)
	{
		boardDisplay.getBoardPane().addMouseListener(mouseListener);
	}
	
	public void newBoard(Image[][] boardImages, Map<Image,Cell> pieceLocations)
	{
		boardDisplay = new BoardFrame(boardImages, pieceLocations, actionOnCloseButton);
	}
	
	/**
	 * Destroy the window, and set visible to false.
	 */
	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}	
}