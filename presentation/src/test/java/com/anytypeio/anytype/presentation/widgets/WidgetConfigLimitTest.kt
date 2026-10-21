package com.anytypeio.anytype.presentation.widgets

import kotlin.test.assertEquals
import org.junit.Test

class WidgetConfigLimitTest {

    @Test
    fun `compact list keeps a limit set by another client`() {
        assertEquals(30, WidgetConfig.resolveListWidgetLimit(isCompact = true, limit = 30))
        assertEquals(50, WidgetConfig.resolveListWidgetLimit(isCompact = true, limit = 50))
    }

    @Test
    fun `gallery and list keep a limit set by another client`() {
        assertEquals(
            50,
            WidgetConfig.resolveListWidgetLimit(isCompact = false, isGallery = true, limit = 50)
        )
        assertEquals(30, WidgetConfig.resolveListWidgetLimit(isCompact = false, limit = 30))
    }

    @Test
    fun `tree keeps a limit set by another client`() {
        assertEquals(30, WidgetConfig.resolveTreeWidgetLimit(30))
        assertEquals(50, WidgetConfig.resolveTreeWidgetLimit(50))
    }

    @Test
    fun `missing or bad limit falls back to the default`() {
        assertEquals(
            WidgetConfig.DEFAULT_COMPACT_LIST_LIMIT,
            WidgetConfig.resolveListWidgetLimit(isCompact = true, limit = WidgetConfig.NO_LIMIT)
        )
        assertEquals(
            WidgetConfig.DEFAULT_LIST_LIMIT,
            WidgetConfig.resolveListWidgetLimit(isCompact = false, limit = -1)
        )
        assertEquals(
            WidgetConfig.DEFAULT_TREE_LIMIT,
            WidgetConfig.resolveTreeWidgetLimit(WidgetConfig.NO_LIMIT)
        )
    }

    @Test
    fun `limit above the maximum falls back to the maximum`() {
        assertEquals(
            WidgetConfig.MAX_LIMIT,
            WidgetConfig.resolveListWidgetLimit(isCompact = true, limit = 10_000)
        )
        assertEquals(
            WidgetConfig.MAX_LIMIT,
            WidgetConfig.resolveListWidgetLimit(isCompact = false, limit = 51)
        )
        assertEquals(WidgetConfig.MAX_LIMIT, WidgetConfig.resolveTreeWidgetLimit(10_000))
    }
}
