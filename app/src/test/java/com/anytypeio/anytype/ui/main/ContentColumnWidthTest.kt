package com.anytypeio.anytype.ui.main

import com.anytypeio.anytype.R
import kotlin.test.assertEquals
import org.junit.Test

class ContentColumnWidthTest {

    private val capped = 1800

    @Test
    fun `should not cap the editor on a phone`() {
        assertEquals(
            NO_MAX_WIDTH,
            contentColumnMaxWidth(
                destinationId = R.id.pageScreen,
                isTablet = false,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should not cap the set screen on a phone`() {
        assertEquals(
            NO_MAX_WIDTH,
            contentColumnMaxWidth(
                destinationId = R.id.objectSetScreen,
                isTablet = false,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should not cap the type screen on a phone`() {
        assertEquals(
            NO_MAX_WIDTH,
            contentColumnMaxWidth(
                destinationId = R.id.objectTypeScreen,
                isTablet = false,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should cap the editor on a tablet`() {
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.pageScreen,
                isTablet = true,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should cap every other screen on a phone`() {
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.chatScreen,
                isTablet = false,
                cappedWidthPx = capped
            )
        )
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.vaultScreen,
                isTablet = false,
                cappedWidthPx = capped
            )
        )
    }
}
