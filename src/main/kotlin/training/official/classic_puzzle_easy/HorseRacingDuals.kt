//package training.official.classic_puzzle_easy

import java.util.*

fun main() {
	val horses = HorseRacingDuals.extractInputData()

	val sortedHorses = horses.sortedBy { it.horsePower }
	var minDiff = Int.MAX_VALUE
	(1 until sortedHorses.size).forEach { i ->
		minDiff = minOf(minDiff, sortedHorses[i].horsePower - sortedHorses[i - 1].horsePower)
	}

	println(minDiff)
}

private class HorseRacingDuals { // only here for codingame
	companion object {
		fun extractInputData(): List<Input> {
			return Scanner(System.`in`).run {
				(0 until nextInt())
					.map { Input(nextInt()) }
					.toList()
			}
		}
	}

	data class Input(val horsePower: Int)
}
