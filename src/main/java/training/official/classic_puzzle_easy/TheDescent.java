package training.official.classic_puzzle_easy;

import java.util.Scanner;

class TheDescent {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		while (true) {
			int target = 0;
			int targetH = 0;

			for (int i = 0; i < 8; i++) {
				int mountainH = in.nextInt();

				if (targetH < mountainH) {
					target = i;
					targetH = mountainH;
				}
			}

			System.out.println(target);
		}
	}
}
