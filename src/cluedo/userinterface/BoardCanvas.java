package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics;
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
	private Rectangle[][] rectangles;
	private Color[][] rectangleColours;
	
	
	public BoardCanvas(Color[][] colors) {
		super();
		this.rectangleColours = colors;
		rectangles = new Rectangle[Board.WIDTH][Board.HEIGHT];
		int x = 0;
		int y = 0;
		for(int i = 0; i < Board.WIDTH; i++){
			for(int j = 0; j < Board.HEIGHT; j++){
				Rectangle rec = new Rectangle(x,y,cellWidth,cellHeight);
				rectangles[i][j] = rec;
				y+= cellHeight;
			}
			y = 0;
			x+= cellWidth;
		}
	}

	protected void paintComponent(Graphics g){
		int x = 0;	
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
		}
	}
}
