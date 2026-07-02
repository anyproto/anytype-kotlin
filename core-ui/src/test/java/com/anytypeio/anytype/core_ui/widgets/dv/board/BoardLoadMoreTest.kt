package com.anytypeio.anytype.core_ui.widgets.dv.board

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [shouldLoadMore] — the per-column infinite-scroll trigger.
 */
class BoardLoadMoreTest {

    @Test
    fun `fires when near the end and more records exist`() {
        // 50 loaded of 120; last visible item index 49 of 51 total (50 cards + footer).
        assertTrue(shouldLoadMore(lastVisibleIndex = 49, totalItemsCount = 51, canPaginate = true, threshold = 2))
    }

    @Test
    fun `does not fire when not near the end`() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 10, totalItemsCount = 51, canPaginate = true, threshold = 2))
    }

    @Test
    fun `does not fire when the column is fully loaded`() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 49, totalItemsCount = 50, canPaginate = false, threshold = 2))
    }

    @Test
    fun `threshold widens the trigger zone`() {
        assertEquals(
            true,
            shouldLoadMore(lastVisibleIndex = 47, totalItemsCount = 50, canPaginate = true, threshold = 3)
        )
    }
}
