package training.official.classic_puzzle_easy;

import java.util.Scanner;

// Solution with no bit operators
// Tried to get something easy to read
class ChuckNorris {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		String message = in.nextLine();

		StringBuilder strBuilder = new StringBuilder();
		char lastBit = '#';

		for (char letter : message.toCharArray()) {
			char[] letterBits = Integer.toBinaryString((int) letter).toCharArray();

			for (int i = 0; i < (7 - letterBits.length); i++) {
				appendNewBit(strBuilder, lastBit, '0');
				lastBit = '0';
			}

			for (char currentBit : letterBits) {
				appendNewBit(strBuilder, lastBit, currentBit);
				lastBit = currentBit;
			}
		}

		System.out.println(strBuilder.toString().trim());
	}

	private static void appendNewBit(StringBuilder strBuilder, char lastBit, char currentBit) {
		if (currentBit != lastBit) {
			if (currentBit == '1') {
				strBuilder.append(" 0 0");
			} else {
				strBuilder.append(" 00 0");
			}
		} else {
			strBuilder.append("0");
		}
	}
}
