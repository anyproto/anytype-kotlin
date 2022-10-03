package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.text.method.ArrowKeyMovementMethod
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.tools.CustomBetterLinkMovementMethod
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.test_utils.MockDataFactory
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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

        val holder = adapter.onCreateViewHolder(recycler, 
            HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "4673675627647237",
                    isArchived = false
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "asudhguyagdhjashj",
                    isArchived = false
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
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "ya.ru",
                    isArchived = false
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
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "4234213rfw",
                    isArchived = false
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
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
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark.Bold(
                    from = 10,
                    to = 15
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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "foifhunsjkdnfjkasnjku",
                    isArchived = false
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark.Bold(
                    from = 10,
                    to = 15
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
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 15,
                    param = "foifhunsjkdnfjkasnjku",
                    isArchived = false
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // Testing

        val mm = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = mm::class.java
        )

        val updated = paragraph.copy(
            text = "This is new bold text",
            marks = listOf(
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
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
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `should be default movement method when text has no marks`() {
        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Text.Paragraph(
            text = "text with links",
            marks = listOf(
                Markup.Mark.Link(
                    from = 10,
                    to = 15,
                    param = "www.anytype.io"
                )
            ),
            id = MockDataFactory.randomUuid(),
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

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Paragraph)

        // TESTING

        val paragraphUpdated = paragraph.copy(
            text = "",
            marks = listOf(),
            isFocused = false
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(
                        BlockViewDiffUtil.TEXT_CHANGED,
                        BlockViewDiffUtil.MARKUP_CHANGED,
                        BlockViewDiffUtil.FOCUS_CHANGED
                    ),
                )
            ),
            item = paragraphUpdated,
            clicked = {},
        )

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }
}