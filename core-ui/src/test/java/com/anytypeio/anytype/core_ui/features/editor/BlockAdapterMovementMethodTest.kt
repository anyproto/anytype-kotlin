package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.text.method.ArrowKeyMovementMethod
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterMovementMethodTest : BlockAdapterTestSetup() {


    @Test
    fun `should be default arrow movement method when text is empty`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod.getInstance(),
            actual = mm
        )
    }

    @Test
    fun `should be default arrow movement method when text has no links`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text without links",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod.getInstance(),
            actual = mm
        )
    }

    @Test
    fun `should be better link movement method when text has links`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "ya.ru"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )
    }

    @Test
    fun `should be better link movement method when text has mention`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "4673675627647237"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )
    }

    @Test
    fun `should be better link movement method when adding link markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "ya.ru"
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be better link movement method when adding mention markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "asudhguyagdhjashj"
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be better link movement method when adding text with link markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "test",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "ya.ru"
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be better link movement method when adding text with mention markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "test",
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "ya.ru"
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be default movement method when remove link markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "ya.ru"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            marks = listOf()
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be default movement method when remove mention markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "4234213rfw"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            marks = listOf()
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be default movement method when remove text with link markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "ya.ru"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.BOLD
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be default movement method when remove text with mention markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "foifhunsjkdnfjkasnjku"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.BOLD
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be better link movement method when update text with mention markup`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.MENTION,
                    param = "foifhunsjkdnfjkasnjku"
                )
            ),
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

        val mm = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 15,
                    type = Markup.Type.LINK,
                    param = "anytype.io"
                )
            )
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BlockViewDiffUtil.TEXT_CHANGED),
                )
            ),
            item = updated,
            clicked = {},
            onSelectionChanged = { _, _ -> },
            onTextChanged = {}
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = BetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }
}