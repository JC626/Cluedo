package cluedo.userinterface;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MenuFrame extends JFrame
{
	public static final int WIDTH = 500;
	public static final int HEIGHT = 500;
	
	private JPanel canvas;
		
	public MenuFrame()
	{
		super("Cluedo");
		
		canvas = new JPanel();
		canvas.setLayout(new GridLayout(0,1,0,40));
		canvas.setBorder(BorderFactory.createEmptyBorder(20, 75, 60, 75));
		JLabel title = new JLabel("Cluedo");
		//TODO fancy title font?
		//Font titleFont = new Font("Helvetica", Font.BOLD, 80);
		System.out.println(title.getFont().getFontName());
		title.setSize(200, 200);
		title.setFont(title.getFont().deriveFont(80.0f));
		JButton newGame = new JButton("New Game");
		newGame.setFont(newGame.getFont().deriveFont(30.0f));
		JButton quit = new JButton("Quit");
		quit.setFont(quit.getFont().deriveFont(30.0f));
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
