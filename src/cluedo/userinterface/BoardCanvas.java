package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import cluedo.board.Board;

public class BoardCanvas extends JPanel {
	private static final long serialVersionUID = 1L;
	static final int HEIGHT = 800;
	public static final int cellWidth = BoardFrame.WIDTH/24;
	public static final int cellHeight = HEIGHT/25;
	private Image[][] boardImages;
	
	
	public BoardCanvas(Image[][] boardImages) {
		super();
		this.boardImages = boardImages;
	}

	protected void paintComponent(Graphics g){
		for(int x = 0; x < Board.WIDTH; x++){
			for(int y = 0; y < Board.HEIGHT; y++){
				Image image = boardImages[x][y];
				g.drawImage(image, x*cellWidth, y*cellHeight, null);
			}

		}
	}
}
