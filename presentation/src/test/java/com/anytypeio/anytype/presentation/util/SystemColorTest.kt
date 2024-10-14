package com.anytypeio.anytype.presentation.util

import com.anytypeio.anytype.core_models.SystemColor
import kotlin.test.assertEquals
import org.junit.Test

class SystemColorTest {

    @Test
    fun `should return expected system color`() {

        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = -1
            )
        )

        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 0
            )
        )

        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 1
            )
        )

        assertEquals(
            expected = SystemColor.AMBER,
            actual = SystemColor.color(
                idx = 2
            )
        )


        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 10
            )
        )

        assertEquals(
            expected = SystemColor.AMBER,
            actual = SystemColor.color(
                idx = 11
            )
        )

        assertEquals(
            expected = SystemColor.GREEN,
            actual = SystemColor.color(
                idx = 18
            )
        )

        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 19
            )
        )
    }
}