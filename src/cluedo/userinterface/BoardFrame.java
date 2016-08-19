package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import cluedo.model.Cell;

/**
 * Displays the Board window
 * Contains:
 * A menu bar
 * The Cluedo board with pieces.
 * Dice Images for the player to know how many moves they have
 * Buttons for player to select game actions (located in the bottom right)
 *
 */
public class BoardFrame extends JFrame
{
	public static final int MIN_WIDTH = 1200; 
	public static final int MIN_HEIGHT = 1000;
		
	private static final int BUTTON_FONT_SIZE = 16;
	
	private static final int MIN_BOTTOM_HEIGHT = MIN_HEIGHT - BoardCanvas.MIN_HEIGHT;
	private static final int MIN_BOTTOM_RIGHT_WIDTH = (int)(MIN_WIDTH * 0.6);
	private static final int MIN_DICE_WIDTH = (int)(MIN_WIDTH * 0.4);
	
	private static final int HORIZONTAL_GAP = 15;
	
	
	private List<JButton> buttons;
	
	private BoardCanvas boardPane;
	private DiceCanvas dicePane;
	private JPanel bottom;
	
	// User gameplay buttons
	private JButton handButton;
	private JButton casefileButton;
	private JButton suggestionButton;
	private JButton accusationButton;
	private JButton endTurnButton;
	
	// Menu options
	private JMenuItem handMenu;
	private JMenuItem casefileMenu;
	private JMenuItem suggestionMenu;
	private JMenuItem accusationMenu;
	private JMenuItem endTurnMenu;
	private JMenuItem newGame;
	private JMenuItem quit;
	
	/**
	 * A new frame that contains a visual representation of the Board.
	 * @param boardImages The images that make up the Board.
	 * @param pieceLocations The locations of the Pieces on the Board.
	 * @param actionOnCloseButton The actions to take on clicking the close button.
	 * Null will be passed in as the action event.
	 */
	public BoardFrame(Image[][] boardImages, Map<Image, Cell> pieceLocations, ActionListener actionOnCloseButton)
	{
		JMenuBar menuBar = createMenu();
		this.buttons = createButtons();
		boardPane = new BoardCanvas(boardImages, pieceLocations);
	
		dicePane = new DiceCanvas(MIN_DICE_WIDTH, MIN_BOTTOM_HEIGHT);
		
		JPanel bottomRight = createButtonPanel(buttons);
		bottom = new JPanel(new FlowLayout(FlowLayout.LEADING,HORIZONTAL_GAP, 0));
		bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));

		bottom.add(dicePane);
		bottom.add(bottomRight);
		
		setFontSizeButtons(buttons);

		this.setTitle("Cluedo Game");
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		this.setJMenuBar(menuBar);
		
		this.add(boardPane, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.PAGE_END);
		

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e)
		    {
				// Ensure the frame has the focus so that the keyboard listener will work
		    	BoardFrame.this.requestFocusInWindow();
		    }
		});
		
		// Close the window in the way that the Controller wants us to close it.
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionOnCloseButton.actionPerformed(null);
			}
		});
		
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	/**
	 * Create menu options
	 * Menu options are attached shortcuts too
	 * @return MenuBar for the frame
	 */
	private JMenuBar createMenu()
	{
		JMenu actions = new JMenu("Game Actions");
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		newGame = new JMenuItem("New Game");
		quit = new JMenuItem("Quit");
		
		file.add(newGame);
		file.add(quit);
		
		menu.add(file);
		
		
		handMenu = new JMenuItem("View Hand");
		casefileMenu = new JMenuItem("View CaseFile");
		suggestionMenu = new JMenuItem("Make Suggestion");
		accusationMenu = new JMenuItem("Make Accusation");
		endTurnMenu = new JMenuItem("End Turn");
		
		actions.add(handMenu);
		actions.add(casefileMenu);
		actions.add(suggestionMenu);
		actions.add(accusationMenu);
		actions.add(endTurnMenu);
		
		menu.add(actions);
		
		//Setting shortcuts
		setShortcut(newGame, KeyEvent.VK_N);
		setShortcut(quit, KeyEvent.VK_ESCAPE);
		setShortcut(handMenu, KeyEvent.VK_H);
		setShortcut(casefileMenu, KeyEvent.VK_C);
		setShortcut(suggestionMenu, KeyEvent.VK_SPACE);
		setShortcut(endTurnMenu, KeyEvent.VK_T);
		
		return menu;
	}
	
	private void setShortcut(JMenuItem item, int event)
	{
		item.setAccelerator(KeyStroke.getKeyStroke(event, 0));
	}
	
	/**
	 * Create buttons for the bottom right panel
	 * @return List buttons for the bottom right panel
	 */
	private List<JButton> createButtons()
	{
		List<JButton> buttons = new ArrayList<JButton>();
		
		handButton = new JButton("View Hand");
		casefileButton = new JButton("View CaseFile");
		suggestionButton = new JButton("Make Suggestion");
		accusationButton = new JButton("Make Accusation");
		endTurnButton = new JButton("End Turn");
		
		buttons.add(handButton);
		buttons.add(casefileButton);
		buttons.add(suggestionButton);
		buttons.add(accusationButton);
		buttons.add(endTurnButton);
		
		return buttons;
	}
	
	/**
	 * Creates a panel for the buttons
	 * These buttons are the game actions
	 * that users can select when playing Cluedo
	 * @param buttons - List of Buttons for game actions
	 * @return The bottom right panel which contains the buttons
	 */
	private JPanel createButtonPanel(List<JButton> buttons)
	{
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(MIN_BOTTOM_RIGHT_WIDTH, MIN_BOTTOM_HEIGHT)); 
		panel.setLayout(new GridLayout(0, buttons.size(), HORIZONTAL_GAP, 0));
		
		for(JButton button : buttons)
		{
			panel.add(button);
		}
		return panel;
	}
	
	/**
	 * Set font size for all the buttons to be consistent
	 * @param buttons
	 */
	private void setFontSizeButtons(List<JButton> buttons)
	{
		for(JButton button : buttons)
		{
			GraphicalUserInterface.setFontSize(button, BUTTON_FONT_SIZE);
		}
	}
	
	
	/*
	 * Getters
	 */
	
	public BoardCanvas getBoardPane()
	{
		return boardPane;
	}

	public DiceCanvas getDicePane()
	{
		return dicePane;
	}

	public List<JButton> getButtons()
	{
		return Collections.unmodifiableList(buttons);
	}
	
	
	/*
	 * Listener adders
	 */
	
	public void addHandListener(ActionListener a)
	{
		handButton.addActionListener(a);
		handMenu.addActionListener(a);
	}
	
	public void addCasefileListener(ActionListener a)
	{
		casefileButton.addActionListener(a);
		casefileMenu.addActionListener(a);
	}
	
	public void addSuggestionListener(ActionListener a)
	{
		suggestionButton.addActionListener(a);
		suggestionMenu.addActionListener(a);
	}
	
	public void addAccusationListener(ActionListener a)
	{
		accusationButton.addActionListener(a);
		accusationMenu.addActionListener(a);
	}
	
	public void addEndTurnListener(ActionListener a)
	{
		endTurnButton.addActionListener(a);
		endTurnMenu.addActionListener(a);
	}
	
	public void addNewGameListener(ActionListener a)
	{
		newGame.addActionListener(a);
	}
	
	public void addQuitListener(ActionListener a)
	{
		quit.addActionListener(a);
	}
}