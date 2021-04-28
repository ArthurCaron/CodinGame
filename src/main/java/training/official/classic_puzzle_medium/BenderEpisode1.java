package training.official.classic_puzzle_medium;

import java.util.*;

//There's still a lot to do to improve the current code (make it more readable, more concise)
//Don't have time right now, will do later

// I had to rename Node to BenderNode because of conflits with DwarfsStandingOnTheShouldersOfGiants's BenderNode class (also renamed)
// Same thing from Graph class
class BenderEpisode1 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int height = in.nextInt();
		int width = in.nextInt();
		if (in.hasNextLine()) { in.nextLine(); }

		BenderGraph graph = new BenderGraph(height, width);
		String[] inputs;

		for (int i = 0; i < height; i++) {
			inputs = in.nextLine().split("");

			for (int j = 0; j < width; j++) {
				graph.nodes[i][j] = new BenderNode(inputs[j]);
			}
		}

		Bender bender = new Bender(graph);
		List<String> movements = bender.getMovements();

		for (String movement : movements) {
			System.out.println(movement);
		}
	}
}

class Bender {
	private List<String> movements = new ArrayList<>();

	private static final Map<String, String> directionPriority;
	private static final Map<String, String> invertedDirectionPriority;

	static {
		directionPriority = new HashMap<>();
		directionPriority.put("SOUTH", "EAST");
		directionPriority.put("EAST", "NORTH");
		directionPriority.put("NORTH", "WEST");
		directionPriority.put("WEST", "SOUTH");

		invertedDirectionPriority = new HashMap<>();
		invertedDirectionPriority.put("WEST", "NORTH");
		invertedDirectionPriority.put("NORTH", "EAST");
		invertedDirectionPriority.put("EAST", "SOUTH");
		invertedDirectionPriority.put("SOUTH", "WEST");
	}

	private boolean breakerMode = false;
	private boolean invertedMode = false;
	private Coordinates position;
	private String direction = "SOUTH";

	private boolean calculationOver = false;
	private boolean looping = false;

	private BenderGraph graph;

	Bender(BenderGraph graph) {
		this.graph = graph;
		position = graph.getStartingPoint();
		graph.removeSymbol(position);
	}

	List<String> getMovements() {
		while (!calculationOver) {
			direction = findCorrectDirection();
			Coordinates nextPosition = calculateNextPosition();
			goTo(nextPosition);
			computeSymbol();

			if (looping) {
				movements.clear();
				movements.add("LOOP");
				calculationOver = true;
			}
		}

		return movements;
	}

	private String findCorrectDirection() {
		Coordinates nextPosition = calculateNextPosition();
		String destinationSymbol = graph.nodes[nextPosition.x][nextPosition.y].symbol;

		if ((destinationSymbol.equals(Symbols.UNBREAKABLE_OBSTACLE)) || (destinationSymbol.equals(Symbols.BREAKABLE_OBSTACLE) && !breakerMode)) {
			direction = invertedMode ? "WEST" : "SOUTH";

			nextPosition = calculateNextPosition();
			destinationSymbol = graph.nodes[nextPosition.x][nextPosition.y].symbol;

			while ((destinationSymbol.equals(Symbols.UNBREAKABLE_OBSTACLE)) || (destinationSymbol.equals(Symbols.BREAKABLE_OBSTACLE)
					&& !breakerMode)) {
				direction = invertedMode ? invertedDirectionPriority.get(direction) : directionPriority.get(direction);

				nextPosition = calculateNextPosition();
				destinationSymbol = graph.nodes[nextPosition.x][nextPosition.y].symbol;
			}
		}

		return direction;
	}

	private Coordinates calculateNextPosition() {
		Coordinates nextPosition = position;

		switch (direction) {
			case "SOUTH":
				nextPosition = new Coordinates(position.x + 1, position.y);
				break;
			case "EAST":
				nextPosition = new Coordinates(position.x, position.y + 1);
				break;
			case "NORTH":
				nextPosition = new Coordinates(position.x - 1, position.y);
				break;
			case "WEST":
				nextPosition = new Coordinates(position.x, position.y - 1);
				break;
		}

		return nextPosition;
	}

	private void goTo(Coordinates coords) {
		position = coords;
		looping = graph.nodes[coords.x][coords.y].visiting(direction, breakerMode, invertedMode);
		movements.add(direction);
	}

	private void computeSymbol() {
		String destinationSymbol = graph.nodes[position.x][position.y].symbol;

		switch (destinationSymbol) {
			case Symbols.SOUTH:
				direction = "SOUTH";
				break;
			case Symbols.EAST:
				direction = "EAST";
				break;
			case Symbols.NORTH:
				direction = "NORTH";
				break;
			case Symbols.WEST:
				direction = "WEST";
				break;
			case Symbols.BEER:
				breakerMode = !breakerMode;
				break;
			case Symbols.INVERTER:
				invertedMode = !invertedMode;
				break;
			case Symbols.TELEPORTER:
				position = graph.getOtherTeleporter(position);
				break;
			case Symbols.BREAKABLE_OBSTACLE:
				if (breakerMode) {
					graph.removeSymbol(position);
				}
				break;
			case Symbols.END:
				calculationOver = true;
				break;
		}
	}
}

class BenderGraph {
	BenderNode[][] nodes;
	private int height;
	private int width;

	BenderGraph(int height, int width) {
		this.nodes = new BenderNode[height][width];
		this.height = height;
		this.width = width;
	}

	Coordinates getStartingPoint() {
		Coordinates coords = null;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (nodes[i][j].symbol.equals(Symbols.START)) {
					coords = new Coordinates(i, j);
				}
			}
		}

		return coords;
	}

	void removeSymbol(Coordinates coords) {
		nodes[coords.x][coords.y] = new BenderNode(Symbols.NOTHING);

		clearVisited();
	}

	private void clearVisited() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				nodes[i][j].clearVisited();
			}
		}
	}

	Coordinates getOtherTeleporter(Coordinates coords) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (nodes[i][j].symbol.equals(Symbols.TELEPORTER)) {
					if (i != coords.x || j != coords.y) {
						return new Coordinates(i, j);
					}
				}
			}
		}

		return coords;
	}
}

class BenderNode {
	private HashMap<String, Boolean> state = new HashMap<>();
	String symbol;

	BenderNode(String symbol) {
		this.symbol = symbol;
	}

	boolean visiting(String direction, boolean breakerMode, boolean invertedMode) {
		if (state.containsKey(direction) && state.get("breakerMode") == breakerMode && state.get("invertedMode") == invertedMode) {
			if (state.containsKey("mayBeLooping")) {
				return true;
			} else {
				state.put("mayBeLooping", true);
				return false;
			}
		} else {
			setVisited(direction, breakerMode, invertedMode);
			return false;
		}
	}

	private void setVisited(String direction, boolean breakerMode, boolean invertedMode) {
		state.put(direction, true);
		state.put("breakerMode", breakerMode);
		state.put("invertedMode", invertedMode);
	}

	void clearVisited() {
		state = new HashMap<>();
	}
}

// TODO: replace by Enum
class Symbols {
	static final String SOUTH = "S";
	static final String EAST = "E";
	static final String NORTH = "N";
	static final String WEST = "W";
	static final String UNBREAKABLE_OBSTACLE = "#";
	static final String BREAKABLE_OBSTACLE = "X";
	static final String START = "@";
	static final String END = "$";
	static final String BEER = "B";
	static final String INVERTER = "I";
	static final String TELEPORTER = "T";
	static final String NOTHING = " ";
}

class Coordinates {
	int x;
	int y;

	Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
