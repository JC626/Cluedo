package cluedo.userinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
				Optional<Integer> optionalSelectedPlayerIndex = view.dialogRadioButtons("Select a player", "Which player would you like?", playerNames, availablePlayers);

				if (!optionalSelectedPlayerIndex.isPresent())
				{
					return;
				}
				
				int selectedPlayerIndex = optionalSelectedPlayerIndex.get();
				Player currentPlayer = Game.allPlayers.get(selectedPlayerIndex);
				
			
				
				activePlayers.add(currentPlayer);
			
				System.out.println(Arrays.toString(activePlayers.toArray()));
				
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
}