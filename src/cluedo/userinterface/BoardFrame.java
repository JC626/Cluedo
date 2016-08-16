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
 * Picture of the cluedo board
 * Dice Images for the player to know how many moves they have
 * Buttons for player to select game actions in the bottom right
 *
 */
public class BoardFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public static final int MIN_WIDTH = 1100; 
	public static final int MIN_HEIGHT = 1000;
	private static final int BUTTON_FONT_SIZE = 16;
	private static final int MIN_BOTTOM_HEIGHT = MIN_HEIGHT - BoardCanvas.MIN_HEIGHT;
	private static final int MIN_BOTTOM_RIGHT_WIDTH = (int)(MIN_WIDTH * 0.6);
	private static final int MIN_DICE_WIDTH = (int)(MIN_WIDTH * 0.4);
	private static final int HORIZONTAL_GAP = 15;


	private BoardCanvas boardPane;
	private DiceCanvas dicePane;
	private JPanel bottom;
	
	private JButton handButton;
	private JButton casefileButton;
	private JButton suggestionButton;
	private JButton accusationButton;
	private JButton endTurnButton;
	
	private JMenuItem handMenu;
	private JMenuItem casefileMenu;
	private JMenuItem suggestionMenu;
	private JMenuItem accusationMenu;
	private JMenuItem endTurnMenu;
	private JMenuItem newGame;
	private JMenuItem quit;
	
	public BoardFrame(Image[][] boardImages, Map<Image,Cell> pieceLocations)
	{
		JMenuBar menuBar = createMenu();
		List<JButton> buttons = createButtons();
		boardPane = new BoardCanvas(boardImages,pieceLocations);
	
		dicePane = new DiceCanvas(MIN_DICE_WIDTH,MIN_BOTTOM_HEIGHT);
		
		JPanel bottomRight = createButtonPanel(buttons);
		bottom = new JPanel(new FlowLayout(FlowLayout.LEADING,HORIZONTAL_GAP,0));
		bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));

		bottom.add(dicePane);
		bottom.add(bottomRight);
		
		setFontSizeButtons(buttons);

		this.setTitle("Cluedo Game");
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		this.setJMenuBar(menuBar);
		
		this.add(boardPane, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.PAGE_END);
		
		//Ensure the frame has the focus so that the keyboard listener will work
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		       BoardFrame.this.requestFocusInWindow();
		    }
		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		setVisible(true);
		setResizable(false);
	}
	
	/**
	 * Create menu options
	 * Menu options are attached shortcuts too
	 * @return MenuBar for the frame
	 */
	private JMenuBar createMenu()
	{
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		this.newGame = new JMenuItem("New Game");
		this.quit = new JMenuItem("Quit");
		file.add(newGame);
		file.add(quit);
		menu.add(file);
		
		JMenu actions = new JMenu("Game Actions");
		this.handMenu = new JMenuItem("View Hand");
		this.casefileMenu = new JMenuItem("View CaseFile");
		this.suggestionMenu = new JMenuItem("Make Suggestion");
		this.accusationMenu = new JMenuItem("Make Accusation");
		this.endTurnMenu = new JMenuItem("End Turn");
		
		actions.add(handMenu);
		actions.add(casefileMenu);
		actions.add(suggestionMenu);
		actions.add(accusationMenu);
		actions.add(endTurnMenu);
		menu.add(actions);
		
		//Setting shortcuts
		newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)); 
		handMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));
		casefileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
		suggestionMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		endTurnMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
		
		return menu;
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
		panel.setLayout(new GridLayout(0, buttons.size(),HORIZONTAL_GAP,0));
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
	
	public BoardCanvas getBoardPane() {
		return boardPane;
	}

	public DiceCanvas getDicePane() {
		return dicePane;
	}
}