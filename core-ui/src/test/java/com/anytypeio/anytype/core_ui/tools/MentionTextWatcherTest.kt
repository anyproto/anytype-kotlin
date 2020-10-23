package com.anytypeio.anytype.core_ui.tools

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class MentionTextWatcherTest {

    @Test
    fun `should fire start and text events`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged("@", 0, 0, 1)

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 0),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply text events`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "start @ end",
            start = 6,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @n end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @ne end",
            start = 8,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @new end",
            start = 9,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @new  end",
            start = 10,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @new p end",
            start = 11,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @new pa end",
            start = 11,
            before = 1,
            count = 2
        )

        watcher.onTextChanged(
            s = "start @new pag end",
            start = 11,
            before = 2,
            count = 3
        )

        watcher.onTextChanged(
            s = "start @new page end",
            start = 14,
            before = 0,
            count = 1
        )

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 6),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@n"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@ne"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new "),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new p"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new pa"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new pag"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@new page")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply text events 2`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "start @ end",
            start = 6,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @m end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my end",
            start = 8,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my  end",
            start = 9,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my t end",
            start = 10,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my tr end",
            start = 10,
            before = 1,
            count = 2
        )

        watcher.onTextChanged(
            s = "start @my tra end",
            start = 10,
            before = 2,
            count = 3
        )

        watcher.onTextChanged(
            s = "start @my training end",
            start = 13,
            before = 0,
            count = 5
        )

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 6),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@m"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my "),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my t"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my tr"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my tra"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my training")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply text events 3`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged("start @ end", 6, 0, 1)
        watcher.onTextChanged("start @b end", 7, 0, 1)
        watcher.onTextChanged("start @bi end", 8, 0, 1)
        watcher.onTextChanged("start @big end", 9, 0, 1)
        watcher.onTextChanged("start @bigt end", 10, 0, 1)
        watcher.onTextChanged("start @bigti end", 11, 0, 1)
        watcher.onTextChanged("start @bigtit end", 12, 0, 1)
        watcher.onTextChanged("start @bigtitl end", 13, 0, 1)
        watcher.onTextChanged("start @bigtitle end", 14, 0, 1)
        watcher.onTextChanged("start @bigtitlet end", 15, 0, 1)
        watcher.onTextChanged("start @bigtitleto end", 16, 0, 1)
        watcher.onTextChanged("start @bigtitletod end", 17, 0, 1)
        watcher.onTextChanged("start @bigtitletode end", 18, 0, 1)
        watcher.onTextChanged("start @bigtitletodel end", 19, 0, 1)
        watcher.onTextChanged("start @bigtitletodele end", 20, 0, 1)
        watcher.onTextChanged("start @bigtitletodelet end", 21, 0, 1)
        watcher.onTextChanged("start @bigtitletodelete end", 22, 0, 1)
        watcher.onTextChanged("start @bi end", 9, 14, 0)

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 6),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@b"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bi"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@big"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigt"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigti"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtit"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitl"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitle"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitlet"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitleto"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletod"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletode"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletodel"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletodele"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletodelet"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigtitletodelete"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bi")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start and multiply text events 4`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged("start @ end", 6, 0, 1)
        watcher.onTextChanged("start @b end", 7, 0, 1)
        watcher.onTextChanged("start @bi end", 8, 0, 1)
        watcher.onTextChanged("start @big end", 9, 0, 1)
        watcher.onTextChanged("start @big endd", 11, 3, 4)
        watcher.onTextChanged("start @bigt endd", 10, 0, 1)

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 6),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@b"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bi"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@big"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@big"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@bigt")
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start, multiply text and stop events`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "start @ end",
            start = 6,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @m end",
            start = 7,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my end",
            start = 8,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my  end",
            start = 9,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my t end",
            start = 10,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "start @my tr end",
            start = 10,
            before = 1,
            count = 2
        )

        watcher.onTextChanged(
            s = "start @my training end",
            start = 0,
            before = 16,
            count = 22
        )

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 6),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@m"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my "),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my t"),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@my tr"),
                MentionTextWatcher.MentionTextWatcherState.Stop
            ),
            actual = events
        )
    }

    @Test
    fun `should fire start, text and stop events`() {

        val events: MutableList<MentionTextWatcher.MentionTextWatcherState> = mutableListOf()

        val watcher = buildMentionWatcher { state -> events.add(state) }

        watcher.onTextChanged(
            s = "@",
            start = 0,
            before = 0,
            count = 1
        )

        watcher.onTextChanged(
            s = "",
            start = 0,
            before = 1,
            count = 0
        )

        assertEquals(
            expected = listOf(
                MentionTextWatcher.MentionTextWatcherState.Start(start = 0),
                MentionTextWatcher.MentionTextWatcherState.Text(text = "@"),
                MentionTextWatcher.MentionTextWatcherState.Stop
            ),
            actual = events
        )
    }

    private fun buildMentionWatcher(
        onMentionEvent: (MentionTextWatcher.MentionTextWatcherState) -> Unit
    ): MentionTextWatcher {
        return MentionTextWatcher(
            onMentionEvent = onMentionEvent
        )
    }
}