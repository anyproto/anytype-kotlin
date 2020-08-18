package com.agileburo.anytype.core_ui.features.editor

import android.content.Context
import android.os.Build
import android.text.Editable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.MockDataFactory
import com.agileburo.anytype.core_ui.features.editor.holders.HeaderOne
import com.agileburo.anytype.core_ui.features.editor.holders.HeaderThree
import com.agileburo.anytype.core_ui.features.editor.holders.HeaderTwo
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.O])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterCursorBindingTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val clipboardInterceptor: ClipboardInterceptor = mock()

    @Test
    fun `should set paragraph cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Paragraph(
            text = text,
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set header-one cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.HeaderOne(
            text = text,
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderOne)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set header-two cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.HeaderTwo(
            text = text,
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderTwo)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set header-three cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.HeaderThree(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is HeaderThree)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set highlight cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Highlight(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            indent = 0,
            cursor = 3,
            color = null,
            backgroundColor = null
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Highlight)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set checkbox cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Checkbox(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            indent = 0,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Checkbox)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set bulleted-item block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Bulleted(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            indent = 0,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BULLET)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Bulleted)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set numbered block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Numbered(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            indent = 0,
            cursor = 3,
            number = 1
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Numbered)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set toggle block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Toggle(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = true,
            indent = 0,
            cursor = 3
        )

        val views = listOf(block)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Toggle)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onEndLineEnterTitleClicked: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _, _ -> },
            onEndLineEnterClicked = { _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onFooterClicked = {},
            onTextInputClicked = {},
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onParagraphTextChanged = {},
            onTitleTextChanged = onTitleTextChanged,
            onEndLineEnterTitleClicked = onEndLineEnterTitleClicked,
            onMarkupActionClicked = { _, _ -> },
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {}
        )
    }
}