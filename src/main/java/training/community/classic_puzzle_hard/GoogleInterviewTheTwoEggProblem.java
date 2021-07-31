package training.community.classic_puzzle_hard;

import java.util.Scanner;

public class GoogleInterviewTheTwoEggProblem {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		System.out.println( (int) Math.ceil( Math.sqrt(0.25 + 2 * in.nextInt()) - 0.5 ) );
	}
}
