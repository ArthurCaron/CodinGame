package training.community.classic_puzzle_medium;

import java.util.Scanner;

public class SandpileAddition {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int sandpileSize = in.nextInt();

		int[][] sandpile = new int[sandpileSize][sandpileSize];

		if (in.hasNextLine()) { in.nextLine(); }

		for (int a = 0; a < 2; a++) {
			for (int i = 0; i < sandpileSize; i++) {
				String[] row = in.nextLine().split("");
				for (int j = 0; j < sandpileSize; j++) {
					sandpile[i][j] += Integer.parseInt(row[j]);
				}
			}
		}

		boolean isInEquilibrium = false;

		while (!isInEquilibrium) {
			isInEquilibrium = true;

			for (int i = 0; i < sandpileSize; i++) {
				for (int j = 0; j < sandpileSize; j++) {
					if (sandpile[i][j] >= 4) {
						isInEquilibrium = false;

						sandpile[i][j] -= 4;

						if (i > 0) {
							sandpile[i - 1][j]++;
						}

						if (i < sandpileSize - 1) {
							sandpile[i + 1][j]++;
						}

						if (j > 0) {
							sandpile[i][j - 1]++;
						}

						if (j < sandpileSize - 1) {
							sandpile[i][j + 1]++;
						}
					}
				}
			}
		}

		for (int i = 0; i < sandpileSize; i++) {
			for (int j = 0; j < sandpileSize; j++) {
				System.out.print(sandpile[i][j]);
			}
			System.out.println();
		}
	}
}
