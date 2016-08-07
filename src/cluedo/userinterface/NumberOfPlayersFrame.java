package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NumberOfPlayersFrame extends JFrame{
	public static final int WIDTH = 500;
	public static final int HEIGHT = 500;
	
	private JPanel canvas;
		
	public NumberOfPlayersFrame()
	{
		super("Select Number of Players");
		canvas = new JPanel();
		canvas.setLayout(new GridLayout(0, 1,0,15));
		canvas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		
		JLabel title = new JLabel("Select the number of players");
		title.setFont(title.getFont().deriveFont(30.0f));
		JButton three = new JButton("3");
		three.setFont(three.getFont().deriveFont(25.0f));
		JButton four = new JButton("4");
		four.setFont(four.getFont().deriveFont(25.0f));
		JButton five = new JButton("5");
		five.setFont(five.getFont().deriveFont(25.0f));
		JButton six = new JButton("6");
		six.setFont(six.getFont().deriveFont(25.0f));

		canvas.add(title);
		canvas.add(three);
		canvas.add(four);
		canvas.add(five);
		canvas.add(six);
		this.add(canvas);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(false);
		setResizable(false);
	}
}
