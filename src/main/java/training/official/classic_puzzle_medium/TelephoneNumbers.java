package training.official.classic_puzzle_medium;

import java.util.Scanner;

class TelephoneNumbers {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		TelephoneGraph graph = new TelephoneGraph();

		int N = in.nextInt();
		for (int i = 0; i < N; i++) {
			graph.add(in.next());
		}

		System.out.println(graph.count());
	}
}

class TelephoneGraph {
	private TelephoneGraph[] graphs = new TelephoneGraph[10];

	void add(String numbers) {
		if (numbers.length() > 0) {
			int firstNumber = Integer.parseInt(numbers.substring(0, 1));
			if (graphs[firstNumber] == null) {
				graphs[firstNumber] = new TelephoneGraph();
			}

			numbers = numbers.substring(1, numbers.length());
			graphs[firstNumber].add(numbers);
		}
	}

	int count() {
		int count = 0;
		for (TelephoneGraph graph : graphs) {
			if (graph != null) {
				count += graph.count() + 1;
			}
		}
		return count;
	}
}
