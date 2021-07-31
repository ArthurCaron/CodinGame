package utils

import kotlin.math.pow
import kotlin.math.sqrt

data class Vector(val x: Double, val y: Double) {
	constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())
	val magnitude by lazy { sqrt(x.pow(2) + y.pow(2)) }
	val direction by lazy { Vector(x / magnitude, y / magnitude) }

	operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)

	operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)

	operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)

	fun normalize() = direction

	fun distance(other: Vector) = (other - this).magnitude

	fun moveTo(destination: Vector, speed: Double) = (destination - this).direction * speed
}
