package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatReadVisibilityTest {
    @Test
    fun `ordinary messages must be fully visible including exact viewport edges`() {
        assertTrue(isMessageVisibleForReading(0, 100, 0, 600))
        assertTrue(isMessageVisibleForReading(500, 100, 0, 600))
        assertFalse(isMessageVisibleForReading(-1, 100, 0, 600))
        assertFalse(isMessageVisibleForReading(501, 100, 0, 600))
    }

    @Test
    fun `a final message taller than the screen is readable at the bottom`() {
        assertTrue(isMessageVisibleForReading(0, 900, 0, 600))
        assertTrue(isMessageVisibleForReading(-100, 900, 0, 600))
        assertFalse(isMessageVisibleForReading(300, 900, 0, 600))
        assertFalse(isMessageVisibleForReading(-400, 900, 0, 600))
    }

    @Test
    fun `visibility uses the viewport offsets including content padding`() {
        assertTrue(isMessageVisibleForReading(-20, 100, -20, 580))
        assertFalse(isMessageVisibleForReading(-21, 100, -20, 580))
        assertTrue(isMessageVisibleForReading(-20, 900, -20, 580))
    }

    @Test
    fun `unmeasured viewport or message is not readable`() {
        assertFalse(isMessageVisibleForReading(0, 100, 0, 0))
        assertFalse(isMessageVisibleForReading(0, 0, 0, 600))
    }

    @Test
    fun `layout must match current keys including date sections before reading`() {
        val keys = listOf("B", "A", "date-1")
        assertTrue(isChatReadLayoutCurrent(keys, 3, listOf(0 to "B", 1 to "A", 2 to "date-1")))
        assertFalse(isChatReadLayoutCurrent(keys, 2, listOf(0 to "A", 1 to "date-1")))
        assertFalse(isChatReadLayoutCurrent(keys, 3, listOf(0 to "A", 1 to "B")))
        assertFalse(isChatReadLayoutCurrent(keys, 3, listOf(2 to "date-2")))
    }

    @Test
    fun `same ids are reported again after a scroll that starts and finishes between snapshots`() = runTest {
        var scrolling by mutableStateOf(false)
        var generation by mutableIntStateOf(0)
        val reports = mutableListOf<ChatReadVisibility>()
        backgroundScope.launch {
            snapshotFlow {
                ChatReadVisibility(if (scrolling) null else "B" to "A", null, generation)
            }.collect { reports += it }
        }
        runCurrent()
        // The missing-target branch invalidates the session synchronously. Compose may
        // never observe scrolling=true, so the generation must restore the unchanged range.
        scrolling = true
        scrolling = false
        generation++
        Snapshot.sendApplyNotifications()
        runCurrent()
        assertEquals(listOf("B" to "A", "B" to "A"), reports.map { it.range })
    }

    @Test
    fun `pause and resume can restore the same viewport without a scroll`() = runTest {
        var resumed by mutableStateOf(true)
        var generation by mutableIntStateOf(0)
        val reports = mutableListOf<ChatReadVisibility>()
        backgroundScope.launch {
            snapshotFlow {
                ChatReadVisibility(if (resumed) "B" to "A" else null, null, generation)
            }.collect { reports += it }
        }
        runCurrent()
        resumed = false
        generation++
        Snapshot.sendApplyNotifications()
        runCurrent()
        resumed = true
        generation++
        Snapshot.sendApplyNotifications()
        runCurrent()
        assertEquals(listOf("B" to "A", null, "B" to "A"), reports.map { it.range })
    }
}
