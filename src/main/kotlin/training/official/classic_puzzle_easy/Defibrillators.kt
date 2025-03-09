//package training.official.classic_puzzle_easy

import java.util.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
	val (position, defibrillators) = Defibrillators.extractInputData()

	val closestDefibrillator = defibrillators.minByOrNull { defibrillator -> Defibrillators.calculateDistance(position, defibrillator.position) }

	println(closestDefibrillator?.name ?: "")
}

private class Defibrillators { // only here for codingame
	companion object {
		fun calculateDistance(userPosition: Input.Position, defibrillatorPosition: Input.Position): Double {
			return calculateDistance(userPosition.longitude, userPosition.latitude, defibrillatorPosition.longitude, defibrillatorPosition.latitude)
		}

		fun calculateDistance(longitudeA: Double, latitudeA: Double, longitudeB: Double, latitudeB: Double): Double {
			val earthRadius = 6371
			val x = (longitudeB - longitudeA) * cos((latitudeA + latitudeB).div(2))
			val y = (latitudeB - latitudeA)
			return sqrt(x.pow(2) + y.pow(2)) * earthRadius
		}

		fun extractInputData(): Input {
			return Scanner(System.`in`).run {
				val longitude = next()
				val latitude = next()
				val numberOfDefibrillators = nextInt()
				if (hasNextLine()) {
					nextLine()
				}
				val defibrillators = (0 until numberOfDefibrillators)
					.map { nextLine() }
					.map { it.split(";") }
					.map { Input.Defibrillator(it[1], Input.Position(it[4], it[5])) }
					.toList()
				Input(Input.Position(longitude, latitude), defibrillators)
			}
		}
	}

	data class Input(val position: Position, val defibrillators: List<Defibrillator>) {
		data class Position(private val _longitude: String, private val _latitude: String) {
			val longitude: Double
				get() = _longitude.replaceCommaWithDot().toDouble()
			val latitude: Double
				get() = _latitude.replaceCommaWithDot().toDouble()

			private fun String.replaceCommaWithDot() = this.replace(",", ".")
		}

		data class Defibrillator(val name: String, val position: Position)
	}
}
