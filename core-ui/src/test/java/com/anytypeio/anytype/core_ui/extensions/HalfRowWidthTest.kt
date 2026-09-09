package com.anytypeio.anytype.core_ui.extensions

import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import org.junit.Test

class HalfRowWidthTest {

    @Test
    fun `should take half of the row minus the gap`() {
        assertEquals(284.dp, halfRowWidth(rowWidth = 600.dp))
    }

    @Test
    fun `should follow a wider row`() {
        assertEquals(884.dp, halfRowWidth(rowWidth = 1800.dp))
    }

    @Test
    fun `should never return a negative width`() {
        assertEquals(0.dp, halfRowWidth(rowWidth = 10.dp))
    }
}
