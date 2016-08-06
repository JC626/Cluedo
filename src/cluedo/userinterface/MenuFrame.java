package cluedo.userinterface;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

public class MenuFrame extends JFrame
{
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	
	MenuCanvas canvas;
		
	public MenuFrame()
	{
		super("Cluedo");
		
		canvas = new MenuCanvas();
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		pack();
		setResizable(false);
		setVisible(true);
	}
}
