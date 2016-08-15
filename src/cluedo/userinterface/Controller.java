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
import javax.swing.ImageIcon;

import cluedo.board.Board;
import cluedo.exceptions.IllegalMethodCallException;
import cluedo.exceptions.InvalidMoveException;
import cluedo.exceptions.NoAvailableExitException;
import cluedo.game.Game;
import cluedo.game.GameBuilder;
import cluedo.model.Cell;
import cluedo.model.Player;
import cluedo.model.Weapon;
import cluedo.model.cards.Card;
import cluedo.model.cards.RoomCard;
import cluedo.model.cards.SuspectCard;
import cluedo.model.cards.WeaponCard;
import cluedo.utility.Heading.Direction;

public class Controller
{
	private Game model;
	private GraphicalUserInterface view;
	
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
						view.setVisible(false);
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
			if(name.isPresent() && name.get().length() <= 0)
			{
				view.error("Invalid name", "A name must be at least one character");
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
			playerNames.add(name.get() + " (" + currentPlayer.get().getName() + ")");

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
		view.newBoard(getImages(),initialisePieces());
		//Setup initial player
		newTurn();
		//Add listeners here
		//TODO add button listeners here

		view.addNewGameListener(newGameListener);
		view.addQuitListener(quitListener);
		view.addEndTurnListener(endTurnListener());
		view.addCasefileListener(casefileListener());
		view.addAccusationListener(accusationListener());
		view.addSuggestionListener(suggestionListener());
		view.addHandListener(handListener());
		
		view.addBoardKeyListener(keyListener());
		//Add mouselistener to board pane so extra height from the menu bar doesn't affect clicking position
		view.addBoardMouseListener(mouseListener());
	}
	/**
	 * Create a action listener for ending the turn
	 * and switching to the next player in the game
	 * @return A ActionListener
	 */
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
				newTurn();
			}
		};
		return listener;
	}
	
	/**
	 * Create a action listener for viewing a casefile
	 * @return A ActionListener
	 */
	private ActionListener casefileListener()
	{
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Map<String,Boolean> suspects = new HashMap<String,Boolean>(); 
				Map<String,Boolean> weapons = new HashMap<String,Boolean>(); 
				Map<String,Boolean> rooms = new HashMap<String,Boolean>(); 
				for (Card suspectCard : model.getSuspectCards())
				{
					boolean outOfSuspicion  = !model.getPlayerSuspectCards().contains(suspectCard);
					suspects.put(suspectCard.getName(), outOfSuspicion);
				}

				for (Card weaponCard : model.getWeaponCards())
				{
					boolean outOfSuspicion  = !model.getPlayerWeaponCards().contains(weaponCard);
					weapons.put(weaponCard.getName(), outOfSuspicion);
				}
				
				for (Card roomCard : model.getRoomCards())
				{
					boolean outOfSuspicion  = !model.getPlayerRoomCards().contains(roomCard);
					rooms.put(roomCard.getName(), outOfSuspicion);
					
				}
				
				String[][] suspectRows = createRows(suspects);
				String[][] weaponRows = createRows(weapons);
				String[][] roomRows = createRows(rooms);
				CaseFileFrame casefile = new CaseFileFrame(suspectRows, weaponRows, roomRows);
			}
		};
		return listener;
	}
	
	/**
	 * Create the rows for a table
	 * @param rows
	 * @return The rows as a String[][] for creating the table
	 */
	private String[][] createRows(Map<String,Boolean> rows)
	{
		String[][] tableRows = new String[rows.size()][2];
		int i = 0;
		for(Map.Entry<String, Boolean> row : rows.entrySet())
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
				//TODO don't do anything if not corresponding key?
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
					view.error("Cannot Move", "You cannot move as all paths are blocked.");
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
						view.changePieceLocation(getPieceImage(playerName), cell);
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
								view.changePieceLocation(getPieceImage(playerName), toExit);
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
	private ActionListener handListener()
	{
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<String> options = allPlayerNames();
				List<Boolean> available = new ArrayList<Boolean>();
				
				fillBoolean(available, available.size(), true);
			
				view.dialogViewHand(model.getCurrentPlayer().getName() + "'s hand", RadioButtonDialog.createRadioButtons(options, available), getCurrentPlayerHandImages());
			}
			
			private List<ImageIcon> getCurrentPlayerHandImages()
			{
				List<ImageIcon> images = new ArrayList<ImageIcon>();
				
				images.addAll(getHandImages(stringListFromRoom(model.getPlayerRoomCards())));
				images.addAll(getHandImages(stringListFromSuspect(model.getPlayerSuspectCards())));
				images.addAll(getHandImages(stringListFromWeapon(model.getPlayerWeaponCards())));
				
				return images;
			}
			
			private List<ImageIcon> getHandImages(List<String> names)
			{
				List<ImageIcon> images = new ArrayList<ImageIcon>();
				
				for (String name : names)
				{
					images.add(new ImageIcon(getImage(name)));
				}
				
				return images;
			}
		};
		return listener;
	}
	
	private ActionListener suggestionListener()
	{
		//TODO suggestions
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!model.canMakeSuggestion())
				{
					if(!model.isInRoom())
					{
						view.error("Cannot make a suggestion", "You cannot make a suggestion as you are not in a room");
					}
					else
					{
						view.error("Cannot make a suggestion", "You cannot make a suggestion as you have already made a suggestion this turn");
					}
				}
				else
				{
					//Get cards for suggesting
					Optional<Card> suspectOption = chooseCard(model.getSuspectCards(), "suspect","I suggest the crime was committed in the " + model.getCurrentRoom().getName() + " by ... ");
					if(!suspectOption.isPresent())
					{
						return;
					}
					Optional<Card> weaponOption = chooseCard(model.getWeaponCards(), "weapon", "with the ...");
					if(!weaponOption.isPresent())
					{
						return;
					}
					SuspectCard murderer = (SuspectCard) suspectOption.get();
					WeaponCard murderWeapon = (WeaponCard) weaponOption.get();
					String verificationQuestion = String.format("You're suggesting %s committed the crime in the %s with the %s?", murderer.getName(), model.getCurrentRoom().getName(), murderWeapon.getName());
					boolean confirm = view.yesNo("Are you sure?", verificationQuestion);
					if(!confirm)
					{
						return;
					}
					Map<Player, Set<Card>> disproved = model.makeSuggestion(murderWeapon, murderer);
					updateBoard();
					if (!disproved.isEmpty())
					{
						Map<Player, Card> disprover = new HashMap<Player, Card>();
						// Given that disproved is not empty, these two variables will be initialised.
						Player disprovingPlayer = null;
						Set<Card> disprovingHandSet = null;

						List<Card> disprovingHandList = new ArrayList<Card>();

						for (Player p : disproved.keySet())
						{
							disprovingPlayer = p;
							disprovingHandSet = disproved.get(p);
							break;
						}

						assert disprovingHandSet != null;
						assert disprovingPlayer != null;

						for (Card c : disprovingHandSet)
						{
							disprovingHandList.add(c);
						}
						String disproverName = model.getHumanName(disprovingPlayer);
						String currentPlayerName = model.getHumanName(model.getCurrentPlayer());

						view.information(disproverName,String.format("%s, you can disprove the suggestion...", disproverName));
						String question = String.format("%s choose a card to reveal to %s:", disproverName, currentPlayerName);
						//radio buttons
						Optional<Card> disproveCard = Optional.empty();
						while(!disproveCard.isPresent())
						{
							 disproveCard = chooseCard(disprovingHandList, "card", question);
						}
						disprover.put(disprovingPlayer, disproveCard.get());
						model.removeCard(disprover);
						view.information("Suggestion disproved", disproverName + " has shown you the card, " + disproveCard.get().getName());
					}
					else
					{
						view.information("No disprovers","No one could disprove your suggestion... Maybe you're onto something here.");
					}
				}
			}
		};
		return listener;
	}
	private ActionListener accusationListener()
	{
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Get accusing player
				List<Player> activePlayers = model.getActivePlayers();
				List<Boolean> available = new ArrayList<Boolean>(activePlayers.size());
				List<String> playerNames = new ArrayList<String>(activePlayers.size());
				for(Player player :activePlayers)
				{
					available.add(true);
					playerNames.add(model.getHumanName(player));
				}
				Optional<Integer> selectedPlayer = view.dialogRadioButtons("Accusation", "Who is making the accusation?", playerNames, available);
				if(!selectedPlayer.isPresent())
				{
					return;
				}	
				Player accusingPlayer = activePlayers.get(selectedPlayer.get());
				//Get cards for accusing
				Optional<Card> suspectOption = chooseCard(model.getSuspectCards(), "suspect","I accuse ...");
				if(!suspectOption.isPresent())
				{
					return;
				}
				Optional<Card> roomOption = chooseCard(model.getRoomCards(), "room", "of committing the crime in the ...");
				if(!roomOption.isPresent())
				{
					return;
				}
				Optional<Card> weaponOption = chooseCard(model.getWeaponCards(), "weapon", "with the ...");
				if(!weaponOption.isPresent())
				{
					return;
				}
				
				SuspectCard murderer = (SuspectCard) suspectOption.get();
				WeaponCard murderWeapon = (WeaponCard) weaponOption.get();
				RoomCard murderRoom = (RoomCard) roomOption.get();
				String confirmationMessage = String.format("Are you sure you want to accuse %s of killing John Boddy in the %s with the %s?", murderer.getName(), murderRoom.getName(), murderWeapon.getName());
				boolean confirm = view.yesNo("Are you sure?", confirmationMessage);
				if(!confirm)
				{
					return;
				}
				String playerName = model.getHumanName(accusingPlayer);
				Player currentPlayer = model.getCurrentPlayer();
				boolean won = model.makeAccusation(accusingPlayer, murderWeapon, murderRoom, murderer);
				updateBoard();
				if(won)
				{
					view.information(playerName + " you win!", "Congratulations on finding the murderer, " + accusingPlayer.getName() + "!");
				}
				else
				{
					view.error("Game Over " + playerName, playerName + ", you've made a very serious accusation and we have evidence to the contrary. You will no longer be able to participate in this investigation.");
					if(accusingPlayer == currentPlayer)
					{
						System.out.println("Hello");
						newTurn();
					}
				}
				//Go back to main menu
				if(model.isGameOver())
				{
					if(!won)
					{
						List<Card> answer = model.getAnswer();
						String answerText = String.format("All players have been eliminated. Answer: %s killed John Boddy in the %s with the %s",answer.get(0).getName(),answer.get(1).getName(),answer.get(2).getName());
						view.information("No winners",answerText);
					}

					view.destoryBoard();
					view.setVisible(true);
				}
			}
		};
		return listener;
	}
	
	/**
	 * Sets up the new turn for the current player
	 */
	private void newTurn()
	{
		Player player = model.getCurrentPlayer();
		String playerName = model.getHumanName(player);
		int[] diceRoll = model.getDiceRoll();
		Image leftDie = getImage("die" + diceRoll[0]);
		Image rightDie = getImage("die" + diceRoll[1]);
		view.changeDice(leftDie, rightDie);
		view.information(playerName + "'s turn", playerName + " it is your turn");
		//Show exits if the player is in the room
		if(model.isInRoom())
		{
			Image exitImage = convertToImage(0, 0, BoardCanvas.CELL_WIDTH, BoardCanvas.CELL_HEIGHT, EXIT_COLOR);
			try 
			{
				List<Cell> exitCells = model.getAvailableExits();
				view.drawExitCells(exitCells, exitImage);
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
	
	private Optional<Card> chooseCard(List<Card> cards, String type,String message)
	{
		List<Boolean> available = new ArrayList<Boolean>(cards.size());
		List<String> cardName = new ArrayList<String>(cards.size());
		for(Card card : cards)
		{
			cardName.add(card.getName());
			available.add(true);
		}
		Optional<Integer> option = view.dialogRadioButtons("Choose a " + type, message, cardName, available);
		if(!option.isPresent())
		{
			return Optional.empty();
		}
		Optional<Card> card = Optional.of(cards.get(option.get()));
		return card;
	}
	
	/**
	 * Update the position of all pieces (players and weapons)
	 * on the BoardCanvas.
	 */
	private void updateBoard()
	{
		for(Player player : Game.allPlayers)
		{
			Image piece = getPieceImage(player.getName());
			Cell pos = model.getPosition(player);
			view.changePieceLocation(piece, pos);
		}
		for(Weapon weapon : model.getWeapons())
		{
			Image piece = getPieceImage(weapon.getName());
			Cell pos = model.getPosition(weapon);
			view.changePieceLocation(piece, pos);
		}		
	}
	
	/**
	 * Creates the board as it is to be drawn, based on Cells and their properties.
	 * @return The array of Images that represent each Cell.
	 */
	private Image[][] getImages()
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
		// We need to modify PIECE_IMAGES here because otherwise the references
		// for the images in the map differ.
		// We can't pass in the resulting map from this method to another because
		// we lose the name associated with each image.
		assert model != null;
		Map<Image, Cell> pieces = new HashMap<Image,Cell>(); 
		List<Weapon> weapons = model.getWeapons();
		
		for(Weapon weapon : weapons)
		{
			String weaponName = weapon.getName();
			Image image = getImage(weaponName);
			
			PIECE_IMAGES.put(weaponName, image);
			pieces.put(image, model.getPosition(weapon));
		}
		
		for(Player player : Game.allPlayers)
		{
			String playerName = player.getName();
			Image image = getImage(playerName);
			
			PIECE_IMAGES.put(playerName, image);
			pieces.put(image, model.getPosition(player));
		}
		return pieces;
	}
	
	private Image getPieceImage(String name)
	{
		if(!PIECE_IMAGES.containsKey(name))
		{
			throw new IllegalArgumentException("There does not exist an image for the piece " + name);
		}
		return PIECE_IMAGES.get(name);
	}
	
	private Image getImage(String name)
	{
		String fileName = "cluedo_images/" + name + ".png";
		try
		{
			return ImageIO.read(new File(fileName));
		} catch (IOException e)
		{
			throw new RuntimeException(fileName + " not found");
		}
	}
	
	private List<String> stringListFromRoom(List<RoomCard> list)
	{
		List<String> names = new ArrayList<String>();
		for (RoomCard r : list)
		{
			names.add(r.getName());
		}
		return names;
	}
	
	private List<String> stringListFromSuspect(List<SuspectCard> list)
	{
		List<String> names = new ArrayList<String>();
		for (SuspectCard s : list)
		{
			names.add(s.getName());
		}
		return names;
	}
	
	private List<String> stringListFromWeapon(List<WeaponCard> list)
	{
		List<String> names = new ArrayList<String>();
		for (WeaponCard w : list)
		{
			names.add(w.getName());
		}
		return names;
	}
	
	private List<String> stringListFromStringArray(String[] array)
	{
		List<String> allStrings = new ArrayList<String>();
		for (String w : array)
		{
			allStrings.add(w);
		}
		return allStrings;
	}
	
	private List<String> allPlayerNames()
	{
		return stringListFromStringArray(GameBuilder.SUSPECT_NAMES);
	}
}