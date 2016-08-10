package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

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
	
	public static final int MIN_WIDTH = 1000; 
	public static final int MIN_HEIGHT = 1000;
	private static final int BUTTON_FONT_SIZE = 18;
	private static final int MIN_BOTTOM_HEIGHT = MIN_HEIGHT - BoardCanvas.MIN_HEIGHT;
	private static final int MIN_BOTTOM_RIGHT_WIDTH = (int)(MIN_WIDTH * 0.7);
	private static final int MIN_DICE_WIDTH = (int)(MIN_WIDTH * 0.3);

	private BoardCanvas board;
	private DiceCanvas dice;
	private JPanel bottom;
	
	public BoardFrame(Image[][] boardImages, Map<Image,Cell> pieceLocations)
	{
		JMenuBar menuBar = createMenu();
		List<JButton> buttons = createButtons();
		board = new BoardCanvas(boardImages,pieceLocations);
		
		//Just so that diceCanvas works.
		//TODO take dice images in BoardFrame constructor?
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("test.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert myPicture != null;
		dice = new DiceCanvas(myPicture, myPicture, MIN_DICE_WIDTH,MIN_BOTTOM_HEIGHT);
		
		JPanel bottomRight = createBottomRightPanel(buttons);
		bottom = new JPanel(new FlowLayout(FlowLayout.LEADING,20,0));
		bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));

		bottom.add(dice);
		bottom.add(bottomRight);
		
		setFontSizeButtons(buttons);
	
		this.setTitle("Cluedo Game");
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		this.setJMenuBar(menuBar);
		
		this.add(board, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.PAGE_END);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		setVisible(true);
		setResizable(false);
	}
	
	/**
	 * Create menu options
	 * @return MenuBar for the frame
	 */
	private JMenuBar createMenu()
	{
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		//file.setMnemonic(KeyEvent.VK_A); //Setting shortcut
		JMenuItem newGame = new JMenuItem("New Game");
		JMenuItem quit = new JMenuItem("Quit");
		file.add(newGame);
		file.add(quit);
		menu.add(file);
		//TODO add listeners for menu items
		JMenu actions = new JMenu("Game Actions");
		JMenuItem casefile = new JMenuItem("View CaseFile");
		JMenuItem suggestion = new JMenuItem("Make Suggestion");
		JMenuItem accusation = new JMenuItem("Make Accusation");
		JMenuItem endTurn = new JMenuItem("End Turn");
		actions.add(casefile);
		actions.add(suggestion);
		actions.add(accusation);
		actions.add(endTurn);
		menu.add(actions);
		
		return menu;
	}
	
	/**
	 * Create buttons for the bottom right panel
	 * @return List buttons for the bottom right panel
	 */
	private List<JButton> createButtons()
	{
		List<JButton> buttons = new ArrayList<JButton>();
		
		buttons.add(new JButton("View CaseFile"));
		buttons.add(new JButton("Make Suggestion"));
		buttons.add(new JButton("Make Accusation"));
		buttons.add(new JButton("End Turn"));
		
		return buttons;
	}
	/**
	 * Creates a panel for the buttons
	 * These buttons are the game actions
	 * that users can select when playing Cluedo
	 * @param buttons - List of Buttons for game actions
	 * @return The bottom right panel
	 */
	private JPanel createBottomRightPanel(List<JButton> buttons)
	{
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(MIN_BOTTOM_RIGHT_WIDTH, MIN_BOTTOM_HEIGHT)); 
		panel.setLayout(new GridLayout(0, buttons.size(),15,0));
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
}