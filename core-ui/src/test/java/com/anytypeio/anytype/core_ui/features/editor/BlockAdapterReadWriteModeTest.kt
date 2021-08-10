package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Checkbox
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterReadWriteModeTest : BlockAdapterTestSetup() {

    @Test
    fun `text-input click listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(block)

        val updated = listOf(block.copy(mode = BlockView.Mode.EDIT))

        val adapter = buildAdapter(
            views = views,
            onTextInputClicked = { trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
            )
        )

        holder.content.performClick()

        adapter.updateWithDiffUtil(items = updated)

        assertEquals(
            expected = 1,
            actual = trigger
        )

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        holder.content.performClick()

        assertEquals(
            expected = 2,
            actual = trigger
        )
    }

    @Test
    fun `endline-enter press listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(block)

        val updated = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT,
                cursor = block.text.length,
                isFocused = true
            )
        )

        val adapter = buildAdapter(
            views = views,
            onSplitLineEnterClicked = { _, _, _ -> trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED,
                    BlockViewDiffUtil.FOCUS_CHANGED,
                    BlockViewDiffUtil.CURSOR_CHANGED
                )
            )
        )

        adapter.updateWithDiffUtil(items = updated)

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        holder.content.onEditorAction(EditorInfo.IME_ACTION_GO)

        assertEquals(
            expected = 1,
            actual = trigger
        )
    }

    @Test
    fun `split-line-enter press listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            cursor = null
        )

        val views = listOf(block)

        val updated = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT,
                cursor = 5,
                isFocused = true
            )
        )

        val adapter = buildAdapter(
            views = views,
            onSplitLineEnterClicked = { _, _, _ -> trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED,
                    BlockViewDiffUtil.FOCUS_CHANGED,
                    BlockViewDiffUtil.CURSOR_CHANGED
                )
            )
        )

        adapter.updateWithDiffUtil(items = updated)

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        assertEquals(
            expected = 5,
            actual = holder.content.selectionStart
        )

        holder.content.onEditorAction(EditorInfo.IME_ACTION_GO)

        assertEquals(
            expected = 1,
            actual = trigger
        )
    }

    @Test
    fun `on-non-empty-block-backspace-press listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            cursor = null
        )

        val views = listOf(block)

        val updated = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT,
                cursor = 0,
                isFocused = true
            )
        )

        val adapter = buildAdapter(
            views = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED,
                    BlockViewDiffUtil.FOCUS_CHANGED,
                    BlockViewDiffUtil.CURSOR_CHANGED
                )
            )
        )

        adapter.updateWithDiffUtil(items = updated)

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        assertEquals(
            expected = 0,
            actual = holder.content.selectionStart
        )

        holder.content.dispatchKeyEvent(
            KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0)
        )

        assertEquals(
            expected = 1,
            actual = trigger
        )
    }

    @Test
    fun `on-empty-block-backspace-press listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.READ,
            text = "",
            id = MockDataFactory.randomUuid(),
            cursor = null
        )

        val views = listOf(block)

        val updated = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT,
                cursor = 0,
                isFocused = true
            )
        )

        val adapter = buildAdapter(
            views = views,
            onEmptyBlockBackspaceClicked = { trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED,
                    BlockViewDiffUtil.FOCUS_CHANGED,
                    BlockViewDiffUtil.CURSOR_CHANGED
                )
            )
        )

        adapter.updateWithDiffUtil(items = updated)

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        assertEquals(
            expected = 0,
            actual = holder.content.selectionStart
        )

        holder.content.dispatchKeyEvent(
            KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0)
        )

        assertEquals(
            expected = 1,
            actual = trigger
        )
    }

    @Test
    fun `checkbox-clicked listener should be enabled when switching from read to edit mode`() {

        // Setup

        var trigger = 0

        val block = BlockView.Text.Checkbox(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isChecked = false
        )

        val views = listOf(block)

        val updated = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT
            )
        )

        val adapter = buildAdapter(
            views = views,
            onCheckboxClicked = { trigger += 1 }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        check(holder is Checkbox)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        holder.checkbox.performClick()

        assertEquals(
            expected = 0,
            actual = trigger
        )

        val payloads: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.updateWithDiffUtil(items = updated)

        adapter.onBindViewHolder(holder, 0, payloads = payloads)

        holder.checkbox.performClick()

        assertEquals(
            expected = 1,
            actual = trigger
        )
    }
}