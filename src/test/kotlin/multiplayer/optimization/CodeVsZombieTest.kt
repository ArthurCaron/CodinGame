//package multiplayer.optimization

import org.junit.jupiter.api.Test
import utils.Vector
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util.*

private fun insertData() {
	val data = """
		1000 1000
		1
		5 500 500
		1
		20 0 0 282 282
	""".trimIndent()
		.repeat(10000)
	val input = ByteArrayInputStream(data.toByteArray(Charset.defaultCharset()))
	System.setIn(input)
}

class CodeVsZombieTest {
	@Test
	fun test() {
		insertData()
		CodeVsZombie().run()
	}
}
