package com.anytypeio.anytype.ui.main

import com.anytypeio.anytype.R
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ContentColumnWidthTest {

    private val capped = 1800

    @Test
    fun `should cap the editor`() {
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.pageScreen,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should not cap the set screen`() {
        assertEquals(
            NO_MAX_WIDTH,
            contentColumnMaxWidth(
                destinationId = R.id.objectSetScreen,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should not cap the type screen`() {
        assertEquals(
            NO_MAX_WIDTH,
            contentColumnMaxWidth(
                destinationId = R.id.objectTypeScreen,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should cap every other screen`() {
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.chatScreen,
                cappedWidthPx = capped
            )
        )
        assertEquals(
            capped,
            contentColumnMaxWidth(
                destinationId = R.id.vaultScreen,
                cappedWidthPx = capped
            )
        )
    }

    @Test
    fun `should show the wallpaper on the widgets screen`() {
        assertTrue(showsWallpaper(R.id.homeScreen))
    }

    @Test
    fun `should show the wallpaper on the collection screen`() {
        assertTrue(showsWallpaper(R.id.homeScreenWidgets))
    }

    @Test
    fun `should show the wallpaper on the vault`() {
        assertTrue(showsWallpaper(R.id.vaultScreen))
    }

    @Test
    fun `should paint the plain backdrop on the editor`() {
        assertFalse(showsWallpaper(R.id.pageScreen))
    }

    @Test
    fun `should paint the plain backdrop on the set screen`() {
        assertFalse(showsWallpaper(R.id.objectSetScreen))
    }

    @Test
    fun `should show the wallpaper on the chat`() {
        assertTrue(showsWallpaper(R.id.chatScreen))
    }
}
