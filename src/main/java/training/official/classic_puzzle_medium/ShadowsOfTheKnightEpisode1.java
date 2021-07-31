package training.official.classic_puzzle_medium;

import java.util.Scanner;

class ShadowsOfTheKnightEpisode1 {
	private static int minX = 0;
	private static int minY = 0;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int maxX = in.nextInt();
		int maxY = in.nextInt();
		in.nextInt(); // maximum number of turns before game over.
		int batX = in.nextInt();
		int batY = in.nextInt();

		// game loop
		while (true) {
			String bombDir = in.next(); // the direction of the bombs from batman's current location (U, UR, R, DR, D, DL, L or UL)

			switch (bombDir) {
				case "U":
					maxY = batY;
					minX = batX;
					maxX = batX;
					break;
				case "D":
					minY = batY;
					minX = batX;
					maxX = batX;
					break;
				case "L":
					maxX = batX;
					minY = batY;
					maxY = batY;
					break;
				case "R":
					minX = batX;
					minY = batY;
					maxY = batY;
					break;
				case "UR":
					minX = batX + 1;
					maxY = batY;
					break;
				case "UL":
					maxX = batX;
					maxY = batY;
					break;
				case "DR":
					minX = batX + 1;
					minY = batY + 1;
					break;
				case "DL":
					maxX = batX;
					minY = batY + 1;
					break;
			}

			batX = minX + ((maxX - minX) / 2);
			batY = minY + ((maxY - minY) / 2);

			System.out.println(batX + " " + batY);
		}
	}
}
