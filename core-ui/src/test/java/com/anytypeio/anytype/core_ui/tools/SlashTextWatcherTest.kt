package com.anytypeio.anytype.core_ui.tools

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SlashTextWatcherTest {

    @Test
    fun `should fire start and text events`() {

        val events: MutableList<SlashTextWatcherState> = mutableListOf()

        val watcher = buildSlashWatcher { state -> events.add(state) }

        watcher.onTextChanged("/", 0, 0, 1)

        assertEquals(
            expected = listOf(
                SlashTextWatcherState.Start(start = 0),
                SlashTextWatcherState.Filter(text = "/")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply text events`() {

        val events: MutableList<SlashTextWatcherState> = mutableListOf()

        val watcher = buildSlashWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "start / end",
            start = 6,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /n end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /ne end",
            start = 8,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /new end",
            start = 9,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /new  end",
            start = 10,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /new p end",
            start = 11,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start /new pa end",
            start = 11,
            before = 1,
            count = 2
        )

        watcher.onTextChanged(
            s = "start /new pag end",
            start = 11,
            before = 2,
            count = 3
        )

        watcher.onTextChanged(
            s = "start /new page end",
            start = 14,
            before = 0,
            count = 1
        )

        assertEquals(
            expected = listOf(
                SlashTextWatcherState.Start(start = 6),
                SlashTextWatcherState.Filter(text = "/"),
                SlashTextWatcherState.Filter(text = "/n"),
                SlashTextWatcherState.Filter(text = "/ne"),
                SlashTextWatcherState.Filter(text = "/new"),
                SlashTextWatcherState.Filter(text = "/new "),
                SlashTextWatcherState.Filter(text = "/new p"),
                SlashTextWatcherState.Filter(text = "/new pa"),
                SlashTextWatcherState.Filter(text = "/new pag"),
                SlashTextWatcherState.Filter(text = "/new page")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and filter events`() {

        val events: MutableList<SlashTextWatcherState> = mutableListOf()

        val watcher = buildSlashWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "tshh/ ",
            start = 5,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "tshh/ t",
            start = 6,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "tshh/ tr",
            start = 6,
            before = 0,
            count = 2
        )

        watcher.onTextChanged(
            s = "tshh/ trd",
            start = 6,
            before = 0,
            count = 3
        )

        watcher.onTextChanged(
            s = "tshh/ trd/",
            start = 6,
            before = 0,
            count = 4
        )

        assertEquals(
            expected = listOf(
                SlashTextWatcherState.Start(start = 9),
                SlashTextWatcherState.Filter(text = "/")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply filter events`() {

        val events: MutableList<SlashTextWatcherState> = mutableListOf()

        val watcher = buildSlashWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "foobar end",
            start = 0,
            before = 0,
            count = 6
        )

        watcher.onTextChanged(
            s = "foobar/ end",
            start = 0,
            before = 0,
            count = 7
        )

        watcher.onTextChanged(
            s = "foobar/m end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "foobar/mo end",
            start = 7,
            before = 0,
            count = 2
        )

        watcher.onTextChanged(
            s = "foobar/mov end",
            start = 7,
            before = 0,
            count = 3
        )

        watcher.onTextChanged(
            s = "foobar/mo end",
            start = 7,
            before = 0,
            count = 2
        )

        watcher.onTextChanged(
            s = "foobar/m end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "foobar/ end",
            start = 7,
            before = 0,
            count = 0
        )

        watcher.onTextChanged(
            s = "foobar end",
            start = 6,
            before = 0,
            count = 0
        )

        assertEquals(
            expected = listOf(
                SlashTextWatcherState.Start(start = 6),
                SlashTextWatcherState.Filter(text = "/"),
                SlashTextWatcherState.Filter(text = "/m"),
                SlashTextWatcherState.Filter(text = "/mo"),
                SlashTextWatcherState.Filter(text = "/mov"),
                SlashTextWatcherState.Filter(text = "/mo"),
                SlashTextWatcherState.Filter(text = "/m"),
                SlashTextWatcherState.Filter(text = "/"),
                SlashTextWatcherState.Stop
            ),
            actual = events
        )
    }

    private fun buildSlashWatcher(
        onSlashEvent: (SlashTextWatcherState) -> Unit
    ): SlashTextWatcher {
        return SlashTextWatcher(
            onSlashEvent = onSlashEvent
        )
    }
}