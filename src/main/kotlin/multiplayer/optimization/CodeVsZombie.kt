//package multiplayer.optimization

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

fun main(args: Array<String>) {
	CodeVsZombie().run()
}

class CodeVsZombie {
	private fun updateLoopObjects(input: Scanner): Triple<Player, Map<Int, Vector>, Map<Int, Zombie>> {
		val player = Player(Vector(input.nextInt(), input.nextInt()))
		val humans = (0 until input.nextInt()).associate { input.nextInt() to Vector(input.nextInt(), input.nextInt()) }
		val zombies = (0 until input.nextInt()).associate {
			val pair = input.nextInt() to Zombie(Vector(input.nextInt(), input.nextInt()))
			Vector(input.nextInt(), input.nextInt()) // ignored zombie next position
			pair
		}
		return Triple(player, humans, zombies)
	}

	fun run() {
		val input = Scanner(System.`in`)

		// game loop
		while (true) {
			val (player, humans, zombies) = updateLoopObjects(input)

			val zombiesNextTurn = zombies.mapValues { (_, zombie) -> zombie.calculateNextZombie(player, humans) }
			zombies.keys.forEach { zombieId ->
				System.err.println("zombies         [$zombieId] pos: ${zombies[zombieId]?.zombiePosition}")
				System.err.println("zombiesNextTurn [$zombieId] pos: ${zombiesNextTurn[zombieId]?.zombiePosition}")
			}
			humans.keys.forEach { humanId ->
				System.err.println("humans          [$humanId] pos: ${humans[humanId]}")
			}

//			player.moveTo(Vector(0, 0))
//			val zombieTarget = player.closestZombie(zombies)
//			player.moveTo(zombieTarget)
			val humanTarget = player.closestCreature(humans)
			player.moveTo(humanTarget)
		}
	}
}

class Player(val playerPosition: Vector) {
	private val speed = 1000.0

	fun closestZombie(zombies: Map<Int, Zombie>) = closestCreature(zombies.mapValues { (_, zombie) -> zombie.zombiePosition })

	fun closestCreature(creatures: Map<Int, Vector>): Vector {
		return creatures.minBy { (_, creature) -> playerPosition.distanceTo(creature) }?.value ?: throw Exception()
	}

	fun moveTo(position: Vector) {
		playerPosition.moveTo(position, speed)
			.apply {
				println("${x.toInt()} ${y.toInt()}")
			}
	}
}

class Zombie(val zombiePosition: Vector) {
	private val speed = 400.0

	fun calculateNextZombie(player: Player, humans: Map<Int, Vector>): Zombie {
		return Zombie(calculateNextZombiePosition(player, humans))
	}

	private fun calculateNextZombiePosition(player: Player, humans: Map<Int, Vector>): Vector {
		val personsPosition = humans.toMutableMap()
		personsPosition[Int.MIN_VALUE] = player.playerPosition
		val (_, closestPerson) = personsPosition.minBy { (_, person) -> zombiePosition.distanceTo(person) } ?: throw Exception()
		return zombiePosition.moveTo(closestPerson, speed)
	}
}

data class Vector(val x: Double, val y: Double) {
	constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

	val magnitude by lazy { sqrt(x.pow(2) + y.pow(2)) }
	val length by lazy { magnitude }
	val direction by lazy { Vector(x / magnitude, y / magnitude) }
	val normalized by lazy { direction }

	operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
	operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)
	operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)

	fun vectorTo(other: Vector) = (other - this)
	fun distanceTo(other: Vector) = vectorTo(other).magnitude
	fun directionTo(other: Vector) = vectorTo(other).direction

	fun moveTo(other: Vector, speed: Double): Vector {
		if (distanceTo(other) <= speed) {
			return other
		}
		val moveVector = directionTo(other) * speed
		return Vector(x + moveVector.x.toInt(), y + moveVector.y.toInt())
	}

	fun print() = "${x.toInt()} ${y.toInt()}"
}
