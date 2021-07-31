package training.official.classic_puzzle_medium;

import java.util.Arrays;
import java.util.Scanner;

class TheGift {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int participants = in.nextInt();
		int giftPrice = in.nextInt();
		int totalBudget = 0;

		int[] budgets = new int[participants];

		for (int i = 0; i < participants; i++) {
			budgets[i] = in.nextInt();
			totalBudget += budgets[i];
		}

		if (totalBudget < giftPrice) {
			System.out.println("IMPOSSIBLE");
		} else {
			Arrays.sort(budgets);

			for (int i = 0; i < participants; i++) {
				int medianPrice = giftPrice / (participants - i);

				if (budgets[i] < medianPrice) {
					giftPrice -= budgets[i];
					System.out.println(budgets[i]);
				} else {
					giftPrice -= medianPrice;
					System.out.println(medianPrice);
				}
			}
		}
	}
}

