package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
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
		/*int x = 0;	
		int y = 0;
		for(int i = 0; i < Board.WIDTH; i++){
			for(int j = 0; j < Board.HEIGHT; j++){
				Rectangle rec = rectangles[i][j];
				g.setColor(rectangleColours[i][j]);
				g.fillRect(x,y,(int)rec.getWidth(),(int)rec.getHeight());
				g.setColor(Color.BLACK);
				//if color != cellColour . do not draw outline
				g.drawRect(x,y,(int)rec.getWidth(),(int)rec.getHeight());
				y+= cellHeight;
			}
			y = 0;
			x+= cellWidth;
		}*/
	}
}
