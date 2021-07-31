//package multiplayer.optimization
//
//import java.util.*
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//fun main(args: Array<String>) {
//	CodeVsZombie().run()
//	println("0 0")
//}
//
//class CodeVsZombie {
//	fun run() {
//		val input = Scanner(System.`in`)
//
//		// game loop
//		while (true) {
//			val (player, humans, zombies) = updateLoopObjects(input)
//
//			val map = zombies.map { (_, zombie) -> zombie.zombieNextPosition to zombie.calculateNextZombiePosition(player, humans) }
//
//			for (pair in map) {
//				if (pair.first == pair.second) System.err.print("YES") else System.err.print(pair.first.toString() + " " + pair.second.toString())
//			}
//
//			println("0 0")
//		}
//	}
//}
//
//private fun updateLoopObjects(input: Scanner): Triple<Player, MutableMap<Int, Human>, MutableMap<Int, Zombie>> {
//	val player = Player(Vector(input.nextInt(), input.nextInt()))
//	val humans = mutableMapOf<Int, Human>()
//	val zombies = mutableMapOf<Int, Zombie>()
//
//	val humanCount = input.nextInt()
//	for (i in 0 until humanCount) {
//		val humanId = input.nextInt()
//		humans[humanId] = Human(Vector(input.nextInt(), input.nextInt()))
//	}
//
//	val zombieCount = input.nextInt()
//	for (i in 0 until zombieCount) {
//		val zombieId = input.nextInt()
//		zombies[zombieId] = Zombie(Vector(input.nextInt(), input.nextInt()), Vector(input.nextInt(), input.nextInt()))
//	}
//
//	return Triple(player, humans, zombies)
//}
//
//class Player(val playerPosition: Vector)
//
//class Human(val humanPosition: Vector)
//
//class Zombie(val zombiePosition: Vector, val zombieNextPosition: Vector) {
//	private val speed = 400.0
//
//	fun calculateNextZombie(): Zombie {
//		return Zombie(zombieNextPosition, Vector(1, 1))
//	}
//
//	fun calculateNextZombiePosition(player: Player, humans: MutableMap<Int, Human>): Vector {
//		val distanceToPlayer = zombiePosition.distance(player.playerPosition)
//
//		val map: List<Pair<Human, Double>> = humans.map { (_, human) -> human to zombiePosition.distance(human.humanPosition) }
//		val (closestHuman, distanceToClosestHuman) = map.minBy { pair -> pair.second } ?: throw Exception()
//
//		System.err.print(distanceToPlayer)
//		System.err.print(distanceToClosestHuman)
//		return if (distanceToPlayer < distanceToClosestHuman) moveTo(player) else moveTo(closestHuman)
//	}
//
//	private fun moveTo(player: Player) = moveTo(player.playerPosition)
//
//	private fun moveTo(human: Human) = moveTo(human.humanPosition)
//
//	private fun moveTo(vector: Vector): Vector {
//		val moveVector = zombiePosition.moveTo(vector, speed)
//		return Vector(moveVector.x.toInt(), moveVector.y.toInt())
//	}
//}
//
//data class Vector(val x: Double, val y: Double) {
//	constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())
//
//	val magnitude by lazy { sqrt(x.pow(2) + y.pow(2)) }
//	val direction by lazy { Vector(x / magnitude, y / magnitude) }
//
//	operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
//
//	operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)
//
//	operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)
//
//	fun normalize() = direction
//
//	fun distance(other: Vector) = (other - this).magnitude
//
//	fun moveTo(destination: Vector, speed: Double) = (destination - this).direction * speed
//}
