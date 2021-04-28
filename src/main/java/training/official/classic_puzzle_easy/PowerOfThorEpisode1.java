package training.official.classic_puzzle_easy;

import java.util.Scanner;

// Clearly not the most readable solution
// I was trying to limit:
// the number of calls inside the loop
// the number of String instantiations
// the number of calculations
class PowerOfThorEpisode1 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int lightX = in.nextInt(); // the X position of the light of power
		int lightY = in.nextInt(); // the Y position of the light of power
		int initialTX = in.nextInt(); // Thor's starting X position
		int initialTY = in.nextInt(); // Thor's starting Y position

		String verticalCommand = "N";
		String horizontalCommand = "W";
		int verticalDistance = initialTY - lightY;
		int horizontalDistance = initialTX - lightX;

		if (verticalDistance < 0) {
			verticalCommand = "S";
			verticalDistance = -verticalDistance;
		}

		if (horizontalDistance < 0) {
			horizontalCommand = "E";
			horizontalDistance = -horizontalDistance;
		}

		while (true) {
			int remainingTurns = in.nextInt(); // The remaining amount of turns Thor can move. Do not remove this line.

			if (verticalDistance == 0) { verticalCommand = ""; }
			if (horizontalDistance == 0) { horizontalCommand = ""; }

			System.out.println(verticalCommand + horizontalCommand);

			verticalDistance--;
			horizontalDistance--;
		}
	}
}
