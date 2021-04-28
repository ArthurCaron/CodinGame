package training.official.classic_puzzle_medium;

import java.util.Scanner;

class ThereIsNoSpoonEpisode1 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int xSize = in.nextInt(); // the number of cells on the X axis
		int ySize = in.nextInt(); // the number of cells on the Y axis
		if (in.hasNextLine()) {
			in.nextLine();
		}

		char[][] grid = new char[xSize][ySize];

		for (int y = 0; y < ySize; y++) {
			char[] column = in.nextLine().toCharArray(); // xSize characters, each either 0 or .
			for (int x = 0; x < xSize; x++) {
				grid[x][y] = column[x];
			}
		}

		int rightX, rightY, bottomX, bottomY;

		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				if (grid[x][y] == '0') {
					rightX = -1;
					rightY = -1;
					for (int x2 = x + 1; x2 < xSize && rightX == -1; x2++) {
						if (grid[x2][y] == '0') {
							rightX = x2;
							rightY = y;
						}
					}

					bottomX = -1;
					bottomY = -1;
					for (int y2 = y + 1; y2 < ySize && bottomY == -1; y2++) {
						if (grid[x][y2] == '0') {
							bottomX = x;
							bottomY = y2;
						}
					}

					System.out.println(x + " " + y + " " + rightX + " " + rightY + " " + bottomX + " " + bottomY);
				}
			}
		}
	}
}
