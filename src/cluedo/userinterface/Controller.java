package cluedo.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import com.sun.javafx.scene.paint.GradientUtils.Point;

import cluedo.board.Board;
import cluedo.game.Game;
import cluedo.game.GameBuilder;
import cluedo.model.Cell;
import cluedo.model.Player;
import cluedo.model.Weapon;

public class Controller
{
	Game model;
	GraphicalUserInterface view;
	private static final Color NORMAL_CELL_COLOUR = new Color(255,248,111);
	private static final Color OUT_OF_BOUNDS_COLOR = Color.DARK_GRAY;
	private static final Color ROOM_COLOR = new Color(206,218,224);
	private static final Color ENTRANCE_COLOR = Color.GREEN;
	private static final Color SECRET_PASSAGE_COLOR = Color.MAGENTA;
	
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
				model = new Game(p,s);
				BoardFrame board = new BoardFrame(getImages(),initialisePieces());
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
		Set<Cell> entranceCells = model.getDoorCells();
		Image secretPassageImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, SECRET_PASSAGE_COLOR);
		Image outOfBoundsImage = convertToImageWithOutline(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, OUT_OF_BOUNDS_COLOR);
		Image roomCellImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, ROOM_COLOR);
		Image entranceImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, ENTRANCE_COLOR);
		Image cellImage = convertToImageWithOutline(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, NORMAL_CELL_COLOUR);
		
		for (int x = 0; x < Board.WIDTH; x++)
		{
			for (int y = 0; y < Board.HEIGHT; y++)
			{
				
				Cell cell = cells[x][y];	
				if(secretPassage.contains(cell))
				{
					images[x][y] = secretPassageImage;
				}
				else if(roomCells.contains(cell))
				{
					images[x][y] = roomCellImage;
				}
				else if(entranceCells.contains(cell))
				{
					images[x][y] = entranceImage;
				}
				else if(outOfBounds.contains(cell))
				{
					images[x][y] = outOfBoundsImage;
				}
				else
				{
					images[x][y] = cellImage;
				}		
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
	private Image convertToImage(int x, int y, int width, int height, Color colour)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		
		graphics.setColor(colour);
		graphics.fillRect(x, y, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT);
		
		return image;
	}
	
	/**
	 * As with convertToImage, but the resulting image has a black outline.
	 * @param rectangle The rectangle to convert to an image.
	 * @param colour The colour of the image.
	 * @return The resulting image.
	 */
	private Image convertToImageWithOutline(int x, int y, int width, int height, Color colour)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		
		graphics.setColor(colour);
		graphics.fillRect(x, y, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT);
		graphics.setColor(Color.BLACK);
		graphics.drawRect(x, y, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT);
		return image;
	}
	
	/**
	 * Get all the pieces and their locations from the game
	 * @return the images of pieces and their locations
	 */
	private Map<Image,Cell> initialisePieces()
	{
		assert model != null;
		Map<Image, Cell> pieces = new HashMap<Image,Cell>(); 
		List<Weapon> weapons = model.getWeapons();
		for(Weapon weapon : weapons)
		{
			String weaponName = weapon.getName();
			pieces.put(getPieceImage(weaponName), model.getPosition(weapon));
		}
		for(Player player : Game.allPlayers)
		{
			String playerName = player.getName();
			pieces.put(getPieceImage(playerName), model.getPosition(player));
		}
		return pieces;
	}
	
	private Image getPieceImage(String name)
	{
		String[] weaponNames = GameBuilder.WEAPON_NAMES;
		String[] suspectNames = GameBuilder.SUSPECT_NAMES;
		for(String suspect : suspectNames)
		{
			if(suspect.equals(name))
			{
				String fileName = "cluedo_images\\" + suspect + ".png";
				try {
					return ImageIO.read(new File(fileName));
				} catch (IOException e) {
					throw new RuntimeException(fileName + " not found");
				}
			}
		}
		for(String weapon : weaponNames)
		{
			if(weapon.equals(name))
			{
				String fileName = "cluedo_images\\" + weapon + ".png";
				try {
					return ImageIO.read(new File(fileName));
				} catch (IOException e) {
					throw new RuntimeException(fileName + " not found");
				}
			}
		}
		throw new IllegalArgumentException("No such image for that name");
	}
	
}