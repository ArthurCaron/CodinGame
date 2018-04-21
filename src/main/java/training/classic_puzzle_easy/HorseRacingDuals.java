package training.classic_puzzle_easy;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class HorseRacingDuals {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int N = in.nextInt();

		int[] horsesPower = new int[N];
		for (int i = 0; i < N; i++) {
			horsesPower[i] = in.nextInt();
		}
		Arrays.sort(horsesPower);

		int answer = Integer.MAX_VALUE;
		for (int i = 1; i < horsesPower.length; i++) {
			int currentPowerDiff = horsesPower[i] - horsesPower[i - 1];
			if (currentPowerDiff < answer) {
				answer = currentPowerDiff;
			}
		}

		System.out.println(answer);
	}
}
