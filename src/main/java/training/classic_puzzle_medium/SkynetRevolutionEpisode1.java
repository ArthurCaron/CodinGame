package training.classic_puzzle_medium;

import java.util.*;

class SkynetRevolutionEpisode1 {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int numberOfNodes = in.nextInt(); // the total number of nodes in the level, including the gateways
		int numberOfLinks = in.nextInt(); // the number of links
		int numberOfExits = in.nextInt(); // the number of exit gateways

		Skynet1Grid grid = new Skynet1Grid(numberOfNodes);

		for (int i = 0; i < numberOfLinks; i++) {
			int node1 = in.nextInt();
			int node2 = in.nextInt();
			grid.nodes[node1].destinations.add(grid.nodes[node2]);
			grid.nodes[node2].destinations.add(grid.nodes[node1]);
		}

		for (int i = 0; i < numberOfExits; i++) {
			grid.nodes[in.nextInt()].isExitNode = true;
		}

		while (true) {
			Skynet1Node agentNode = grid.nodes[in.nextInt()]; // The index of the node on which the Skynet agent is positioned this turn
			Link output = null;

			// We try to find if there is a link to an exit
			output = grid.getDirectLinkToAnExit(agentNode);

			// If not, we try to find if there is a link we can cut to block the agent
			if (output == null) {
				output = grid.getLinkToCutToBlockAgent(agentNode);
			}

			// If not, we try to find any link that links two nodes that are recursively linked to nodes which also have an exit and two nodes that are recursively...
			if (output == null) {
				output = grid.getLinkBetweenTwoRecursivelyValidNodes();
			}

			// If not, we try to find any link to an exit
			if (output == null) {
				output = grid.getDefaultLinkToAnExit();
			}

			System.out.println(output.node1.id + " " + output.node2.id);
			grid.removeLinks(output);
		}
	}
}

class Skynet1Grid {
	Skynet1Node[] nodes;
	Link linkBetweenTwoRecursivelyValidNodes;

	public Skynet1Grid(int numberOfNodes) {
		nodes = new Skynet1Node[numberOfNodes];
		for (int i = 0; i < numberOfNodes; i++) {
			nodes[i] = new Skynet1Node(i);
		}
	}

	public Link getDirectLinkToAnExit(Skynet1Node agentNode) {
		Link output = null;
		for (Skynet1Node node : agentNode.destinations) {
			if (node.isExitNode) {
				output = new Link(agentNode, node);
			}
		}
		return output;
	}

	public Link getLinkToCutToBlockAgent(Skynet1Node agentNode) {
		Link output = null;
		List<Skynet1Node> destinationsToExit = new ArrayList();
		for (Skynet1Node node : agentNode.destinations) {
			if (doesNodeGoToExitNode(node)) {
				destinationsToExit.add(node);
			}
		}

		if (destinationsToExit.size() == 1) {
			output = new Link(agentNode, destinationsToExit.get(0));
		}

		for (int i = 0; i < nodes.length; i++) {
			nodes[i].visited = false;
		}
		return output;
	}

	private boolean doesNodeGoToExitNode(Skynet1Node parentNode) {
		boolean nodeGoesToExitNode = false;
		parentNode.visited = true;
		for (Skynet1Node node : parentNode.destinations) {
			if (node.isExitNode) {
				nodeGoesToExitNode = true;
			}
			else {
				if (!node.visited) {
					nodeGoesToExitNode = doesNodeGoToExitNode(node);
				}
			}
		}
		return nodeGoesToExitNode;
	}

	public Link getLinkBetweenTwoRecursivelyValidNodes() {
		for (Skynet1Node node1 : nodes) {
			if (node1.hasThreeLinks() && node1.hasExitNodeLinked(nodes) && nodeIsValid(node1, 2)) {
				for (Skynet1Node node2 : node1.destinations) {
					if (!node2.isExitNode) {
						linkBetweenTwoRecursivelyValidNodes = new Link(node1, node2);
					}
				}
			}
		}
		return linkBetweenTwoRecursivelyValidNodes;
	}

	private boolean nodeIsValid(Skynet1Node parentNode, int depth) {
		int count = 0;
		for (Skynet1Node node : parentNode.destinations) {
			if (!node.isExitNode && node.hasThreeLinks() && node.hasExitNodeLinked(nodes)) {
				if (depth > 0) {
					if (nodeIsValid(node, depth - 1)) {
						count++;
					}
				}
				else {
					count++;
				}
			}
		}
		return count == 2;
	}

	public Link getDefaultLinkToAnExit() {
		Link output = null;
		for (Skynet1Node node : nodes) {
			if (node.isExitNode == true && node.destinations.size() > 0) {
				output = new Link(node, node.destinations.get(0));
			}
		}
		return output;
	}

	public void removeLinks(Link link) {
		removeLink(link.node1, link.node2);
		removeLink(link.node2, link.node1);
	}

	private void removeLink(Skynet1Node node1, Skynet1Node node2) {
		Iterator<Skynet1Node> iterator = node1.destinations.iterator();
		while (iterator.hasNext()) {
			Skynet1Node node = iterator.next();
			if (node.id == node2.id) {
				iterator.remove();
			}
		}
	}
}

class Skynet1Node {
	int id;
	boolean isExitNode = false;
	boolean visited = false;
	List<Skynet1Node> destinations = new ArrayList();

	public Skynet1Node(int id) {
		this.id = id;
	}

	public boolean hasExitNodeLinked(Skynet1Node[] nodes) {
		for (Skynet1Node node : destinations) {
			if (node.isExitNode) {
				return true;
			}
		}
		return false;
	}

	public boolean hasThreeLinks() {
		return destinations.size() == 3;
	}
}

class Link {
	Skynet1Node node1;
	Skynet1Node node2;

	public Link(Skynet1Node node1, Skynet1Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}
}
