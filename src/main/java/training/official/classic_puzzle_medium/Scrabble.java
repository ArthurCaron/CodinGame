package training.official.classic_puzzle_medium;

import java.util.HashMap;
import java.util.Scanner;

class Scrabble {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int numberWords = in.nextInt();
		if (in.hasNextLine()) { in.nextLine(); }

		String[] dictionary = new String[numberWords];
		for (int i = 0; i < numberWords; i++) {
			dictionary[i] = in.nextLine();
		}

		HashMap<Character, Integer> availableLetters = getAvailableLetters(in.nextLine());

		HashMap<Character, Integer> scorePerLetter = getScorePerLetter();

		String bestWord = "";
		int bestWordScore = 0;

		for (String word : dictionary) {
			if (isMatch(availableLetters, word)) {
				int score = computeScore(scorePerLetter, word);
				if (score > bestWordScore) {
					bestWord = word;
					bestWordScore = score;
				}
			}
		}

		System.out.println(bestWord);
	}

	private static boolean isMatch(HashMap<Character, Integer> availableLetters, String word) {
		HashMap<Character, Integer> wordLettersCount = new HashMap<>();

		for (Character letter : word.toCharArray()) {
			if (wordLettersCount.containsKey(letter)) {
				wordLettersCount.put(letter, wordLettersCount.get(letter) + 1);
			} else {
				wordLettersCount.put(letter, 1);
			}

			if (wordLettersCount.get(letter) > availableLetters.getOrDefault(letter, 0)) {
				return false;
			}
		}

		return true;
	}

	private static int computeScore(HashMap<Character, Integer> scorePerLetter, String word) {
		int score = 0;

		for (Character letter : word.toCharArray()) {
			score += scorePerLetter.get(letter);
		}

		return score;
	}

	private static HashMap<Character, Integer> getAvailableLetters(String word) {
		HashMap<Character, Integer> availableLetters = new HashMap<>();

		for (Character letter : word.toCharArray()) {
			if (availableLetters.containsKey(letter)) {
				availableLetters.put(letter, availableLetters.get(letter) + 1);
			} else {
				availableLetters.put(letter, 1);
			}
		}

		return availableLetters;
	}

	private static HashMap<Character, Integer> getScorePerLetter() {
		HashMap<Character, Integer> scorePerLetter = new HashMap<>();

		scorePerLetter.put('e', 1);
		scorePerLetter.put('a', 1);
		scorePerLetter.put('i', 1);
		scorePerLetter.put('o', 1);
		scorePerLetter.put('n', 1);
		scorePerLetter.put('r', 1);
		scorePerLetter.put('t', 1);
		scorePerLetter.put('l', 1);
		scorePerLetter.put('s', 1);
		scorePerLetter.put('u', 1);
		scorePerLetter.put('d', 2);
		scorePerLetter.put('g', 2);
		scorePerLetter.put('b', 3);
		scorePerLetter.put('c', 3);
		scorePerLetter.put('m', 3);
		scorePerLetter.put('p', 3);
		scorePerLetter.put('f', 4);
		scorePerLetter.put('h', 4);
		scorePerLetter.put('v', 4);
		scorePerLetter.put('w', 4);
		scorePerLetter.put('y', 4);
		scorePerLetter.put('k', 5);
		scorePerLetter.put('j', 8);
		scorePerLetter.put('x', 8);
		scorePerLetter.put('q', 10);
		scorePerLetter.put('z', 10);

		return scorePerLetter;
	}
}
