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
import java.util.Set;

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
			Optional<List<Player>> activePlayers = createPlayers();
		});

		view.buttonQuitListener((a) -> {
			if (view.yesNo("Are you sure?", "Do you want to quit?"))
			{
				System.exit(0);
			}
		});

	}
	
	private Optional<List<Player>> createPlayers()
	{
		List<Player> activePlayers =  new ArrayList<Player>();
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
				return Optional.empty();
			}

			int selectedPlayerIndex = optionalSelectedPlayerIndex.get();
			Player currentPlayer = Game.allPlayers.get(selectedPlayerIndex);

			activePlayers.add(currentPlayer);

			availablePlayers.remove(selectedPlayerIndex);
			availablePlayers.add(selectedPlayerIndex, false);
		}
		
		return Optional.of(activePlayers);
	}
	
	public Image[][] getImages()
	{
		assert model != null : "Cannot get cells for an empty game";
		Image[][] images = new Image[Board.WIDTH][Board.HEIGHT];
		Cell[][] cells = model.getCells();
		Set<Cell> outOfBounds = model.getOutOfBoundCells();
		Set<Cell> roomCells = model.getRoomCells();
		Set<Cell> secretPassage = model.getSecretPassageCells();
		Set<Cell> doorCells = model.getDoorCells();
		int startX = 0;
		int startY = 0;
		for(int x = 0; x < Board.WIDTH; x++)
		{
			for(int y = 0; y < Board.HEIGHT; y++)
			{
				//Create rectangles here
				Image image = null;
				Cell cell = cells[x][y];
				Rectangle rec = new Rectangle(startX,startY,BoardCanvas.cellWidth,BoardCanvas.cellHeight);
				if(secretPassage.contains(cell))
				{
					image = convertToImage(rec, Color.MAGENTA);
				}
				else if(roomCells.contains(cell))
				{
					image = convertToImage(rec, Color.LIGHT_GRAY);
				}
				else if(doorCells.contains(cell))
				{
					image = convertToImage(rec, Color.GREEN);
				}
				else if(outOfBounds.contains(cell))
				{
					image = convertToImageWithOutline(rec, Color.BLACK);
				}
				else
				{
					image = convertToImageWithOutline(rec, Color.YELLOW);
				}
				assert image != null: "Should have an image to add into the arrya";
				startY += BoardCanvas.cellHeight;
				images[x][y] = image;
			}
			startY = 0;
			startX+= BoardCanvas.cellWidth;
		}
		return images;
	}
	private Image convertToImage(Rectangle rectangle, Color colour)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.cellWidth, BoardCanvas.cellHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(colour);
		graphics.fill(rectangle);
		return image;
	}
	private Image convertToImageWithOutline(Rectangle rectangle, Color colour)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.cellWidth, BoardCanvas.cellHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(colour);
		graphics.fill(rectangle);
		graphics.setColor(Color.BLACK);
		graphics.draw(rectangle);
		return image;
	}



}