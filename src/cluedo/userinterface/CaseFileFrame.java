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
 * An "X" represents the cards that the player has crossed
 * out as part of the murder case.
 * A " " represents that the card may still be part of the murder
 */
public class CaseFileFrame extends JFrame{
	private static final int MIN_WIDTH = 600;
	private static final int MIN_HEIGHT = 600;
	private static final float BUTTON_FONT_SIZE = 20;
	private static final int VERTICAL_GAP = 30;

	//TODO remove these. Should pass these into the constructor from controller
	 String[] ROOM_NAMES = new String[] {
			 "Ballroom",
			 "Billiard Room", "Conservatory",
				"Dining Room", "Hall", "Kitchen", "Library", "Lounge", "Study" };
	 String[] SUSPECT_NAMES = new String[]{ "Miss Scarlett","Colonel Mustard",
			 "Mrs. White","Reverend Green","Mrs. Peacock","Professor Plum"};
	 String[] WEAPON_NAMES = new String[] { "Dagger", "Candlestick", "Revolver", "Rope",
				"Lead Pipe", "Spanner" };

	 String[] suspectHeader = new String[]{
			 "Suspects","\t"
	 };
	 String[] weaponHeader = new String[]{
			 "Weapons","\t"
	 };
	 String[] roomHeader = new String[]{
			 "Rooms","\t"
	 };

	public CaseFileFrame()
	{
		this.setTitle("Your Casefile");
		this.setMinimumSize(new Dimension(MIN_WIDTH,MIN_HEIGHT));
		
		String[][] roomRows = createRows(ROOM_NAMES);
		String[][] weaponRows = createRows(WEAPON_NAMES);
		String[][] suspectRows = createRows(SUSPECT_NAMES);

		JScrollPane suspects = createTable(suspectHeader,suspectRows);
		JScrollPane weapons = createTable(weaponHeader,weaponRows);
		JScrollPane rooms = createTable(roomHeader,roomRows);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JButton ok = new JButton("OK");
		GraphicalUserInterface.setFontSize(ok, BUTTON_FONT_SIZE);
		JLabel title = new JLabel("Your Casefile");
		GraphicalUserInterface.setFontSize(title, BUTTON_FONT_SIZE);
		//Align so that OK button is on the right
		title.setAlignmentX(LEFT_ALIGNMENT);
		suspects.setAlignmentX(LEFT_ALIGNMENT);
		weapons.setAlignmentX(LEFT_ALIGNMENT);
		rooms.setAlignmentX(LEFT_ALIGNMENT);
		ok.setAlignmentX(LEFT_ALIGNMENT);
		
		panel.add(title);
		panel.add(Box.createVerticalStrut(VERTICAL_GAP)); 
		panel.add(suspects);
		panel.add(weapons);
		panel.add(rooms);
		panel.add(Box.createVerticalStrut(VERTICAL_GAP));
		panel.add(ok);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 50, 20));
		this.add(panel);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		setResizable(false);
	}
	
	//TODO get controller to do
	/**
	 * Create the rows for this table to with made up
	 * boolean values
	 * @param rows
	 * @return
	 */
	private String[][] createRows(String[] rows)
	{
		String[][] tableRows = new String[rows.length][2];
		boolean alternate = false;
		for(int i = 0; i < rows.length; i++){
			tableRows[i][0] = rows[i];
			if(alternate)
			{
				tableRows[i][1] = "X";
			}
			else
			{
				tableRows[i][1] = " ";
			}
			alternate = !alternate;
		}
		return tableRows;
	}
	/**
	 * Create the tables using the provided columns headers 
	 * and the values in each row
	 * @param columnHeaders
	 * @param rowItems
	 * @return
	 */
	private JScrollPane createTable(String[] columnHeaders, String[][] rowItems)
	{
		TableModel model = new DefaultTableModel(rowItems, columnHeaders)
		  {
		    public boolean isCellEditable(int row, int column)
		    {
		      return false;//This causes all cells to be not editable
		    }
		  };
		JTable table = new JTable(model);
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(false); 
		int height = (int)(table.getRowHeight() * (table.getRowCount() + 1.5)) - 1;
		//So headers are attached to the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setEnabled(false);
		scrollPane.setPreferredSize(new Dimension(MIN_WIDTH,height));
		return scrollPane;
	}
	public static void main(String[] args)
	{
		new CaseFileFrame();
	}


}
