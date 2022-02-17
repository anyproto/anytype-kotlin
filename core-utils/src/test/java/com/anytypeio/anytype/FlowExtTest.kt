package com.anytypeio.anytype

import com.anytypeio.anytype.core_utils.ext.switchToLatestFrom
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class FlowExtTest {


    @Test
    fun `should emit latest value from B whenever A emits something`() = runBlockingTest {

        val aChannel = Channel<Int>()
        val bChannel = Channel<String>()

        val aFlow = aChannel.consumeAsFlow()
        val bFlow = bChannel.consumeAsFlow()

        launch {
            val result = aFlow.switchToLatestFrom(bFlow).toList()
            assertEquals(
                expected = listOf("A", "B", "D"),
                actual = result
            )
        }

        launch {
            bChannel.send("A")
            aChannel.send(1)
            bChannel.send("B")
            aChannel.send(2)
            bChannel.send("C")
            bChannel.send("D")
            aChannel.send(2)
            bChannel.send("E")
            bChannel.send("F")
            bChannel.send("G")
        }

        aChannel.close()
        bChannel.close()
    }

    @Test
    fun `should apply withLatestFrom operator for several streams`() = runTest {

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