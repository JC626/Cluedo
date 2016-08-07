package cluedo.userinterface;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cluedo.utility.ErrorChecking;

public class GraphicalUserInterface extends JFrame
{
	JPanel mainMenu;

	JLabel title = new JLabel("Cluedo");


	JButton newGame = new JButton("New Game");
	JButton quit = new JButton("Quit");	

	public GraphicalUserInterface()
	{
		super("Cluedo"); // Window title

		setupMainMenu();

		addAllPanels();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(600, 600));
		
		pack();
		setResizable(false);
		this.setVisible(true);
	}

	private void addAllPanels()
	{
		this.add(mainMenu);
	}

	private void setupMainMenu()
	{
		createMainMenu();
		setLayoutMainMenu();
		setLayoutSizes();
		addToPanel(mainMenu, getMainMenuComponents());
	}
	
	private void createMainMenu()
	{
		mainMenu = new JPanel();
		mainMenu.add(title);
	}
	
	private void setLayoutMainMenu()
	{
		mainMenu.setLayout(new GridLayout(0,1,0,80));
		mainMenu.setBorder(BorderFactory.createEmptyBorder(20, 75, 50, 75));
	}

	private void setLayoutSizes()
	{
		setFontSize(title, 100);
		setFontSize(newGame, 25);
		setFontSize(quit, 25);
	}
	
	private Iterator<Component> getMainMenuComponents()
	{
		List<Component> list = new ArrayList<Component>();
		list.add(newGame);
		list.add(quit);
		return list.iterator();
	}

	private void addToPanel(JPanel panel, Iterator<Component> toAdd)
	{
		while (toAdd.hasNext())
		{
			panel.add(toAdd.next());
		}
	}


	public boolean yesNo(String title, String question)
	{
		return ConfirmationDialog.yesNo(title, question);
	}

	public boolean okCancel(String title, String question)
	{
		return ConfirmationDialog.okCancel(title, question);
	}




	public Optional<Integer> dialogRadioButtons(String windowTitle, String question, List<String> options, List<Boolean> available)
	{
		ErrorChecking.ensureNonEmpty(options, available);
		
		List<JRadioButton> buttons = ButtonDialog.createRadioButtons(options, available);
		
		Optional<Integer> selectedOption = new ButtonDialog(this, windowTitle).getUserSelection(buttons);
		
		if (selectedOption.isPresent())
		{
			assert selectedOption.get() >= 0 && selectedOption.get() < options.size() : "Selected option was not in options";
			assert available.get(selectedOption.get()) : "Selected an unavailable option";
		}
		 
		return selectedOption;
	}
	
	public Optional<String> dialogTextInput(String windowTitle, String question)
	{
		return new TextDialog(this, windowTitle).getUserInput(question);
	}
	
	
	

	public void buttonNewGameListener(ActionListener a)
	{
		newGame.addActionListener(a);
	}

	public void buttonQuitListener(ActionListener a)
	{
		quit.addActionListener(a);
	}
	
	/**
	 * A wrapper method for setFont, which changes the font size of a component.
	 * @param component The component that needs size changing.
	 * @param size The size to change it to.
	 */
	public static void setFontSize(Component component, float size)
	{
		component.setFont(component.getFont().deriveFont(size));
	}
}