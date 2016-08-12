package cluedo.userinterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
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

import cluedo.board.Board;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.exceptions.NoAvailableExitException;
import cluedo.game.Game;
import cluedo.game.GameBuilder;
import cluedo.model.Cell;
import cluedo.model.Player;
import cluedo.model.Weapon;
import cluedo.utility.Heading.Direction;

public class Controller
{
	Game model;
	GraphicalUserInterface view;
	BoardFrame board;
	
	private static final BasicStroke WALL_THICKNESS = new BasicStroke(7.0f);
	private static final Color NORMAL_CELL_COLOUR = new Color(255,248,111);
	private static final Color OUT_OF_BOUNDS_COLOR = Color.DARK_GRAY;
	private static final Color ROOM_COLOR = new Color(206,218,224);
	private static final Color EXIT_COLOR = Color.GREEN; //May require for later
	private static final Color SECRET_PASSAGE_COLOR = Color.MAGENTA;
	
	private static final Map<String,Image> PIECE_IMAGES = new HashMap<String,Image>();	
	
	private final ActionListener newGameListener;
	private final ActionListener quitListener;
	
	public Controller()
	{
		this.view = new GraphicalUserInterface();
		
		newGameListener = new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					Optional<SimpleEntry<List<Player>, List<String>>> activePlayers = createPlayers();
					
					if (activePlayers.isPresent())
					{
						List<Player> p = activePlayers.get().getKey();
						List<String> s = activePlayers.get().getValue();
						
						for (int i = 0; i < p.size(); i++)
						{
							System.out.println(String.format("%s: %s", p.get(i).getName(), s.get(i)));
						}
						//TODO set menu to invisible
						model = new Game(p,s);
						setupBoard();
					}
				}
			};

		quitListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (view.yesNo("Are you sure?", "Do you want to quit?"))
				{
					System.exit(0);
				}
			}
		};

		
		view.buttonNewGameListener(newGameListener);

		view.buttonQuitListener(quitListener);

	}
	
	/**
	 * Get user input on the characters that each player wants to play.
	 * @return Optional.of the players in turn order that each player wants to play.
	 * Optional.empty() if the user canceled.
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
			if(name.isPresent() && (name.get().length() == 0 || name.get().length() > 15)) //TODO character limit
			{
				view.error("Invalid name", "A name must be between 0 and 15 characters");
				continue;
			}
			//Cancelled or closed the dialog
			if(!name.isPresent())
			{
				break;
			}
			Optional<Integer> selectedPlayerIndex = promptUserCharacterIndex(Arrays.asList(GameBuilder.SUSPECT_NAMES), availablePlayers);
			Optional<Player> currentPlayer = getPlayerFromIndex(selectedPlayerIndex);

			if (!selectedPlayerIndex.isPresent() || !currentPlayer.isPresent())
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
	private void setupBoard()
	{
		this.board = new BoardFrame(getImages(),initialisePieces());
		//Setup initial player
		Player startPlayer = model.getCurrentPlayer();
		String startPlayerName = model.getHumanName(startPlayer);
		int[] startDiceRoll = model.getDiceRoll();
		Image d1 = getDiceImage(startDiceRoll[0]);
		Image d2 = getDiceImage(startDiceRoll[1]);
		board.getDicePane().changeDice(d1, d2);
		
		view.information(startPlayerName + "'s turn", startPlayerName + " it is your turn. You are playing as " + startPlayer.getName());
		
		//Add listeners here
		//TODO add button listeners here

		board.addNewGameListener(newGameListener);
		board.addQuitListener(quitListener);
		board.addEndTurnListener(endTurnListener());
		board.addCasefileListener(casefileListener());
		
		board.addKeyListener(keyListener());
		//Add mouselistener to board pane so extra height from the menu bar doesn't affect clicking position
		board.getBoardPane().addMouseListener(mouseListener());
	}
	
	private ActionListener endTurnListener()
	{
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(model.getRemainingMoves() > 0)
				{
					view.error("Cannot End Turn", "Must use all your moves before ending your turn");
					return;
				}
				model.nextTurn();
				Player player = model.getCurrentPlayer();
				String playerName = model.getHumanName(player);
				int[] diceRoll = model.getDiceRoll();
				Image leftDie = getDiceImage(diceRoll[0]);
				Image rightDie = getDiceImage(diceRoll[1]);
				board.getDicePane().changeDice(leftDie, rightDie);
				view.information(playerName + "'s turn", playerName + " it is your turn. You are playing as " + player.getName());
				//Show exits if the player is in the room
				if(model.isInRoom())
				{
					Image exitImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, EXIT_COLOR);
					try 
					{
						List<Cell> exitCells = model.getAvailableExits();
						board.getBoardPane().drawExitCells(exitCells, exitImage);
					} 
					catch (InvalidMoveException e1) 
					{
						throw new IllegalMethodCallException(e1.getMessage());
					} 
					catch (NoAvailableExitException e1) 
					{
						view.error("All exits blocked", "All exits are blocked so cannot move out of a room.");
					}
				}
			}
		};
		return listener;
	}
	
	private ActionListener casefileListener()
	{
		//TODO casefile listener
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		};
		return listener;
	}
	
	/**
	 * Create a keylistener for the board
	 * It is used for keys to be used to move 
	 * around the board
	 * @return A new keylistener
	 */
	private KeyListener keyListener()
	{
		KeyListener keyListener = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Anything?
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Anything?
			}

			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if(model.getRemainingMoves() == 0)
				{
					if(model.isInRoom())
					{
						view.error("Cannot Move", "You just entered a room!");
					}
					else
					{
						view.error("Cannot Move", "No remaining moves");
					}
				}
				else if(model.isInRoom())
				{
					view.error("Cannot Move With Keyboard", "You cannot move in a room. Please select a exit with the mouse instead");
				}
				else if(!model.canMove())
				{
					view.error("Cannot Move", "You cannot move as all paths are blocked");
				}
				else
				{
					//TODO remove dialog boxes if move is invalid?
					//Find direction player moved
					Direction direction = null;
					switch(code)
					{
					case KeyEvent.VK_UP:
						direction = Direction.North;
						break;
					case KeyEvent.VK_DOWN:
						direction = Direction.South;
						break;
					case KeyEvent.VK_LEFT:
						direction = Direction.West;
						break;
					case KeyEvent.VK_RIGHT:
						direction = Direction.East;
						break;
					}
					if(direction == null)
					{
						return;
					}
					//Move the player
					try 
					{
						Cell cell = model.move(direction);
						String playerName = model.getCurrentPlayer().getName();
						board.getBoardPane().changePieceLocation(getPieceImage(playerName), cell);
					} 
					catch (InvalidMoveException e1) 
					{
						view.error("Cannot Move", "You cannot move in that direction");
					}

					if(model.isInRoom())
					{
						view.information("Entered a room", "You have entered the " + model.getCurrentRoom().getName());
					}
				}
			}
		};		
		return keyListener;
	}
	
	/**
	 * Create a mouselistener for the board
	 * Used for selecting exits
	 * @return A new mouselistener
	 */
	private MouseListener mouseListener()
	{
		MouseListener mouseListener = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				if(model.isInRoom())
				{ 
					try 
					{
						//Find the corresponding cell 
						int x = arg0.getX() / BoardCanvas.CELL_WIDTH;
						int y = arg0.getY() / BoardCanvas.CELL_HEIGHT;
						List<Cell> exitCells = model.getAvailableExits();
						for(Cell cell : exitCells)
						{
							if(cell.getX() == x && cell.getY() == y)
							{
								Cell toExit = model.takeExit(cell);
								String playerName = model.getCurrentPlayer().getName();
								board.getBoardPane().changePieceLocation(getPieceImage(playerName), toExit);
								if(model.isInRoom())
								{
									view.information("Entered a room", "You used the secret passage to enter the " + model.getCurrentRoom().getName());
								}
							}
						}
					} 
					catch (InvalidMoveException e) 
					{
						throw new IllegalMethodCallException(e.getMessage());
					} 
					catch (NoAvailableExitException e) 
					{
						view.error("All exits blocked", "All exits are blocked so cannot move out of a room.");
					}

				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Anything?
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Anything?
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Anything?
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Anything?
			}
		};
		return mouseListener;
	}
	
	/**
	 * Create the rows for this table to with made up
	 * boolean values
	 * @param rows
	 * @return
	 */
	private String[][] createRows(Map<String,Boolean> rooms)
	{
		String[][] tableRows = new String[rooms.size()][2];
		int i = 0;
		for(Map.Entry<String, Boolean> row : rooms.entrySet())
		{
			assert i < tableRows.length;
			tableRows[i][0] = row.getKey();
			boolean crossed = row.getValue();
			if(crossed)
			{
				tableRows[i][1] = "X";
			}
			i++;
		}
		return tableRows;
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
		Image secretPassageImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, SECRET_PASSAGE_COLOR);
		Image outOfBoundsImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, OUT_OF_BOUNDS_COLOR);
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
					images[x][y] = convertToImageRoomCell(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, cell);
				}
				else if(outOfBounds.contains(cell))
				{
					images[x][y] = outOfBoundsImage;
				}
				else
				{
					images[x][y] = convertToImageNormalCell(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, cell);
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
		Graphics2D graphics = image.createGraphics();
		
		graphics.setColor(colour);
		graphics.fillRect(x, y, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT);
		
		return image;
	}
	
	private Image convertToImageNormalCell(int x, int y, int width, int height, Cell cell)
	{
		BasicStroke outline = new BasicStroke(1.0f);
		BufferedImage image = new BufferedImage(BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(NORMAL_CELL_COLOUR);
		graphics.fillRect(x, y, width, height);
		graphics.setColor(Color.BLACK);
		if(cell.hasWall(Direction.North))
		{
			graphics.setStroke(WALL_THICKNESS);
			graphics.drawLine(x, y, x+width, y);
		}
		else
		{
			graphics.setStroke(outline);
			graphics.drawLine(x, y, x+width, y);
		}
		
		if(cell.hasWall(Direction.South))
		{
			graphics.setStroke(WALL_THICKNESS);
			graphics.drawLine(x, y+height, x+width, y+height);
		}
		else
		{
			graphics.setStroke(outline);
			graphics.drawLine(x, y+height, x+width, y+height);
		}
		
		if(cell.hasWall(Direction.West))
		{
			graphics.setStroke(WALL_THICKNESS);
			graphics.drawLine(x, y, x, y+height);
		}
		else
		{
			graphics.setStroke(outline);
			graphics.drawLine(x, y, x, y+height);
		}
		
		if(cell.hasWall(Direction.East))
		{
			graphics.setStroke(WALL_THICKNESS);
			graphics.drawLine(x+width, y, x+width, y+height);
		}
		else
		{
			graphics.setStroke(outline);
			graphics.drawLine(x+width, y, x+width, y+height);
		}
		return image;
	}

	private Image convertToImageRoomCell(int x, int y, int width, int height, Cell cell)
	{
		BufferedImage image = new BufferedImage(BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		
		graphics.setColor(ROOM_COLOR);
		graphics.fillRect(x, y, width, height);
		graphics.setColor(Color.BLACK);
		graphics.setStroke(WALL_THICKNESS);
		
		if(cell.hasWall(Direction.North))
		{
			graphics.drawLine(x, y, x+width, y);
		}
		if(cell.hasWall(Direction.South))
		{
			graphics.drawLine(x, y+height, x+width, y+height);
		}
		if(cell.hasWall(Direction.West))
		{
			graphics.drawLine(x, y, x, y+height);
		}
		if(cell.hasWall(Direction.East))
		{
			graphics.drawLine(x+width, y, x+width, y+height);
		}
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
			Image image = initialisePieceImage(weaponName);
			PIECE_IMAGES.put(weaponName,image);
			pieces.put(image, model.getPosition(weapon));
		}
		for(Player player : Game.allPlayers)
		{
			String playerName = player.getName();
			Image image = initialisePieceImage(playerName);
			PIECE_IMAGES.put(playerName,image);
			pieces.put(image, model.getPosition(player));
		}
		return pieces;
	}
	
	private Image initialisePieceImage(String name)
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
	
	private Image getPieceImage(String name)
	{
		if(!PIECE_IMAGES.containsKey(name))
		{
			throw new IllegalArgumentException("There does not exist an image for the piece " + name);
		}
		return PIECE_IMAGES.get(name);
	}
	
	private Image getDiceImage(int number)
	{
		String fileName = "cluedo_images\\die" + number + ".png";
		try {
			return ImageIO.read(new File(fileName));
		} catch (IOException e) {
			throw new RuntimeException(fileName + " not found");
		}
	}
	
}