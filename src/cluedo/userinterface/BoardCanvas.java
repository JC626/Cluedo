package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class BoardCanvas extends JPanel {
	private static final long serialVersionUID = 1L;
	static final int HEIGHT = 700;
	
	protected void paintComponent(Graphics g){
		List<Rectangle> rectangles = new ArrayList<Rectangle>();
		//Need to discern between room cells, entrance and exit cell rectangles
		int x = 0;
		int y = 0;
		int cellWidth = BoardFrame.WIDTH/24;
		int cellHeight = HEIGHT/25;
		boolean alternate = false; //Just to make checkerboard
		for(int i = 0; i < 25; i++){
			for(int j = 0; j < 24; j++){
				if(alternate)
				{
					g.setColor(Color.GRAY);
				}
				else{
					g.setColor(Color.LIGHT_GRAY);
				}
				rectangles.add(new Rectangle(x,y,cellWidth,cellHeight));
				g.fillRect(x,y,cellWidth,cellHeight);
				x+= cellWidth;
				alternate = !alternate;
			}
			alternate = !alternate;
			x = 0;
			y+= cellHeight;
		}
	}
}
