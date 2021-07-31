package training.official.classic_puzzle_easy;

import java.util.Scanner;

class Temperatures {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int n = in.nextInt(); // the number of temperatures to analyse
		if (in.hasNextLine()) {
			in.nextLine();
		}
		String temps = in.nextLine(); // the n temperatures expressed as integers ranging from -273 to 5526

		if (n == 0) {
			System.out.println(0);
		}
		else {
			int result = 5526;
			String[] inputs = temps.trim().split(" ");

			for (String input : inputs) {
				int temp = Integer.parseInt(input);
				int absTemp = Math.abs(temp);
				int absResult = Math.abs(result);

				if ((absTemp < absResult) || (absTemp == absResult && temp > 0)) {
					result = temp;
				}
			}

			System.out.println(result);
		}
	}
}
