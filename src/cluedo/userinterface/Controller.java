package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.AbstractMap.SimpleEntry;
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
			Optional<SimpleEntry<List<Player>, List<String>>> activePlayers = createPlayers();
			
			if (activePlayers.isPresent())
			{
				List<Player> p = activePlayers.get().getKey();
				List<String> s = activePlayers.get().getValue();
				
				for (int i = 0; i < p.size(); i++)
				{
					System.out.println(String.format("%s: %s", p.get(i).getName(), s.get(i)));
				}
			}
			
		});

		view.buttonQuitListener((a) -> {
			if (view.yesNo("Are you sure?", "Do you want to quit?"))
			{
				System.exit(0);
			}
		});

	}
	
	/**
	 * Get user input on the characters that each player wants to play.
	 * @return Optional.of the players in turn order that each player wants to play.
	 * Optiona.empty() if the user canceled.
	 */
	private Optional<SimpleEntry<List<Player>, List<String>>> createPlayers()
	{
		List<Player> activePlayers =  new ArrayList<Player>(Game.allPlayers.size());
		List<Boolean> availablePlayers = new ArrayList<Boolean>(activePlayers.size());
		List<String> playerNames = new ArrayList<String>(activePlayers.size());
		
		fillBoolean(availablePlayers, Game.allPlayers.size(), true);
		
		while (activePlayers.size() < Game.MIN_HUMAN_PLAYERS // Always ask until we have the minimum number
		// Once we have the min, and less than the max, only continue if the players want to
		|| (activePlayers.size() < Game.MAX_HUMAN_PLAYERS && view.yesNo("Any more players?", "Do you want to add more players? You currently have " + activePlayers.size())))
		{
			Optional<String> name = promptUserName();
			Optional<Integer> selectedPlayerIndex = promptUserCharacterIndex(Arrays.asList(GameBuilder.SUSPECT_NAMES), availablePlayers);
			Optional<Player> currentPlayer = getPlayerFromIndex(selectedPlayerIndex);

			if (!selectedPlayerIndex.isPresent() || !currentPlayer.isPresent() || !name.isPresent())
			{
				break;
			}
			
			activePlayers.add(currentPlayer.get());
			playerNames.add(name.get());

			availablePlayers.remove((int) selectedPlayerIndex.get()); // Integer is treated as .remove(Object x) but we want .remove(int x) so we need a cast.
			availablePlayers.add(selectedPlayerIndex.get(), false);
		}
		
		if (activePlayers.size() >= Game.MIN_HUMAN_PLAYERS)
		{
			assert activePlayers.size() == playerNames.size();
			SimpleEntry<List<Player>, List<String>> pair = new SimpleEntry<List<Player>, List<String>>(activePlayers, playerNames);
			return Optional.of(pair);
		}
		else
		{
			return Optional.empty();
		}
	}
	
	private Optional<String> promptUserName()
	{
		return view.dialogTextInput("Player name", "What is your name?");
	}
	
	private Optional<Integer> promptUserCharacterIndex(List<String> playerNames, List<Boolean> availablePlayers)
	{
		return view.dialogRadioButtons("Select a player", "Which player would you like?", playerNames, availablePlayers);
	}
	
	private Optional<Player> getPlayerFromIndex(Optional<Integer> index)
	{
		return index.isPresent() ? Optional.of(Game.allPlayers.get(index.get())) : Optional.empty();
	}
	
	private void fillBoolean(List<Boolean> list, int size, boolean value)
	{
		for (int i = 0; i < size; i++)
		{
			list.add(value);
		}
	}
	
	/**
	 * Creates the board as it is to be drawn, based on Cells and their properties.
	 * @return The array of Images that represent each Cell.
	 */
	public Image[][] getImages()
	{
		assert model != null : "Cannot get cells for an empty game";
		
		Image[][] images = new Image[Board.WIDTH][Board.HEIGHT];
		Cell[][] cells = model.getCells();
		
		Set<Cell> outOfBounds = model.getOutOfBoundCells();
		Set<Cell> roomCells = model.getRoomCells();
		Set<Cell> secretPassage = model.getSecretPassageCells();
		Set<Cell> doorCells = model.getDoorCells();
		
		for (int x = 0; x < Board.WIDTH; x++)
		{
			for (int y = 0; y < Board.HEIGHT; y++)
			{
				Image image = null;
				
				Cell cell = cells[x][y];
				int startX = x * BoardCanvas.cellWidth;
				int startY = y * BoardCanvas.cellHeight;
				
				Rectangle rec = new Rectangle(startX, startY, BoardCanvas.cellWidth, BoardCanvas.cellHeight);
				
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
				
				assert image != null: "Should have an image to add into the array";
				
				images[x][y] = image;
			}
		}
		return images;
	}
	
	/**
	 * Convert a rectangle and colour into an image.
	 * @param rectangle The rectangle to convert into an image.
	 * @param colour The colour of the image.
	 * @return The resulting image.
	 */
	private Image convertToImage(Rectangle rectangle, Color colour)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.cellWidth, BoardCanvas.cellHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		
		graphics.setColor(colour);
		graphics.fill(rectangle);
		
		return image;
	}
	
	/**
	 * As with convertToImage, but the resulting image has a black outline.
	 * @param rectangle The rectangle to convert to an image.
	 * @param colour The colour of the image.
	 * @return The resulting image.
	 */
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