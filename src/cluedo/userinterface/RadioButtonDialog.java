package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cluedo.utility.ErrorChecking;

/**
 * Displays a number of Radio buttons as defined by the caller.
 */
public class RadioButtonDialog extends JDialog
{
	private JPanel panel;
	private JPanel buttonPanel;
	private Optional<Integer> selectedIndex;
	
	private static final String AFFIRMATIVE_BUTTON_LABEL = "Ok";
	
	// LAYOUT_ROWS are determined by the number of buttons.
	private static int LAYOUT_COLS = 1;
	private static final int LAYOUT_HORIZONTAL_GAP = 0;
	private static final int LAYOUT_VERTICAL_GAP = 30;

	private static final int BORDER_TOP = 20;
	private static final int BORDER_LEFT = 50;
	private static final int BORDER_BOTTOM = 20;
	private static final int BORDER_RIGHT = 50;
	
	private static final int BUTTON_FONT_SIZE = 26;
	
	private static final int WINDOW_MINIMUM_WIDTH = 600;
	private static final int WINDOW_MINIMUM_HEIGHT = 400;

	/**
	 * A dialog box that contains buttons, as defined by getUserSelection.
	 * @param owner The owner of this window.
	 * @param title The title of the window.
	 */
	public RadioButtonDialog(Frame owner, String title,String question)
	{
		super(owner, title, true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				selectedIndex = Optional.empty();
				cleanupDialog();
			}
		});
		
		panel = new JPanel();
		buttonPanel = new JPanel();
		JLabel message = new JLabel(question);
		GraphicalUserInterface.setFontSize(message, BUTTON_FONT_SIZE);
		message.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));
		this.add(message, BorderLayout.NORTH);
	}

	/**
	 * Create the window and get user selection for the list of buttons.
	 * @param buttons The radio buttons that are to be displayed.
	 * These buttons can be created by the static createRadioButtons.
	 * @return The integer selected, or Optional.empty() if close was selected.
	 */
	public Optional<Integer> getUserSelection(List<? extends JRadioButton> buttons)
	{
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.PAGE_END);

		this.setMinimumSize(new Dimension(WINDOW_MINIMUM_WIDTH, WINDOW_MINIMUM_HEIGHT));
		panel.setLayout(new GridLayout(buttons.size(), LAYOUT_COLS, LAYOUT_HORIZONTAL_GAP, LAYOUT_VERTICAL_GAP));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));
		
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));

		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < buttons.size(); i++)
		{
			AbstractButton thisButton = buttons.get(i);

			group.add(thisButton);
			panel.add(thisButton);
		}

		JButton affirmativeButton = new JButton(AFFIRMATIVE_BUTTON_LABEL);
		affirmativeButton.addActionListener((a) -> {
			selectedIndex =  getSelectedIndex(buttons);
			cleanupDialog();
		});
		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);

		buttonPanel.add(affirmativeButton);
		
		pack();
		this.setVisible(true);

		return selectedIndex;
	}

	/**
	 * Destroy the window, and set visible to false.
	 */
	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}

	/**
	 * Find the selected button in buttons.
	 * @param buttons The buttons from which to find the selected item.
	 * @return Optional.of(buttonIndex) starting from O, or Optional.empty() if there is no selected button.
	 */
	private Optional<Integer> getSelectedIndex(List<? extends JRadioButton> buttons)
	{
		ErrorChecking.ensureNonEmpty(buttons);

		for (int i = 0; i < buttons.size(); i++)
		{
			if (buttons.get(i).isSelected())
			{
				return Optional.of(i);
			}
		}

		return Optional.empty();
	}

	/**
	 * Create a list of radio buttons.
	 * Input arguments must be of the same length.
	 * The first available item starts off as being selected.
	 * @param options The strings to present after each button. Need not be unique.
	 * @param available The availability status of each option. True if can be selected, false otherwise.
	 * @return The newly created radio buttons. Can be used with getUserSelection.
	 */
	public static List<JRadioButton> createRadioButtons(List<String> options, List<Boolean> available)
	{
		assert options.size() == available.size() : "Lists are of differing sizes";

		final int originalSize = options.size();
		boolean haveDefaultSelection = false;

		List<JRadioButton> buttons = new ArrayList<JRadioButton>();

		for (int i = 0; i < options.size(); i++)
		{
			JRadioButton currentButton = new JRadioButton(options.get(i));
			GraphicalUserInterface.setFontSize(currentButton, BUTTON_FONT_SIZE);
			currentButton.setEnabled(available.get(i));

			// Make the first available option the default.
			if (!haveDefaultSelection && currentButton.isEnabled())
			{
				currentButton.setSelected(true);
				haveDefaultSelection = true;
			}

			buttons.add(currentButton);
		}

		assert buttons.size() == options.size() : "Didn't add all options";
		assert options.size() == originalSize : "Changed options size";
		assert options.size() == available.size() : "Changed one of the lists";
		return buttons;
	}
}
