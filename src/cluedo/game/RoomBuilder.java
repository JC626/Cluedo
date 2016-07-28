package cluedo.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cluedo.model.Cell;
import cluedo.model.Room;

//TODO better description RoomBuilder
/**
 * Creates the Rooms needed for the Game class
 * The cells for the room and the entrance and exit
 * cells are hardcoded in.
 *
 */
public class RoomBuilder 
{

	private final Cell[][] cells;

	private Map<Room, Set<Cell>> entranceCells = new HashMap<Room,Set<Cell>>();
	private Map<Room, Set<Cell>> roomCells = new HashMap<Room,Set<Cell>>();
	private Map<Room, List<Cell>> exitCells = new HashMap<Room,List<Cell>>();
	private Map<Cell, Room> cellToRoom = new HashMap<Cell,Room>();
	private List<Room> rooms = new ArrayList<Room>();

	private int[] KITCHEN = new int[] { 0, 1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 0, 2, 1, 2, 2, 2, 3, 2, 4, 2, 5, 2, 0, 3, 1,
			3, 2, 3, 3, 3, 4, 3, 5, 3, 0, 4, 1, 4, 2, 4, 3, 4, 4, 4, 5, 4, 0, 5, 1, 5, 2, 5, 3, 5, 4, 5, 5, 5, 1, 6, 2,
			6, 3, 6, 4, 6, 5, 6 };
	private int[] KITCHEN_ENTRANCE = new int[] { 4, 6, 5, 1 };

	private int[] KITCHEN_EXIT = new int[] { 4, 7, 21, 23 };

	private int[] BALLROOM = new int[] { 10, 2, 11, 2, 12, 2, 13, 2, 8, 3, 9, 3, 10, 3, 11, 3, 12, 3, 13, 3, 14, 3, 15,
			3, 8, 4, 9, 4, 10, 4, 11, 4, 12, 4, 13, 4, 14, 4, 15, 4, 8, 5, 9, 5, 10, 5, 11, 5, 12, 5, 13, 5, 14, 5, 15,
			5, 8, 6, 9, 6, 10, 6, 11, 6, 12, 6, 13, 6, 14, 6, 15, 6, 8, 7, 9, 7, 10, 7, 11, 7, 12, 7, 13, 7, 14, 7, 15,
			7 };

	private int[] BALLROOM_ENTRANCE = new int[] { 8, 5, 9, 7, 14, 7, 15, 5 };
	private int[] BALLROOM_EXIT = new int[] { 7, 5, 9, 8, 14, 8, 14, 5 };

	private int[] CONSERVATORY = new int[] { 18, 1, 19, 1, 20, 1, 21, 1, 22, 1, 23, 1, 18, 2, 19, 2, 20, 2, 21, 2, 22,
			2, 23, 2, 18, 3, 19, 3, 20, 3, 21, 3, 22, 3, 23, 3, 18, 4, 19, 4, 20, 4, 21, 4, 22, 4, 23, 4, 19, 5, 20, 5,
			21, 5, 22, 5 };

	private int[] CONSERVATORY_ENTRANCE = new int[] { 19, 5, 22, 5 };
	private int[] CONSERVATORY_EXIT = new int[] { 19, 6, 0, 19 };

	private int[] DINING_ROOM = new int[] { 0, 9, 1, 9, 2, 9, 3, 9, 4, 9, 0, 10, 1, 10, 2, 10, 3, 10, 4, 10, 5, 10, 6,
			10, 7, 10, 0, 11, 1, 11, 2, 11, 3, 11, 4, 11, 5, 11, 6, 11, 7, 11, 0, 12, 1, 12, 2, 12, 3, 12, 4, 12, 5, 12,
			6, 12, 7, 12, 0, 13, 1, 13, 2, 13, 3, 13, 4, 13, 5, 13, 6, 13, 7, 13, 0, 14, 1, 14, 2, 14, 3, 14, 4, 14, 5,
			14, 6, 14, 7, 14, 0, 15, 1, 15, 2, 15, 3, 15, 4, 15, 5, 15, 6, 15, 7, 15 };
	private int[] DINING_ROOM_ENTRANCE = new int[] { 6, 15, 7, 12 };
	private int[] DINING_ROOM_EXIT = new int[] { 6, 16, 8, 12 };

	private int[] BILLIARD_ROOM = new int[] { 18, 8, 19, 8, 20, 8, 21, 8, 22, 8, 23, 8, 18, 9, 19, 9, 20, 9, 21, 9, 22,
			9, 23, 9, 18, 10, 19, 10, 20, 10, 21, 10, 22, 10, 23, 10, 18, 11, 19, 11, 20, 11, 21, 11, 22, 11, 23, 11,
			18, 12, 19, 12, 20, 12, 21, 12, 22, 12, 23, 12 };

	private int[] BILLIARD_ROOM_ENTRANCE = new int[] { 18, 9, 22, 12 };
	private int[] BILLIARD_ROOM_EXIT = new int[] { 17, 9, 22, 13 };
	private int[] LIBRARY = new int[] { 18, 14, 19, 14, 20, 14, 21, 14, 22, 14, 17, 15, 18, 15, 19, 15, 20, 15, 21, 15,
			22, 15, 23, 15, 17, 16, 18, 16, 19, 16, 20, 16, 21, 16, 22, 16, 23, 16, 17, 17, 18, 17, 19, 17, 20, 17, 21,
			17, 22, 17, 23, 17, 18, 18, 19, 18, 20, 18, 21, 18, 22, 18 };

	private int[] LIBRARY_ENTRANCE = new int[] { 17, 16, 20, 14 };
	private int[] LIBRARY_EXIT = new int[] { 16, 16, 20, 13 };

