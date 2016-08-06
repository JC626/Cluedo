package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PlayerSelectionFrame extends JFrame {
	public static final int WIDTH = 500;
	public static final int HEIGHT = 500;
	private JPanel canvas;
	
	public PlayerSelectionFrame()
	{
		super("Select Number of Players");
		canvas = new JPanel();
		canvas.setLayout(new GridLayout(0, 1,0,10));
		canvas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		
		String text = "Player X please enter your name"; //TODO Need to grab number (X) from controller
		JLabel nameSelection = new JLabel(text);
		nameSelection.setFont(nameSelection.getFont().deriveFont(25.0f));
		
		JTextField nameInput = new JTextField();
		nameInput.setFont(nameInput.getFont().deriveFont(40.0f));
		//TODO set nameInput text limit?
		
		JLabel characterSelection = new JLabel("Choose a character");
		characterSelection.setFont(characterSelection.getFont().deriveFont(25.0f));
		String[] characters = { "White", "Peacock"}; 
		//TODO actual character names here
		//TODO Need to remove characters that have been picked
		JComboBox<String> characterBox = new JComboBox<String>(characters);
		characterBox.setFont(characterBox.getFont().deriveFont(30.0f));

		canvas.add(nameSelection);
		canvas.add(nameInput);
		canvas.add(characterSelection);
		canvas.add(characterBox);
		this.add(canvas);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(false);
		setResizable(false);
	}
}
