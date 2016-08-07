package cluedo.userinterface;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class BoardFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	static final int WIDTH = 1000;
	static final int HEIGHT = 1000;
	BoardCanvas board;
	public BoardFrame()
	{
		this.setTitle("Cluedo Game");
		this.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		board = new BoardCanvas();
		JPanel bottom = new JPanel();
		this.add(board);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		setResizable(false);
	}
	public static void main(String[] args){
		new BoardFrame();
	}
}
