package com.agileburo.anytype.core_ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.BACKGROUND_COLOR_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.FOCUS_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_COLOR_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.FOCUS_TIMEOUT_MILLIS
import com.agileburo.anytype.core_utils.ext.hexColorCode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should return transparent hex code when int color value is zero`() {

        val transparentColor = 0

        val actual = transparentColor.hexColorCode()

        assertEquals(
            expected = "#00000000",
            actual = actual
        )
    }

    @Test
    fun `should create paragraph view holder`() {

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        assertEquals(
            expected = BlockViewHolder.Paragraph::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        val text = holder.content.text.toString()

        assertEquals(
            expected = paragraph.text,
            actual = text
        )
    }

    @Test
    fun `should set text color for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = Color.RED.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        val color = holder.content.currentTextColor

        assertEquals(
            expected = Color.RED,
            actual = color
        )
    }

    @Test
    fun `should update paragraph holder with new text`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should update paragraph background color`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            backgroundColor = Color.RED.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BACKGROUND_COLOR_CHANGED)
                )
            )
        )

        assertEquals(
            expected = Color.parseColor(updated.backgroundColor),
            actual = (holder.root.background as ColorDrawable).color
        )
    }

    @Test
    fun `should update paragraph holder with new text color`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = Color.RED.hexColorCode()
        )

        val updated = paragraph.copy(
            color = Color.BLUE.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = Color.RED,
            actual = holder.content.currentTextColor
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_COLOR_CHANGED)
                )
            )
        )

        assertEquals(
            expected = Color.BLUE,
            actual = holder.content.currentTextColor
        )
    }

    @Test
    fun `should request paragraph focus after delay of 60 ms`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = true
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should call back when paragraph view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        holder.content.requestFocus()

        assertEquals(
            expected = listOf(
                Pair(paragraph.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should request paragraph focus if payload changes contain focus-changed event`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val focused = paragraph.copy(
            focused = true
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            ),
            item = focused
        )

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )

        assertEquals(
            expected = listOf(
                Pair(paragraph.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to paragraph holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when updating paragraph text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            ),
            item = updated
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should create title view holder`() {

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        assertEquals(
            expected = BlockViewHolder.Title::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for title holder`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        val text = holder.content.text.toString()

        assertEquals(
            expected = title.text,
            actual = text
        )
    }

    @Test
    fun `should update title holder with new text`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = title.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not request title focus after delay of 60 ms`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should call back when title view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        holder.content.requestFocus()

        assertEquals(
            expected = listOf(
                Pair(title.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to title holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should preserve cursor position after updating paragraph text`() {

        // Setup

        val title = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = title.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        val cursorBeforeUpdate = holder.content.selectionEnd

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        val cursorAfterUpdate = holder.content.selectionEnd

        assertEquals(
            expected = cursorBeforeUpdate,
            actual = cursorAfterUpdate
        )
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = {},
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _ -> },
            onEndLineEnterClicked = { _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onFooterClicked = {},
            onPageClicked = {},
            onTextInputClicked = {}
        )
    }
}