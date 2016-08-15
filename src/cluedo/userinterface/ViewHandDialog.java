package cluedo.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ViewHandDialog extends JDialog
{
	private JPanel panel;
	private JPanel buttonPanel;
	
	private JLabel largeImage; // The image of the selected card in hand.
	private int currentImageIndex;
	
	private static final String AFFIRMATIVE_BUTTON_LABEL = "Ok";
	
	// LAYOUT_ROWS are determined by the number of buttons.
	private static final int LAYOUT_COLS = 1;
	private static final int LAYOUT_HORIZONTAL_GAP = 0;
	private static final int LAYOUT_VERTICAL_GAP = 30;

	private static final int BORDER_TOP = 20;
	private static final int BORDER_LEFT = 50;
	private static final int BORDER_BOTTOM = 20;
	private static final int BORDER_RIGHT = 50;
	
	private static final int BUTTON_FONT_SIZE = 26;
	
	private static final int WINDOW_MINIMUM_WIDTH = 600;
	private static final int WINDOW_MINIMUM_HEIGHT = 400;
	
	public ViewHandDialog(Frame owner, String title)
	{
		super(owner, title, true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanupDialog();
			}
		});
		
		panel = new JPanel();
		buttonPanel = new JPanel();
		
		largeImage = new JLabel();
	}
	
	/**
	 * Display the created window.
	 * @param buttons The radio buttons that are to be displayed.
	 * These buttons can be created by the static createRadioButtons.
	 * @return The integer selected, or Optional.empty() if close was selected.
	 */
	public void display(List<? extends JRadioButton> buttons, List<? extends ImageIcon> images)
	{
		this.add(panel, BorderLayout.CENTER);
		this.add(largeImage, BorderLayout.EAST);
		this.add(buttonPanel, BorderLayout.PAGE_END);

		this.setMinimumSize(new Dimension(WINDOW_MINIMUM_WIDTH, WINDOW_MINIMUM_HEIGHT));
		panel.setLayout(new GridLayout(buttons.size(), LAYOUT_COLS, LAYOUT_HORIZONTAL_GAP, LAYOUT_VERTICAL_GAP));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));
		
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT));

		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < buttons.size(); i++)
		{
			currentImageIndex = i;
			AbstractButton thisButton = buttons.get(i);

			group.add(thisButton);
			panel.add(thisButton);
			thisButton.addActionListener((a) -> {
				largeImage.setIcon(images.get(currentImageIndex));
			});
		}

		JButton affirmativeButton = new JButton(AFFIRMATIVE_BUTTON_LABEL);
		affirmativeButton.addActionListener((a) -> {
			cleanupDialog();
		});
		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);

		buttonPanel.add(affirmativeButton);
		
		pack();
		this.setVisible(true);
	}
	
	
	
	/**
	 * Destroy the window, and set visible to false.
	 */
	private void cleanupDialog()
	{
		this.dispose();
		this.setVisible(false);
	}
}
