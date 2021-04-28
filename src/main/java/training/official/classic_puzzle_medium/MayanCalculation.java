package training.official.classic_puzzle_medium;

import java.util.LinkedList;
import java.util.Scanner;

class MayanCalculation {
	private static final int BASE = 20;

	public static void main(String args[]) {
		Scanner inputs = new Scanner(System.in);
		int digitWidth = inputs.nextInt();
		int digitHeight = inputs.nextInt();

		Digit[] numerals = constructNumerals(inputs, digitHeight, digitWidth);

		Digit[] firstNumber = constructNextNumber(inputs, numerals, digitHeight, digitWidth);
		Digit[] secondNumber = constructNextNumber(inputs, numerals, digitHeight, digitWidth);

		long result = calculate(firstNumber, inputs.next(), secondNumber);

		LinkedList<Digit> resultDigits = longToDigits(numerals, result);

		for (Digit digit : resultDigits) {
			print(digit);
		}
	}

	private static Digit[] constructNumerals(Scanner inputs, int digitHeight, int digitWidth) {
		Digit[] numerals = new Digit[BASE];

		for (int i = 0; i < numerals.length; i++) {
			numerals[i] = new Digit(digitHeight, digitWidth);
		}

		for (int i = 0; i < digitHeight; i++) {
			String line = inputs.next();

			for (int j = 0; j < numerals.length; j++) {
				numerals[j].setLine(i, line.substring(j * digitWidth, (j * digitWidth) + digitWidth));
			}
		}

		return numerals;
	}

	private static Digit[] constructNextNumber(Scanner inputs, Digit[] numerals, int digitHeight, int digitWidth) {
		Digit[] number = new Digit[inputs.nextInt() / digitHeight];

		for (int i = 0; i < number.length; i++) {
			number[i] = new Digit(digitHeight, digitWidth);

			for (int j = 0; j < digitHeight; j++) {
				number[i].setLine(j, inputs.next());
			}

			number[i].evaluation = Digit.evaluateDigit(numerals, number[i]);
		}

		return number;
	}

	private static long calculate(Digit[] firstNumber, String operation, Digit[] secondNumber) {
		switch (operation) {
			case "+":
				return digitsToLong(firstNumber) + digitsToLong(secondNumber);
			case "-":
				return digitsToLong(firstNumber) - digitsToLong(secondNumber);
			case "*":
				return digitsToLong(firstNumber) * digitsToLong(secondNumber);
			case "/":
				return digitsToLong(firstNumber) / digitsToLong(secondNumber);
			default:
				return 0L;
		}
	}

	private static long digitsToLong(Digit[] number) {
		long result = 0;

		for (int i = 0; i < number.length; i++) {
			result += number[i].evaluation * Math.pow(20, number.length - 1 - i);
		}

		return result;
	}

	private static LinkedList<Digit> longToDigits(Digit[] numerals, long value) {
		LinkedList<Digit> result = new LinkedList<>();

		do {
			result.addFirst(numerals[(int) (value % 20)]);
			value = value / 20;
		} while (value > 0);

		return result;
	}

	private static void print(Digit digit) {
		for (int i = 0; i < digit.height; i++) {
			System.out.println(digit.getLine(i));
		}
	}
}

class Digit {
	int height;
	private int width;
	int evaluation;

	private char[][] symbols;

	Digit(int height, int width) {
		this.height = height;
		this.width = width;
		symbols = new char[height][width];
	}

	void setLine(int heightIndex, String line) {
		symbols[heightIndex] = line.toCharArray();
	}

	String getLine(int heightIndex) {
		return new String(symbols[heightIndex]);
	}

	static int evaluateDigit(Digit[] numerals, Digit digitToEvaluate) {
		for (int i = 0; i < numerals.length; i++) {
			if (compareDigits(numerals[i], digitToEvaluate)) {
				return i;
			}
		}
		return -1;
	}

	private static boolean compareDigits(Digit digit1, Digit digit2) {
		for (int i = 0; i < digit1.height; i++) {
			for (int j = 0; j < digit1.width; j++) {
				if (digit1.symbols[i][j] != digit2.symbols[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
}
