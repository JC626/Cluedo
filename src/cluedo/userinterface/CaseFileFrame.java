package cluedo.userinterface;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 * A window that displays the current player's casefile.
 * This window consists of three tables which are the
 * suspects, weapons and rooms in the casefile
 * An "X" represents the cards that the player has removed from
 * suspicion.
 * A " " represents that the card may still be part of the murder
 */
public class CaseFileFrame extends JFrame
{
	private static final int MIN_WIDTH = 600;
	private static final int MIN_HEIGHT = 600;
	private static final float BUTTON_FONT_SIZE = 20;
	private static final int VERTICAL_GAP = 30;

	//The column header name for each table
	private String[] suspectHeader = {"Suspects", "\t"};
	private String[] weaponHeader = {"Weapons", "\t"};
	private String[] roomHeader = {"Rooms", "\t"};

	/**
	 * Create the tables using the provided columns headers
	 * Each parameter represents a single table to be created.
	 * @param suspectRows
	 * @param weaponRows
	 * @param roomRows
	 */
	public CaseFileFrame(String[][] suspectRows, String[][] weaponRows, String[][] roomRows) 
	{
		this.setTitle("Your Casefile");
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

		JPanel panel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JLabel title = new JLabel("Your Casefile");
		
		JScrollPane suspects = createTable(suspectHeader,suspectRows);
		JScrollPane weapons = createTable(weaponHeader,weaponRows);
		JScrollPane rooms = createTable(roomHeader,roomRows);
		
		JButton affirmativeButton = new JButton("OK");
		affirmativeButton.addActionListener(a -> {
			this.dispose();
		});

		GraphicalUserInterface.setFontSize(affirmativeButton, BUTTON_FONT_SIZE);
		GraphicalUserInterface.setFontSize(title, BUTTON_FONT_SIZE);

		//Align everything to the left
		title.setAlignmentX(LEFT_ALIGNMENT);
		suspects.setAlignmentX(LEFT_ALIGNMENT);
		weapons.setAlignmentX(LEFT_ALIGNMENT);
		rooms.setAlignmentX(LEFT_ALIGNMENT);
		
		// Line up our button appropriately
		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
		affirmativeButton.setAlignmentX(CENTER_ALIGNMENT);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(title);
		panel.add(Box.createVerticalStrut(VERTICAL_GAP)); 
		panel.add(suspects);
		panel.add(weapons);
		panel.add(rooms);
		panel.add(Box.createVerticalStrut(VERTICAL_GAP));
		
		panel.add(buttonPanel);
		buttonPanel.add(affirmativeButton);
		
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 50, 20));
		this.add(panel);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		setResizable(false);
	}

	/**
	 * Create the tables using the provided columns headers 
	 * and the values in each row
	 * @param columnHeaders The string values in each header.
	 * @param rowItems The items in each row.
	 * @return An table that cannot be changed by the user. 
	 */
	private JScrollPane createTable(String[] columnHeaders, String[][] rowItems)
	{
		TableModel model = new DefaultTableModel(rowItems, columnHeaders)
		{
			public boolean isCellEditable(int row, int column)
			{
				return false; // This causes all cells to be not be editable
			}
		};
		
		JTable table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		//Height of table sized to fit all the rows at the minimum height required to see the row
		int height = (int)(table.getRowHeight() * (table.getRowCount() + 1.5)) - 1;
		
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(false); 
		
		// Ensure headers are attached to the table
		scrollPane.setEnabled(false);
		scrollPane.setPreferredSize(new Dimension(MIN_WIDTH, height));
		return scrollPane;
	}

}
