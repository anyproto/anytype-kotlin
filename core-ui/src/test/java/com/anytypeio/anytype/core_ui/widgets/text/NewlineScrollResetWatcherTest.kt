package com.anytypeio.anytype.core_ui.widgets.text

import android.os.Build
import android.text.SpannableStringBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class NewlineScrollResetWatcherTest {

    private var scrollResetCalled = false
    private val watcher = NewlineScrollResetWatcher { scrollResetCalled = true }

    @Test
    fun `should reset scroll when newline is inserted`() {
        scrollResetCalled = false
        simulateTextChange("abc", "\n", 3)
        assert(scrollResetCalled) { "Expected scroll reset when newline inserted" }
    }

    @Test
    fun `should not reset scroll when text without newline is inserted`() {
        scrollResetCalled = false
        simulateTextChange("abc", "def", 3)
        assert(!scrollResetCalled) { "Expected no scroll reset when normal text inserted" }
    }

    @Test
    fun `should not reset scroll when text is deleted`() {
        scrollResetCalled = false
        // Simulate deletion: count=0, before=3 (3 chars deleted)
        val text = SpannableStringBuilder("abc")
        watcher.onTextChanged(text, 0, 3, 0)
        watcher.afterTextChanged(text)
        assert(!scrollResetCalled) { "Expected no scroll reset when text is deleted" }
    }

    @Test
    fun `should reset scroll when multiple newlines are inserted`() {
        scrollResetCalled = false
        simulateTextChange("abc", "\n\n\n", 3)
        assert(scrollResetCalled) { "Expected scroll reset when multiple newlines inserted" }
    }

    @Test
    fun `should reset scroll when newline is part of inserted text`() {
        scrollResetCalled = false
        simulateTextChange("abc", "line1\nline2", 3)
        assert(scrollResetCalled) { "Expected scroll reset when newline is part of inserted text" }
    }

    @Test
    fun `should not reset scroll when empty string is inserted`() {
        scrollResetCalled = false
        simulateTextChange("abc", "", 3)
        assert(!scrollResetCalled) { "Expected no scroll reset for empty insertion" }
    }

    @Test
    fun `should handle null CharSequence gracefully`() {
        scrollResetCalled = false
        watcher.onTextChanged(null, 0, 0, 1)
        watcher.afterTextChanged(null)
        assert(!scrollResetCalled) { "Expected no scroll reset for null CharSequence" }
    }

    @Test
    fun `should handle out of bounds gracefully`() {
        scrollResetCalled = false
        val text = SpannableStringBuilder("ab")
        // start + count > length
        watcher.onTextChanged(text, 1, 0, 10)
        watcher.afterTextChanged(text)
        assert(!scrollResetCalled) { "Expected no scroll reset for out of bounds" }
    }

    private fun simulateTextChange(before: String, inserted: String, insertAt: Int) {
        val after = before.substring(0, insertAt) + inserted + before.substring(insertAt)
        val text = SpannableStringBuilder(after)
        watcher.onTextChanged(text, insertAt, 0, inserted.length)
        watcher.afterTextChanged(text)
    }
}
