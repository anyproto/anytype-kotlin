package com.anytypeio.anytype.core_ui.extensions

import kotlin.test.assertEquals
import org.junit.Test

class HalfRowWidthTest {

    @Test
    fun `should take half of the row minus the deduction`() {
        assertEquals(284, halfRowWidthPx(availableWidthPx = 600, deductPx = 16))
    }

    @Test
    fun `should follow a wider row`() {
        assertEquals(884, halfRowWidthPx(availableWidthPx = 1800, deductPx = 16))
    }

    @Test
    fun `should never return a negative width`() {
        assertEquals(0, halfRowWidthPx(availableWidthPx = 10, deductPx = 16))
    }

    @Test
    fun `should keep an unbounded row unbounded`() {
        assertEquals(
            Int.MAX_VALUE,
            halfRowWidthPx(availableWidthPx = Int.MAX_VALUE, deductPx = 16)
        )
    }
}
