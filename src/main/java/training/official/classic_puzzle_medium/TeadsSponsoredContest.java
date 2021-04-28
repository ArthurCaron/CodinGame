package training.official.classic_puzzle_medium;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class TeadsSponsoredContest {
	private static final int MAX_ID_VALUE_POSSIBLE = 200000;
	private static final TeadsNode[] nodes = new TeadsNode[MAX_ID_VALUE_POSSIBLE];
	private static TeadsNode firstNode;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		for (int i = in.nextInt(); i > 0; i--) {
			int idNode1 = in.nextInt();
			int idNode2 = in.nextInt();

			if (nodes[idNode1] == null) {
				nodes[idNode1] = new TeadsNode();
			}
			if (nodes[idNode2] == null) {
				nodes[idNode2] = new TeadsNode();
			}

			nodes[idNode1].linkedNodes.add(nodes[idNode2]);
			nodes[idNode2].linkedNodes.add(nodes[idNode1]);

			if (firstNode == null) {
				firstNode = nodes[idNode1];
			}
		}

		firstNode.floodFill(firstNode);

		TeadsNode furthestNode = firstNode.findFurthestNode();

		int distanceFromEndOfGraph = furthestNode.floodFill(furthestNode);

		System.out.println((distanceFromEndOfGraph + 1) / 2);
	}
}

class TeadsNode {
	final List<TeadsNode> linkedNodes = new ArrayList<>();
	private int distanceFromEndOfGraph;
	private TeadsNode furthestNode;

	TeadsNode findFurthestNode() {
		furthestNode = this;

		for (TeadsNode node : linkedNodes) {
			if (node.distanceFromEndOfGraph == (distanceFromEndOfGraph - 1)) {
				if (node.distanceFromEndOfGraph == 0) {
					furthestNode = node;
				} else {
					node.findFurthestNode();
					furthestNode = node.furthestNode;
				}
			}
		}

		return furthestNode;
	}

	int floodFill(TeadsNode caller) {
		distanceFromEndOfGraph = 0;

		for (TeadsNode node : linkedNodes) {
			if (node != caller) {
				distanceFromEndOfGraph = Math.max(
						distanceFromEndOfGraph,
						node.floodFill(this) + 1
				);
			}
		}

		return distanceFromEndOfGraph;
	}
}
