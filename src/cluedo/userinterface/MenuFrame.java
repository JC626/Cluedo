package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MenuFrame extends JFrame
{
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	
	private JPanel canvas;
		
	public MenuFrame()
	{
		super("Cluedo");
		
		canvas = new JPanel();
		canvas.setLayout(new GridLayout(0,1,0,40));
		canvas.setBorder(BorderFactory.createEmptyBorder(20, 75, 50, 75));
		JLabel title = new JLabel("Cluedo");
		title.setSize(200, 200);
		title.setFont(title.getFont().deriveFont(70.0f));
		JButton newGame = new JButton("New Game");
		JButton quit = new JButton("Quit");
		
		quit.addActionListener(
				e -> {
					if(ConfirmationDialog.yesNo("Are you sure?", "Do you want to quit?"))
					{
						System.exit(0);
					}
				});
		
		canvas.add(title);
		canvas.add(newGame);
		canvas.add(quit);
		this.add(canvas);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		pack();
		setResizable(false);
		setVisible(true);
	}
}
