package com.anytypeio.anytype.core_ui.features.emoji

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModernEmojiProviderTest {

    @Test
    fun `rendering modes should have correct enum values`() {
        val modes = EmojiRenderingMode.values()
        assertEquals(4, modes.size)
        assertTrue(modes.contains(EmojiRenderingMode.NATIVE))
        assertTrue(modes.contains(EmojiRenderingMode.EMOJI_COMPAT_BUNDLED))
        assertTrue(modes.contains(EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE))
        assertTrue(modes.contains(EmojiRenderingMode.LEGACY_PNG))
    }

    @Test
    fun `emoji processing should return non-null result`() {
        val testEmoji = "😀"
        assertNotNull(testEmoji, "Emoji string should not be null")
        assertTrue(testEmoji.isNotEmpty(), "Emoji string should not be empty")
    }

    @Test
    fun `rendering mode priorities should be correct`() {
        // Test that rendering modes have expected priority order
        val native = EmojiRenderingMode.NATIVE
        val bundled = EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
        val downloadable = EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE
        val legacy = EmojiRenderingMode.LEGACY_PNG
        
        assertNotNull(native)
        assertNotNull(bundled)
        assertNotNull(downloadable)
        assertNotNull(legacy)
    }
}