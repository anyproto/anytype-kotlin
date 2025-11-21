package com.anytypeio.anytype.presentation.util

import com.anytypeio.anytype.core_models.SystemColor
import kotlin.test.assertEquals
import org.junit.Test

class SystemColorTest {

    @Test
    fun `should return expected system color`() {

        // Negative indices should fallback to YELLOW
        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = -1
            )
        )

        // Index 0 should return GRAY (new behavior)
        assertEquals(
            expected = SystemColor.GRAY,
            actual = SystemColor.color(
                idx = 0
            )
        )

        // Index 1 should return YELLOW
        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 1
            )
        )

        // Index 2 should return AMBER
        assertEquals(
            expected = SystemColor.AMBER,
            actual = SystemColor.color(
                idx = 2
            )
        )

        // Index 9 should return GREEN (last color)
        assertEquals(
            expected = SystemColor.GREEN,
            actual = SystemColor.color(
                idx = 9
            )
        )

        // Index 10 should wrap around to GRAY (10 % 10 = 0)
        assertEquals(
            expected = SystemColor.GRAY,
            actual = SystemColor.color(
                idx = 10
            )
        )

        // Index 11 should wrap around to YELLOW (11 % 10 = 1)
        assertEquals(
            expected = SystemColor.YELLOW,
            actual = SystemColor.color(
                idx = 11
            )
        )

        // Index 18 should return TEAL (18 % 10 = 8)
        assertEquals(
            expected = SystemColor.TEAL,
            actual = SystemColor.color(
                idx = 18
            )
        )

        // Index 19 should return GREEN (19 % 10 = 9)
        assertEquals(
            expected = SystemColor.GREEN,
            actual = SystemColor.color(
                idx = 19
            )
        )

        // Index 20 should wrap around to GRAY (20 % 10 = 0)
        assertEquals(
            expected = SystemColor.GRAY,
            actual = SystemColor.color(
                idx = 20
            )
        )
    }
}