	private int[] LOUNGE = new int[] { 0, 18, 1, 18, 2, 18, 3, 18, 4, 18, 5, 18, 6, 18, 0, 19, 1, 19, 2, 19, 3, 19, 4,
			19, 5, 19, 6, 19, 0, 20, 1, 20, 2, 20, 3, 20, 4, 20, 5, 20, 6, 20, 0, 21, 1, 21, 2, 21, 3, 21, 4, 21, 5, 21,
			6, 21, 0, 22, 1, 22, 2, 22, 3, 22, 4, 22, 5, 22, 6, 22, 0, 23, 1, 23, 2, 23, 3, 23, 4, 23, 5, 23, 6, 23, 0,
			24, 1, 24, 2, 24, 3, 24, 4, 24, 5, 24 };
	private int[] LOUNGE_ENTRANCE = new int[] { 6, 19, 0, 19 };
	private int[] LOUNGE_EXIT = new int[] { 6, 18, 22, 5 };

	private int[] HALL = new int[] { 9, 18, 10, 18, 11, 18, 12, 18, 13, 18, 14, 18, 9, 19, 10, 19, 11, 19, 12, 19, 13,
			19, 14, 19, 9, 20, 10, 20, 11, 20, 12, 20, 13, 20, 14, 20, 9, 21, 10, 21, 11, 21, 12, 21, 13, 21, 14, 21, 9,
			22, 10, 22, 11, 22, 12, 22, 13, 22, 14, 22, 9, 23, 10, 23, 11, 23, 12, 23, 13, 23, 14, 23, 9, 24, 10, 24,
			11, 24, 12, 24, 13, 24, 14, 24 };

	private int[] HALL_ENTRANCE = new int[] { 11, 18, 12, 18, 14, 20 };
	private int[] HALL_EXIT = new int[] { 11, 17, 12, 17, 15, 20 };

	private int[] STUDY = new int[] { 17, 21, 18, 21, 19, 21, 20, 21, 21, 21, 22, 21, 23, 21, 17, 22, 18, 22, 19, 22,
			20, 22, 21, 22, 22, 22, 23, 22, 17, 23, 18, 23, 19, 23, 20, 23, 21, 23, 22, 23, 23, 23, 18, 24, 19, 24, 20,
			24, 21, 24, 22, 24, 23, 24 };
	private int[] STUDY_ENTRANCE = new int[] { 17, 21, 23, 21 };
	private int[] STUDY_EXIT = new int[] { 17, 20, 5, 1 };
	
			
	public RoomBuilder(Cell[][] cells) 
	{
		this.cells = cells;
		createRooms();
		createCells(rooms.get(0),BALLROOM,BALLROOM_ENTRANCE,BALLROOM_EXIT);
		createCells(rooms.get(1),BILLIARD_ROOM,BILLIARD_ROOM_ENTRANCE,BILLIARD_ROOM_EXIT);
		createCells(rooms.get(2),CONSERVATORY,CONSERVATORY_ENTRANCE,CONSERVATORY_EXIT);
		createCells(rooms.get(3),DINING_ROOM,DINING_ROOM_ENTRANCE,DINING_ROOM_EXIT);
		createCells(rooms.get(4),HALL,HALL_ENTRANCE,HALL_EXIT);
		createCells(rooms.get(5),KITCHEN,KITCHEN_ENTRANCE,KITCHEN_EXIT);
		createCells(rooms.get(6),LIBRARY,LIBRARY_ENTRANCE,LIBRARY_EXIT);
		createCells(rooms.get(7),LOUNGE,LOUNGE_ENTRANCE,LOUNGE_EXIT);
		createCells(rooms.get(8),STUDY,STUDY_ENTRANCE,STUDY_EXIT);
	}
	private void createRooms()
	{
		for(int i = 0; i <Game.ROOM_NAMES.length;i++)
		{
			String roomName = Game.ROOM_NAMES[i];
			rooms.add(new Room(roomName));
		}
	}
	private void createCells(Room room, int[] roomArray, int[] entranceArray, int[] exitArray)
	{
		createRoomCells(room,roomArray);
		createEntranceRoomCells(room,entranceArray);
		createExitRoomCells(room,exitArray);
	}
	private void createRoomCells(Room room, int[] roomArray) 
	{
		Set<Cell> roomSetCells = new HashSet<Cell>();
		for(int i = 0; i < roomArray.length;i++)
		{
			int x = i;
			int y = i+1;
			Cell cell = cells[x][y];
			cellToRoom.put(cell,room);
			roomSetCells.add(cell);
		}
		roomCells.put(room, roomSetCells);
	}

	private void createEntranceRoomCells(Room room, int[] entranceArray) 
	{
		Set<Cell> entrances = new HashSet<Cell>();
		for(int i =0; i< entranceArray.length;i+=2)
		{
			int x = i;
			int y = i+1;
			Cell cell = cells[x][y];
			entrances.add(cell);
		}
		entranceCells.put(room, entrances);
	}

	private void createExitRoomCells(Room room, int[] exitArray) 
	{
		List<Cell> exits = new ArrayList<Cell>();
		for(int i =0; i< exitArray.length;i+=2)
		{
			int x = i;
			int y = i+1;
			Cell cell = cells[x][y];
			exits.add(cell);
		}
		exitCells.put(room, exits);
	}

	public Map<Room, Set<Cell>> getEntranceCells() 
	{
		return entranceCells;
	}

	public Map<Room, Set<Cell>> getRoomCells() 
	{
		return roomCells;
	}

	public Map<Room, List<Cell>> getExitCells() 
	{
		return exitCells;
	}

	public Map<Cell, Room> getCellToRoom() 
	{
		return cellToRoom;
	}
	public List<Room> getRooms() 
	{
		return rooms;
	}
}
