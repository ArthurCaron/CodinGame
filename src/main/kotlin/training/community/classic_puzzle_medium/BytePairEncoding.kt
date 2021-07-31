package training.community.classic_puzzle_medium

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util.*

fun main() {
	insertData()
	val inScanner = Scanner(System.`in`)
	val n = inScanner.nextInt()
	val m = inScanner.nextInt()

	val input = (0 until n)
		.map { inScanner.next() }
		.reduce { acc, i -> acc + i }

	if (input.length <= 1) {
		println(input)
	}

	var result = input
	val nonTerminals = mutableListOf<Pair<String, String>>()

	for (currentNonTerminal in NonTerminalIterator()) {
		val pairCount = result.windowed(2)
			.fold(PairCount()) { pairCount, pair ->
				pairCount.insertPair(pair)
			}
		val (maxKey, maxValue) = pairCount.maxPair()

		if (maxValue <= 1) {
			break
		}

		result = result.replace(maxKey, currentNonTerminal)
		nonTerminals.add(currentNonTerminal to maxKey)
	}

	println(result)
	nonTerminals.forEach { (key, value) ->
		println("$key = $value")
	}
}

class PairCount {
	private val pairCount = mutableMapOf<String, Int>()
	private var previousPair = ""

	fun insertPair(pair: String): PairCount {
		previousPair =
			if (previousPair != pair) {
				pairCount.merge(pair, 1) { oldValue, _ -> oldValue + 1 }
				pair
			} else {
				""
			}
		return this
	}

	fun maxPair() = pairCount.maxBy { it.value } ?: AbstractMap.SimpleEntry("", 0)
}

class NonTerminalIterator : Iterator<String> {
	private var currentNonTerminal: Char = 'Z'.inc()

	override fun hasNext() = currentNonTerminal > 'A'

	override fun next(): String {
		if (hasNext()) {
			currentNonTerminal = currentNonTerminal.dec()
			return currentNonTerminal.toString()
		} else {
			throw NoSuchElementException()
		}
	}
}

fun insertData() {
	val data = """
		1 11
		aaabdaaabac
	""".trimIndent()
	val input = ByteArrayInputStream(data.toByteArray(Charset.defaultCharset()))
	System.setIn(input)
}
