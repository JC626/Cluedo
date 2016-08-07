package cluedo.userinterface;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.sun.glass.events.KeyEvent;


public class BoardFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	static final int WIDTH = 1000;
	static final int HEIGHT = 1000;
	BoardCanvas board;
	public BoardFrame()
	{
		this.setTitle("Cluedo Game");
		this.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		JMenuBar menuBar = createMenu();
		this.setJMenuBar(menuBar);
		board = new BoardCanvas();
		List<JButton> buttons = createButtons();
		setFontSizeButtons(buttons);
		JPanel bottom = createBottomPanel(buttons);
		this.add(board, BorderLayout.CENTER);
		this.add(bottom,BorderLayout.PAGE_END);
		bottom.setBorder(BorderFactory.createEmptyBorder(20, 20, 50, 20));
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
		JMenuItem casefile = new JMenuItem("View CaseFile");
		JMenuItem suggestion = new JMenuItem("Make Suggestion");
		JMenuItem accusation = new JMenuItem("Make Accusation");
		JMenuItem endTurn = new JMenuItem("End Turn");
		
		file.add(newGame);
		file.add(quit);
		menu.add(file);
		
		JMenu actions = new JMenu("Game Actions");
		actions.add(casefile);
		actions.add(suggestion);
		actions.add(accusation);
		actions.add(endTurn);
		menu.add(actions);
		return menu;
	}
	private List<JButton> createButtons()
	{
		List<JButton> buttons = new ArrayList<JButton>();
		buttons.add(new JButton("View CaseFile"));
		buttons.add(new JButton("Make Suggestion"));
		buttons.add(new JButton("Make Accusation"));
		buttons.add(new JButton("End Turn"));
		return buttons;
	}
	private JPanel createBottomPanel(List<JButton> buttons)
	{
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(WIDTH, HEIGHT - BoardCanvas.HEIGHT));
		panel.setLayout(new GridLayout(0, buttons.size(),20,0));
		for(JButton button : buttons)
		{
			panel.add(button);
		}
		return panel;
	}
	private void setFontSizeButtons(List<JButton> buttons)
	{
		for(JButton button : buttons)
		{
			button.setFont(button.getFont().deriveFont(18.0f));	
		}
	}
	public static void main(String[] args){
		new BoardFrame();
	}
}
