package training.classic_puzzle_easy;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class AsciiArt {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int letterWidth = in.nextInt();
		int H = in.nextInt(); // not used
		if (in.hasNextLine()) {
			in.nextLine();
		}
		int QUESTION_MARK = 26; // alphabet + '?', starting at index 0
		int ASCII_UPPERCASE_A_VALUE = 65;
		StringBuilder strBuilder = new StringBuilder();

		String textToOuput = in.nextLine().toUpperCase();
		while (in.hasNextLine()) {
			String ROW = in.nextLine();

			for (int i = 0; i < textToOuput.length(); i++) {
				char c = textToOuput.charAt(i);

				int letterPosition;
				if (Character.isLetter(c)) {
					letterPosition = (c - ASCII_UPPERCASE_A_VALUE) * letterWidth;
				} else {
					letterPosition = QUESTION_MARK * letterWidth;
				}

				strBuilder.append(ROW.substring(letterPosition, letterPosition + letterWidth));
			}

			strBuilder.append(System.getProperty("line.separator"));
		}

		System.out.println(strBuilder.toString());
	}
}