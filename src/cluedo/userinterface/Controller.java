package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cluedo.board.Board;
import cluedo.game.Game;
import cluedo.game.GameBuilder;
import cluedo.model.Cell;
import cluedo.model.Player;

public class Controller
{
	Game model;
	GraphicalUserInterface view;

	public Controller()
	{
		this.view = new GraphicalUserInterface();

		view.buttonNewGameListener((a) -> {
			List<Player> activePlayers = new ArrayList<Player>();
			List<Boolean> availablePlayers = new ArrayList<Boolean>(Game.allPlayers.size());

			for (Player p : Game.allPlayers)
			{
				availablePlayers.add(true);
			}
			List<String> playerNames = Arrays.asList(GameBuilder.SUSPECT_NAMES);
			
			while (activePlayers.size() < Game.MIN_HUMAN_PLAYERS // Always ask until we have the minimum number
					// Once we have the min, and less than the max, only continue if the players want to
					|| (activePlayers.size() < Game.MAX_HUMAN_PLAYERS && view.yesNo("Any more players?", "Do you want to add more players? You currently have " + activePlayers.size())))
			{
				Optional<Integer> optionalSelectedPlayerIndex = view.dialogRadioButtons("Select a player", "Which player would you like?", playerNames, availablePlayers);

				if (!optionalSelectedPlayerIndex.isPresent())
				{
					return;
				}
				
				int selectedPlayerIndex = optionalSelectedPlayerIndex.get();
				Player currentPlayer = Game.allPlayers.get(selectedPlayerIndex);
				
				activePlayers.add(currentPlayer);
			
				availablePlayers.remove(selectedPlayerIndex);
				availablePlayers.add(selectedPlayerIndex, false);
			}
		});

		view.buttonQuitListener((a) -> {
			if (view.yesNo("Are you sure?", "Do you want to quit?"))
			{
				System.exit(0);
			}
		});

	}
	public Image[][] getImages()
	{
		assert model != null : "Cannot get cells for an empty game";
		Image[][] images = new Image[Board.WIDTH][Board.HEIGHT];
		for(int x = 0; x < Board.WIDTH; x++)
		{
			for(int y = 0; y < Board.HEIGHT; y++)
			{
				//Create rectangles here
				//Add images here
				//images[x][y] = convertToImage(rectangle);
			}
		}
		return images;
	}
	private Image convertToImage(Rectangle rectangle)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.cellWidth, BoardCanvas.cellHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.draw(rectangle);
		return image;
	}
}