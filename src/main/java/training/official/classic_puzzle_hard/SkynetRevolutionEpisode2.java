package training.official.classic_puzzle_hard;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SkynetRevolutionEpisode2 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int numberOfNodes = in.nextInt(); // the total number of nodes in the level, including the gateways
		int numberOfLinks = in.nextInt(); // the number of links
		int numberOfExitNodes = in.nextInt(); // the number of exit gateways

		Skynet2Graph graph = new Skynet2Graph(numberOfNodes);

		for (int i = 0; i < numberOfLinks; i++) {
			int node1 = in.nextInt();
			int node2 = in.nextInt();
			graph.nodes[node1].linkedNodes.add(graph.nodes[node2]);
			graph.nodes[node2].linkedNodes.add(graph.nodes[node1]);
		}

		for (int i = 0; i < numberOfExitNodes; i++) {
			int exitNodeId = in.nextInt();
			graph.nodes[exitNodeId].isExitNode = true;
			graph.exitNodes.add(graph.nodes[exitNodeId]);
		}

		while (true) {
			Node agentNode = graph.nodes[in.nextInt()]; // The index of the node on which the Skynet agent is positioned this turn

			Link linkToCut = graph.findLinkToCut(agentNode);

			System.out.println(linkToCut.node1.id + " " + linkToCut.node2.id);

			graph.removeLink(linkToCut);
		}
	}
}

class Skynet2Graph {
	Node[] nodes;
	List<Node> exitNodes;

	Skynet2Graph(int numberOfNodes) {
		nodes = new Node[numberOfNodes];
		for (int i = 0; i < numberOfNodes; i++) {
			nodes[i] = new Node(i);
		}
		exitNodes = new ArrayList<>();
	}

	Link findLinkToCut(Node agentNode) {
		Link linkToCut = null;

		// We try to find if there is a direct link to an exit node
		linkToCut = findDirectLinkToAnExit(agentNode);

		// If there isn't, we find the closest link to a double exit node
		if (linkToCut == null) {
			linkToCut = findClosestLinkToDoubleExit(agentNode);
		}

		// If there isn't, we take any link to an exit node
		if (linkToCut == null) {
			linkToCut = getAnyLinkToAnExit();
		}

		return linkToCut;
	}

	private Link findDirectLinkToAnExit(Node agentNode) {
		Link linkToCut = null;

		for (Node node : agentNode.linkedNodes) {
			if (node.isExitNode) {
				linkToCut = new Link(agentNode, node);
			}
		}

		return linkToCut;
	}

	private Link findClosestLinkToDoubleExit(Node agentNode) {
		Link linkToCut = null;
		List<Node> nodesToDoubleExit = findNodesToDoubleExit();
		int shortestDistance = Integer.MAX_VALUE;

		floodFill(agentNode, 1);
		for (Node node : nodesToDoubleExit) {
			if (shortestDistance > node.distanceFromAgent) {
				shortestDistance = node.distanceFromAgent;
				linkToCut = new Link(node, node.getAnyLinkedExitNode());
			}
		}
		resetNodesFloodFillValues();

		return linkToCut;
	}

	private Link getAnyLinkToAnExit() {
		Link linkToCut = null;

		for (Node exitNode : exitNodes) {
			if (exitNode.linkedNodes.size() > 0) {
				linkToCut = new Link(exitNode, exitNode.linkedNodes.get(0));
			}
		}

		return linkToCut;
	}

	private List<Node> findNodesToDoubleExit() {
		List<Node> nodesToDoubleExit = new ArrayList<>();

		for (Node exitNode : exitNodes) {
			for (Node node : exitNode.linkedNodes) {
				if (node.countExitNodes() > 1) {
					nodesToDoubleExit.add(node);
				}
			}
		}

		return nodesToDoubleExit;
	}

	private void floodFill(Node source, int distanceFromAgent) {
		for (Node node : source.linkedNodes) {
			if (node.distanceFromAgent == -1 || node.distanceFromAgent > distanceFromAgent) {
				node.distanceFromAgent = distanceFromAgent;
				if (node.countExitNodes() > 0) {
					floodFill(node, distanceFromAgent);
				} else {
					floodFill(node, distanceFromAgent + 1);
				}
			}
		}
	}

	private void resetNodesFloodFillValues() {
		for (Node node : nodes) {
			node.distanceFromAgent = -1;
		}
	}

	void removeLink(Link link) {
		removeLink(link.node1, link.node2);
		removeLink(link.node2, link.node1);
	}

	private void removeLink(Node node1, Node node2) {
		node1.linkedNodes.removeIf(node -> node.id == node2.id);
	}
}

class Node {
	int id;
	boolean isExitNode;
	List<Node> linkedNodes = new ArrayList<>();
	int distanceFromAgent = -1;

	Node(int id) {
		this.id = id;
	}

	int countExitNodes() {
		int count = 0;

		for (Node linkedNode : linkedNodes) {
			if (linkedNode.isExitNode) {
				count++;
			}
		}

		return count;
	}

	Node getAnyLinkedExitNode() {
		Node node = null;

		for (Node linkedNode : linkedNodes) {
			if (linkedNode.isExitNode) {
				node = linkedNode;
			}
		}

		return node;
	}
}

class Link {
	Node node1;
	Node node2;

	Link(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}
}
