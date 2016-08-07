package cluedo.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;

import cluedo.game.Game;
import cluedo.game.GameBuilder;
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
				String currentPlayerName = view.selectPlayer(playerNames, availablePlayers);
				
				Player currentPlayer = getPlayer(currentPlayerName);
				int currentPlayerIndex = Game.allPlayers.indexOf(currentPlayer);
				
				activePlayers.add(currentPlayer);
				
				availablePlayers.remove(currentPlayerIndex);
				availablePlayers.add(currentPlayerIndex, false);
			}
		});




		view.buttonQuitListener((a) -> { 
			if (view.yesNo("Are you sure?", "Do you want to quit?"))
			{
				System.exit(0);
			}
		});

	}

	private Player getPlayer(String name)
	{
		for(Player p : Game.allPlayers)
		{
			if(p.getName().equals(name))
			{
				return p;
			}
		}
		
		throw new IllegalArgumentException("Name must be one of the players");
	}
}