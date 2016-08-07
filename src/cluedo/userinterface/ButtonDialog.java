package cluedo.userinterface;

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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import cluedo.utility.ErrorChecking;

public class ButtonDialog extends JDialog
{
	private JPanel panel;
	private Optional<Integer> selectedIndex;

	public ButtonDialog(Frame owner, String title)
	{
		super(owner, title, true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				selectedIndex = Optional.empty();
				cleanupDialog();
			}
		});

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		panel = new JPanel();
	}

	public Optional<Integer> getUserSelection(List<? extends AbstractButton> buttons)
	{
		this.getContentPane().add(panel);

		this.setMinimumSize(new Dimension(600,600));
		panel.setLayout(new GridLayout(buttons.size(), 1));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 60, 75));


		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < buttons.size(); i++)
		{
			AbstractButton thisButton = buttons.get(i);

			group.add(thisButton);
			panel.add(thisButton, i);
		}

		JButton myButton = new JButton("Ok");
		myButton.addActionListener((a) -> {
			selectedIndex =  getSelectedIndex(buttons);
			cleanupDialog();
		});
		GraphicalUserInterface.setFontSize(myButton, 25);

		panel.add(myButton);
		pack();
		this.setVisible(true);

		return selectedIndex;
	}

	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}

	private Optional<Integer> getSelectedIndex(List<? extends AbstractButton> buttons)
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

	public static List<JRadioButton> createRadioButtons(List<String> options, List<Boolean> available)
	{
		assert options.size() == available.size() : "Lists are of differing sizes";

		final int originalSize = options.size();
		boolean haveDefaultSelection = false;

		List<JRadioButton> buttons = new ArrayList<JRadioButton>();

		for (int i = 0; i < options.size(); i++)
		{
			JRadioButton currentButton = new JRadioButton(options.get(i));
			GraphicalUserInterface.setFontSize(currentButton, 25);
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
