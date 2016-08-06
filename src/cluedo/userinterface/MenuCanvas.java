package cluedo.userinterface;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MenuCanvas extends JPanel
{
	private static final long serialVersionUID = 1L;
	private boolean visible = true;

	public MenuCanvas()
	{
		JTextArea title = new JTextArea("Cluedo");
		JButton newGame = new JButton("New Game");
		JButton quit = new JButton("Quit");

		quit.addActionListener(
				e -> {
					if(ConfirmationDialog.yesNo("Do you want to quit?", "Do you want to quit?"))
					{
						System.exit(0);
					}
				});

		title.setEditable(false);

		add(title);
		add(newGame);
		add(quit);
	}


	public boolean isVisible()
	{
		return visible;
	}

}
