package com.anytypeio.anytype.core_ui

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderOne
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderThree
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderTwo
import com.anytypeio.anytype.core_ui.features.page.BlockAdapter
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class HeaderBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val clipboardInterceptor: ClipboardInterceptor = mock()

    @Test
    fun `should be italic markup in header one`() {
        val headerOne = BlockView.Text.Header.One(
            text = "Test header one string with spans",
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            marks = listOf(Markup.Mark(type = Markup.Type.ITALIC, from = 3, to = 10)),
            indent = 0
        )

        val views = listOf(headerOne)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderOne)

        // Testing

        val spannableText = holder.content.text as Spannable

        val spans = spannableText.getSpans(0, spannableText.length, Any::class.java)

        assertNotEquals(illegal = 0, actual = spans.size)
        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        var start = -1
        var end = -1

        for (span in spans) {
            if (span is Span.Italic) {
                start = spannableText.getSpanStart(span)
                end = spannableText.getSpanEnd(span)
            }
        }

        assertEquals(expected = 3, actual = start)
        assertEquals(expected = 10, actual = end)
    }

    @Test
    fun `should be italic markup in header two`() {
        val headerTwo = BlockView.Text.Header.Two(
            text = "Test header two string with spans",
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            marks = listOf(Markup.Mark(type = Markup.Type.ITALIC, from = 5, to = 13)),
            indent = 0
        )

        val views = listOf(headerTwo)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderTwo)

        // Testing

        val spannableText = holder.content.text as Spannable

        val spans = spannableText.getSpans(0, spannableText.length, Any::class.java)

        assertNotEquals(illegal = 0, actual = spans.size)
        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        var start = -1
        var end = -1

        for (span in spans) {
            if (span is Span.Italic) {
                start = spannableText.getSpanStart(span)
                end = spannableText.getSpanEnd(span)
            }
        }

        assertEquals(expected = 5, actual = start)
        assertEquals(expected = 13, actual = end)
    }

    @Test
    fun `should be italic markup in header three`() {
        val headerThree = BlockView.Text.Header.Three(
            text = "Test header three string with spans",
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            marks = listOf(Markup.Mark(type = Markup.Type.ITALIC, from = 7, to = 27)),
            indent = 0
        )

        val views = listOf(headerThree)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderThree)

        // Testing

        val spannableText = holder.content.text as Spannable

        val spans = spannableText.getSpans(0, spannableText.length, Any::class.java)

        assertNotEquals(illegal = 0, actual = spans.size)
        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        var start = -1
        var end = -1

        for (span in spans) {
            if (span is Span.Italic) {
                start = spannableText.getSpanStart(span)
                end = spannableText.getSpanEnd(span)
            }
        }

        assertEquals(expected = 7, actual = start)
        assertEquals(expected = 27, actual = end)
    }

    @Test
    fun `should not trigger on-text-changed event when updating header one text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val headerOne = BlockView.Text.Header.One(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = headerOne.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(headerOne)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderOne)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED)
                )
            ),
            item = updated,
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when updating header two text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val headerTwo = BlockView.Text.Header.Two(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            indent = 0
        )

        val updated = headerTwo.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(headerTwo)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderTwo)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED)
                )
            ),
            item = updated,
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when updating header three text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val headerThree = BlockView.Text.Header.Three(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            indent = 0
        )

        val updated = headerThree.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(headerThree)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderThree)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED)
                )
            ),
            item = updated,
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {}
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit = { _, _, _ ->},
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = onSplitLineEnterClicked,
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = {},
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onTextBlockTextChanged = {},
            onTitleBlockTextChanged = {},
            onContextMenuStyleClick = {},
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {},
            onBackPressedCallback = { false }
        )
    }
}