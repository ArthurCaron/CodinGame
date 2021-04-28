package training.official.classic_puzzle_medium;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class ConwaySequence {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		List<Integer> currentLine = new ArrayList<>();
		currentLine.add(in.nextInt());

		for (int i = in.nextInt() - 1; i > 0; i--) {
			currentLine = getNextLine(currentLine);
		}

		StringBuilder result = new StringBuilder();
		for (int value : currentLine) {
			result.append(" ");
			result.append(value);
		}

		System.out.println(result.toString().trim());
	}

	private static List<Integer> getNextLine(List<Integer> currentLine) {
		List<Integer> nextLine = new ArrayList<>();

		int currentValue = currentLine.get(0);
		int currentCount = 0;

		for (int value : currentLine) {
			if (currentValue == value) {
				currentCount++;
			}
			else {
				nextLine.add(currentCount);
				nextLine.add(currentValue);
				currentValue = value;
				currentCount = 1;
			}
		}
		nextLine.add(currentCount);
		nextLine.add(currentValue);

		return nextLine;
	}
}
