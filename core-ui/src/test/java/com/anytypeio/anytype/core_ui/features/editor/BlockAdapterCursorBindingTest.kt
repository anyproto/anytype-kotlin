package com.anytypeio.anytype.core_ui.features.editor

import android.content.Context
import android.os.Build
import android.text.Editable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.features.editor.holders.text.*
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_ONE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_THREE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.O])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterCursorBindingTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val clipboardInterceptor: ClipboardInterceptor = object : ClipboardInterceptor {
        override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
        override fun onUrlPasted(url: Url) {}
    }

    @Test
    fun `should set paragraph cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Paragraph(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set header-one cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Header.One(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_ONE)

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

        val block = BlockView.Text.Header.Two(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_TWO)

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

        val block = BlockView.Text.Header.Three(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_THREE)

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

        val block = BlockView.Text.Highlight(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set checkbox cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Checkbox(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Checkbox)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set bulleted-item block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Bulleted(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_BULLET)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Bulleted)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set numbered block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Numbered(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_NUMBERED)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Numbered)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    @Test
    fun `should set toggle block cursor on first binding`() {

        // Setup

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Toggle(
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_TOGGLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Toggle)

        // Testing

        assertEquals(expected = block.text, actual = holder.content.text.toString())
        assertEquals(expected = block.cursor, actual = holder.content.selectionStart)
        assertEquals(expected = block.cursor, actual = holder.content.selectionEnd)
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            restore = LinkedList(),
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _, _ -> },
            onSplitDescription = { _, _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = {},
            onPageIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onTextBlockTextChanged = {},
            onTitleBlockTextChanged = {_, _ -> },
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {},
            onBackPressedCallback = { false },
            onCoverClicked = {},
            onSlashEvent = {},
            onKeyPressedEvent = {},
            onDragAndDropTrigger = { _, _ -> false },
            onDescriptionChanged = {},
            onTitleCheckboxClicked = {},
            onDragListener = EditorDragAndDropListener(
                onDragEnded = { _, _ -> },
                onDragExited = {},
                onDragLocation = { _, _ -> },
                onDrop = { _, _ -> },
                onDragStart = {}
            ),
            dragAndDropSelector = DragAndDropAdapterDelegate(),
            lifecycle = object : Lifecycle() {
                override fun addObserver(observer: LifecycleObserver) {}
                override fun removeObserver(observer: LifecycleObserver) {}
                override fun getCurrentState() = State.DESTROYED
            }
        )
    }
}