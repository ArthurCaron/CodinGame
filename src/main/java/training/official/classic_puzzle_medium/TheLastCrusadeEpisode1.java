package training.official.classic_puzzle_medium;

import java.util.HashMap;
import java.util.Scanner;

class TheLastCrusadeEpisode1 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int width = in.nextInt(); // number of columns.
		int height = in.nextInt(); // number of rows.
		if (in.hasNextLine()) { in.nextLine(); }

		Room[][] rooms = new Room[width][height];
		for (int h = 0; h < height; h++) {
			String[] floorRooms = in.nextLine().split(" "); // represents a line in the grid and contains W integers. Each integer represents one room of a given type.

			for (int w = 0; w < floorRooms.length; w++) {
				rooms[w][h] = Room.getRoom(Integer.parseInt(floorRooms[w]));
			}
		}
		int EX = in.nextInt(); // the coordinate along the X axis of the exit (not useful for this first mission, but must be read).

		while (true) {
			int XI = in.nextInt();
			int YI = in.nextInt();
			String origin = in.next();

			if (rooms[XI][YI].isDestinationLeft(origin)) {
				System.out.println((XI - 1) + " " + YI);
			}
			else if (rooms[XI][YI].isDestinationRight(origin)) {
				System.out.println((XI + 1) + " " + YI);
			}
			else if (rooms[XI][YI].isDestinationBottom(origin)) {
				System.out.println(XI + " " + (YI + 1));
			}
		}
	}
}

class Room {
	private static final String IMPOSSIBLE = "IMPOSSIBLE";
	private static final String TOP = "TOP";
	private static final String LEFT = "LEFT";
	private static final String RIGHT = "RIGHT";
	private static final String BOTTOM = "BOTTOM";

	private HashMap<String, String> destinations = new HashMap<>();

	private Room(String topDestination, String leftDestination, String rightDestination) {
		destinations.put(TOP, topDestination);
		destinations.put(LEFT, leftDestination);
		destinations.put(RIGHT, rightDestination);
	}

	boolean isDestinationLeft(String origin) {
		return destinations.get(origin).equals(LEFT);
	}
	boolean isDestinationRight(String origin) {
		return destinations.get(origin).equals(RIGHT);
	}
	boolean isDestinationBottom(String origin) {
		return destinations.get(origin).equals(BOTTOM);
	}

	static Room getRoom(int type) {
		Room room = null;
		switch (type) {
			case 0:
				room = new Room(IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE);
				break;
			case 1:
				room = new Room(BOTTOM, BOTTOM, BOTTOM);
				break;
			case 2:
				room = new Room(IMPOSSIBLE, RIGHT, LEFT);
				break;
			case 3:
				room = new Room(BOTTOM, IMPOSSIBLE, IMPOSSIBLE);
				break;
			case 4:
				room = new Room(LEFT, IMPOSSIBLE, BOTTOM);
				break;
			case 5:
				room = new Room(RIGHT, BOTTOM, IMPOSSIBLE);
				break;
			case 6:
				room = new Room(IMPOSSIBLE, RIGHT, LEFT);
				break;
			case 7:
				room = new Room(BOTTOM, IMPOSSIBLE, BOTTOM);
				break;
			case 8:
				room = new Room(IMPOSSIBLE, BOTTOM, BOTTOM);
				break;
			case 9:
				room = new Room(BOTTOM, BOTTOM, IMPOSSIBLE);
				break;
			case 10:
				room = new Room(LEFT, IMPOSSIBLE, IMPOSSIBLE);
				break;
			case 11:
				room = new Room(RIGHT, IMPOSSIBLE, IMPOSSIBLE);
				break;
			case 12:
				room = new Room(IMPOSSIBLE, IMPOSSIBLE, BOTTOM);
				break;
			case 13:
				room = new Room(IMPOSSIBLE, BOTTOM, IMPOSSIBLE);
				break;
		}
		return room;
	}
}
