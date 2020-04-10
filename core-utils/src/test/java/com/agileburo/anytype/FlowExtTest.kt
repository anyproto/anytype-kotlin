package com.agileburo.anytype

import com.agileburo.anytype.core_utils.ext.withLatestFrom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class FlowExtTest {

    @Test
    fun `should apply withLatestFrom operator for several streams`() = runBlocking {

        run {
            val flow = flowOf('a', 'b', 'c').onEach { delay(1000) }

            val other = (1..5).asFlow().onEach { delay(100) }

            val another = flowOf("I", "II", "III", "IV", "V").onEach { delay(100) }

            val result = flow.withLatestFrom(other, another) { a, b, c -> "$a$b$c" }.toList()

            val expected = listOf("a5V", "b5V", "c5V")

            assertEquals(
                expected = expected,
                actual = result
            )
        }

        run {
            val flow = flowOf('a', 'b', 'c').onEach { delay(10) }

            val other = (1..5).asFlow().onEach { delay(100) }

            val another = flowOf("I", "II", "III", "IV", "V").onEach { delay(100) }

            val result = flow.withLatestFrom(other, another) { a, b, c -> "$a$b$c" }.toList()

            val expected = listOf<String>()

            assertEquals(
                expected = expected,
                actual = result
            )
        }

        run {
            val flow = flowOf('a', 'b', 'c').onEach { delay(35) }

            val other = (1..5).asFlow().onEach { delay(100) }

            val another = flowOf("I", "II", "III", "IV", "V").onEach { delay(100) }

            val result = flow.withLatestFrom(other, another) { a, b, c -> "$a$b$c" }.toList()

            val expected = listOf("c1I")

            assertEquals(
                expected = expected,
                actual = result
            )
        }

        run {
            val flow = flowOf('a', 'b', 'c', 'd', 'e').onEach { delay(55) }

            val other = (1..5).asFlow().onEach { delay(100) }

            val another = flowOf("I", "II", "III", "IV", "V").onEach { delay(100) }

            val result = flow.withLatestFrom(other, another) { a, b, c -> "$a$b$c" }.toList()

            val expected = listOf("b1I", "c1I", "d2II", "e2II")

            assertEquals(
                expected = expected,
                actual = result
            )
        }
    }

}