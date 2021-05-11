package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.text.method.ArrowKeyMovementMethod
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.tools.CustomBetterLinkMovementMethod
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class TextBlockSetTextTest : BlockAdapterTestSetup() {

    @Test
    fun `set empty text, empty marks`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "",
            marks = listOf(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, empty marks`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text",
            marks = listOf(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks without links or mentions`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text",
            marks = listOf(
                Markup.Mark(
                    from = 0,
                    to = 4,
                    type = Markup.Type.BOLD
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks with link`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text with link",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 14,
                    type = Markup.Type.LINK,
                    param = "link"
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks with mention`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text with mention",
            marks = listOf(
                Markup.Mark(
                    from = 10,
                    to = 14,
                    type = Markup.Type.MENTION,
                    param = "mention"
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }
}