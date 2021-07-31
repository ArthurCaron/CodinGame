package training.official.classic_puzzle_medium;

import java.util.Arrays;
import java.util.Scanner;

class NetworkCabling {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		long minX = Integer.MAX_VALUE;
		long maxX = Integer.MIN_VALUE;

		int nbPoints = in.nextInt();
		long[] yValues = new long[nbPoints];

		for (int i = 0; i < nbPoints; i++) {
			long X = in.nextInt();
			long Y = in.nextInt();

			minX = Math.min(minX, X);
			maxX = Math.max(maxX, X);

			yValues[i] = Y;
		}

		Arrays.sort(yValues);

		long median = yValues[nbPoints / 2];

		long result = maxX - minX;

		for (long y : yValues) {
			result += Math.abs(y - median);
		}

		System.out.println(result);
	}
}
