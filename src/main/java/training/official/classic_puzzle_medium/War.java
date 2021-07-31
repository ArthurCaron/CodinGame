package training.official.classic_puzzle_medium;

import java.util.LinkedList;
import java.util.Scanner;

class War {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		LinkedList<Integer> player1Cards = new LinkedList<>();
		LinkedList<Integer> player2Cards = new LinkedList<>();

		int n = in.nextInt();
		for (int i = 0; i < n; i++) {
			player1Cards.add(getCardValue(in.next()));
		}

		int m = in.nextInt();
		for (int i = 0; i < m; i++) {
			player2Cards.add(getCardValue(in.next()));
		}


		LinkedList<Integer> player1Buffer = new LinkedList<>();
		LinkedList<Integer> player2Buffer = new LinkedList<>();
		int rounds = 0;

		while (player1Cards.size() != 0 && player2Cards.size() != 0) {
			int player1Card = player1Cards.poll();
			int player2Card = player2Cards.poll();

			player1Buffer.add(player1Card);
			player2Buffer.add(player2Card);

			if (player1Card > player2Card) {
				rounds++;
				player1Cards.addAll(player1Buffer);
				player1Cards.addAll(player2Buffer);
				player1Buffer.clear();
				player2Buffer.clear();
			}
			else if (player1Card < player2Card) {
				rounds++;
				player2Cards.addAll(player1Buffer);
				player2Cards.addAll(player2Buffer);
				player2Buffer.clear();
				player1Buffer.clear();
			}
			else {
				player1Buffer.add(player1Cards.poll());
				player1Buffer.add(player1Cards.poll());
				player1Buffer.add(player1Cards.poll());

				player2Buffer.add(player2Cards.poll());
				player2Buffer.add(player2Cards.poll());
				player2Buffer.add(player2Cards.poll());
			}
		}

		if (player1Buffer.size() != 0 || player2Buffer.size() != 0) {
			System.out.println("PAT");
		}
		else if (player1Cards.size() == 0) {
			System.out.println("2 " + rounds);
		}
		else if (player2Cards.size() == 0) {
			System.out.println("1 " + rounds);
		}
	}

	private static int getCardValue(String playerCard) {
		String cardSymbol = playerCard.substring(0, playerCard.length() - 1);

		switch (cardSymbol) {
			case "J":
				return 11;
			case "Q":
				return 12;
			case "K":
				return 13;
			case "A":
				return 14;
			default:
				return Integer.parseInt(cardSymbol);
		}
	}
}
