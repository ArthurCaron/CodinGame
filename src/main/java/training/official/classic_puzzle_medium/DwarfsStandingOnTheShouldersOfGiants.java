package training.official.classic_puzzle_medium;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// I had to rename Node to DwarfsNode because of conflits with BenderEpisode1's BenderNode class (also renamed)
class DwarfsStandingOnTheShouldersOfGiants {
	private static final int MAX_ID_VALUE_POSSIBLE = 10000;
	private static final DwarfsNode[] nodes = new DwarfsNode[MAX_ID_VALUE_POSSIBLE];

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		for (int i = in.nextInt(); i > 0; i--) {
			int idNode1 = in.nextInt();
			int idNode2 = in.nextInt();

			if (nodes[idNode1] == null) {
				nodes[idNode1] = new DwarfsNode();
			}
			if (nodes[idNode2] == null) {
				nodes[idNode2] = new DwarfsNode();
			}

			nodes[idNode1].destinationNodes.add(nodes[idNode2]);
			nodes[idNode2].sourceNodes.add(nodes[idNode1]);
		}

		List<DwarfsNode> startingNodes = DwarfsNode.findStartingNodes(nodes);

		int distanceToEndOfGraph = 0;

		for (DwarfsNode node : startingNodes) {
			distanceToEndOfGraph = Math.max(
					distanceToEndOfGraph,
					node.floodFill()
			);
		}

		System.out.println(distanceToEndOfGraph);
	}
}

class DwarfsNode {
	final List<DwarfsNode> destinationNodes = new ArrayList<>();
	final List<DwarfsNode> sourceNodes = new ArrayList<>();

	static List<DwarfsNode> findStartingNodes(DwarfsNode[] nodes) {
		List<DwarfsNode> startingNodes = new ArrayList<>();

		for (DwarfsNode node : nodes) {
			if (node != null) {
				if (node.sourceNodes.size() == 0) {
					startingNodes.add(node);
				}
			}
		}

		return startingNodes;
	}

	int floodFill() {
		int distanceToEndOfGraph = 1;

		for (DwarfsNode node : destinationNodes) {
			distanceToEndOfGraph = Math.max(
					distanceToEndOfGraph,
					node.floodFill() + 1
			);
		}

		return distanceToEndOfGraph;
	}
}
