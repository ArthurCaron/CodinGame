//package training.official.classic_puzzle_easy

import java.util.*

fun main() {
	val scanner = Scanner(System.`in`)

	while (true) {
		(0 until 8)
			.map { TheDescent.Input(it, scanner.nextInt()) }
			.maxByOrNull { it.mountainHeight }
			?.also { println(it.index) }
	}
}

private class TheDescent { // only here for codingame
	data class Input(val index: Int, val mountainHeight: Int)
}
