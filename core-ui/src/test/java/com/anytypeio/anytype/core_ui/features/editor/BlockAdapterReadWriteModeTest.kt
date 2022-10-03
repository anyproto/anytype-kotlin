package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Checkbox
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.tools.CustomBetterLinkMovementMethod
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterReadWriteModeTest : BlockAdapterTestSetup() {
    /**
     * Scenario:
     * 1. Text block[bold, link spans] EDIT mode
     * 2. Text block[bold, link spans] READ mode (enter selected mode for example)
     * 3. Text block[bold, link spans] EDIT mode (exit selected mode for example)
     */
    @Test
    fun `block text should have custom better link movement method and link span after read-write payload`() {

        val blockText = MockDataFactory.randomString()
        val linkParam = MockDataFactory.randomString()

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.EDIT,
            text = blockText,
            id = MockDataFactory.randomUuid(),
            marks = listOf(
                Markup.Mark.Link(
                    from = 0,
                    to = blockText.length,
                    param = linkParam
                ),
                Markup.Mark.Bold(
                    from = 0,
                    to = blockText.length
                )
            )
        )

        val views = listOf(block)

        val adapter = buildAdapter(
            views = views
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val urlSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Url }
            assertNotNull(urlSpan)
            assertEquals(expected = linkParam, actual = (urlSpan as Span.Url).url)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }

        val updated1 = listOf(
            block.copy(
                mode = BlockView.Mode.READ
            )
        )

        adapter.updateWithDiffUtil(updated1)

        val payload1: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload1)

        //READ MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = null
            assertEquals(expected = expectedMm, actual = actualMm)

            val urlSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Url }
            assertNotNull(urlSpan)
            assertEquals(expected = linkParam, actual = (urlSpan as Span.Url).url)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertFalse(content.isTextSelectable)
        }

        val updated2 = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT
            )
        )

        adapter.updateWithDiffUtil(updated2)

        val payload2: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload2)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val urlSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Url }
            assertNotNull(urlSpan)
            assertEquals(expected = linkParam, actual = (urlSpan as Span.Url).url)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }
    }

    /**
     * Scenario:
     * 1. Text block[bold, object spans] EDIT mode
     * 2. Text block[bold, object spans] READ mode (enter selected mode for example)
     * 3. Text block[bold, object spans] EDIT mode (exit selected mode for example)
     */
    @Test
    fun `block text should have custom better link movement method and object span after read-write payload`() {

        val blockText = MockDataFactory.randomString()
        val objectParam = MockDataFactory.randomString()

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.EDIT,
            text = blockText,
            id = MockDataFactory.randomUuid(),
            marks = listOf(
                Markup.Mark.Object(
                    from = 0,
                    to = blockText.length,
                    param = objectParam,
                    isArchived = false
                ),
                Markup.Mark.Bold(
                    from = 0,
                    to = blockText.length
                )
            )
        )

        val views = listOf(block)

        val adapter = buildAdapter(
            views = views
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = objectParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }

        val updated1 = listOf(
            block.copy(
                mode = BlockView.Mode.READ
            )
        )

        adapter.updateWithDiffUtil(updated1)

        val payload1: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload1)

        //READ MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = null
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = objectParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertFalse(content.isTextSelectable)
        }

        val updated2 = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT
            )
        )

        adapter.updateWithDiffUtil(updated2)

        val payload2: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload2)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = objectParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }
    }

    /**
     * Scenario:
     * 1. Text block[bold, base mention spans] EDIT mode
     * 2. Text block[bold, base mention spans] READ mode (enter selected mode for example)
     * 3. Text block[bold, base mention spans] EDIT mode (exit selected mode for example)
     */
    @Test
    fun `block text should have custom better link movement method and base mention span after read-write payload`() {

        val blockText = MockDataFactory.randomString()
        val mentionParam = MockDataFactory.randomString()

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.EDIT,
            text = blockText,
            id = MockDataFactory.randomUuid(),
            marks = listOf(
                Markup.Mark.Mention.Base(
                    from = 0,
                    to = blockText.length,
                    param = mentionParam,
                    isArchived = false
                ),
                Markup.Mark.Bold(
                    from = 0,
                    to = blockText.length
                )
            )
        )

        val views = listOf(block)

        val adapter = buildAdapter(
            views = views
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = mentionParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }

        val updated1 = listOf(
            block.copy(
                mode = BlockView.Mode.READ
            )
        )

        adapter.updateWithDiffUtil(updated1)

        val payload1: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload1)

        //READ MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = null
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = mentionParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertFalse(content.isTextSelectable)
        }

        val updated2 = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT
            )
        )

        adapter.updateWithDiffUtil(updated2)

        val payload2: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload2)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val objectLinkSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.ObjectLink }
            assertNotNull(objectLinkSpan)
            assertEquals(expected = mentionParam, actual = (objectLinkSpan as Span.ObjectLink).link)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }
    }

    /**
     * Scenario:
     * 1. Text block[bold, emoji mention spans] EDIT mode
     * 2. Text block[bold, emoji mention spans] READ mode (enter selected mode for example)
     * 3. Text block[bold, emoji mention spans] EDIT mode (exit selected mode for example)
     */
    @Test
    fun `block text should have custom better link movement method and emoji mention span after read-write payload`() {

        val blockText = MockDataFactory.randomString()
        val mentionParam = MockDataFactory.randomString()
        val emoji = MockDataFactory.randomString()

        val block = BlockView.Text.Paragraph(
            mode = BlockView.Mode.EDIT,
            text = blockText,
            id = MockDataFactory.randomUuid(),
            marks = listOf(
                Markup.Mark.Mention.WithEmoji(
                    from = 0,
                    to = blockText.length,
                    param = mentionParam,
                    isArchived = false,
                    emoji = emoji
                ),
                Markup.Mark.Bold(
                    from = 0,
                    to = blockText.length
                )
            )
        )

        val views = listOf(block)

        val adapter = buildAdapter(
            views = views
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val mentionSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is MentionSpan }
            assertNotNull(mentionSpan)
            assertEquals(expected = mentionParam, actual = (mentionSpan as MentionSpan).param)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }

        val updated1 = listOf(
            block.copy(
                mode = BlockView.Mode.READ
            )
        )

        adapter.updateWithDiffUtil(updated1)

        val payload1: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload1)

        //READ MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = null
            assertEquals(expected = expectedMm, actual = actualMm)

            val mentionSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is MentionSpan }
            assertNotNull(mentionSpan)
            assertEquals(expected = mentionParam, actual = (mentionSpan as MentionSpan).param)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertFalse(content.isTextSelectable)
        }

        val updated2 = listOf(
            block.copy(
                mode = BlockView.Mode.EDIT
            )
        )

        adapter.updateWithDiffUtil(updated2)

        val payload2: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.SELECTION_CHANGED,
                    BlockViewDiffUtil.READ_WRITE_MODE_CHANGED
                )
            )
        )

        adapter.onBindViewHolder(holder, 0, payload2)

        //EDIT MODE
        with(holder) {

            val actualMm = content.movementMethod
            val expectedMm = CustomBetterLinkMovementMethod
            assertEquals(expected = expectedMm, actual = actualMm)

            val mentionSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is MentionSpan }
            assertNotNull(mentionSpan)
            assertEquals(expected = mentionParam, actual = (mentionSpan as MentionSpan).param)

            val boldSpan =
                content.text?.getSpans(0, blockText.length, Span::class.java)
                    ?.first { it is Span.Bold }
            assertNotNull(boldSpan)

            val actualText = content.text.toString()
            assertEquals(expected = blockText, actual = actualText)
            assertTrue(content.isTextSelectable)
        }
    }
}