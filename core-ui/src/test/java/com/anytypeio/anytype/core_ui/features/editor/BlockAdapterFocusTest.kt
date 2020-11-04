package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterFocusTest : BlockAdapterTestSetup() {

    @Test
    fun `should call back when paragraph view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false
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

        check(holder is Paragraph)

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

        val paragraph = BlockView.Text.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false
        )

        val focused = paragraph.copy(
            isFocused = true
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

        check(holder is Paragraph)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.FOCUS_CHANGED)
                )
            ),
            item = focused,
            onSelectionChanged = { _, _ -> },
            onTextChanged = {},
            clicked = {},
            onMentionEvent = {}
        )

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
}