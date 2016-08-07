package cluedo.userinterface;

import java.awt.Color;
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
	/*public Color[][] getColouredCells()
	{
		assert model != null : "Cannot get cells for an empty game";
		Cell[][] cells = model.getCells();
		Color[][] coloured = new Color[Board.WIDTH][Board.HEIGHT];
		for(int x = 0; x < Board.WIDTH; x++)
		{
			for(int y = 0; y < Board.HEIGHT; y++)
			{
				Cell c= cells[x][y];
				try
				{
					model.getRoom(c);
					coloured[x][y] = Color.YELLOW;
				}
				catch(IllegalArgumentException e)
				{
					coloured[x][y] = Color.LIGHT_GRAY;
				}
			}
		}
		return coloured;
	}*/
}