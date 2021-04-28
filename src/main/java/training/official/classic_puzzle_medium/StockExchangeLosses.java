package training.official.classic_puzzle_medium;

import java.util.Scanner;

class StockExchangeLosses {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int n = in.nextInt();

		int buyingPrice = Integer.MIN_VALUE;
		int maxLoss = 0;

		for (int i = 0; i < n; i++) {
			int value = in.nextInt();

			if (value > buyingPrice) {
				buyingPrice = value;
			}

			int currentLoss = value - buyingPrice;

			if (maxLoss > currentLoss) {
				maxLoss = currentLoss;
			}
		}

		System.out.println(maxLoss);
	}
}
