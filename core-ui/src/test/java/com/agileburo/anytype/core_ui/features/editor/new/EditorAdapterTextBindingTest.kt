package com.agileburo.anytype.core_ui.features.editor.new

import android.graphics.drawable.ColorDrawable
import com.agileburo.anytype.core_ui.MockDataFactory
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.holders.ParagraphViewHolder
import org.junit.Test
import kotlin.test.assertEquals

//class EditorAdapterTextBindingTest : EditorAdapterTestSetup() {

//    @Test
//    fun `should set text for paragraph holder`() {
//
//        // Setup
//
//        val paragraph = BlockView.Paragraph(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid()
//        )
//
//        val views = listOf(paragraph)
//
//        val adapter = adapter(views = views)
//
//        val recycler = recycler(adapter)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
//
//        // Testing
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is ParagraphViewHolder)
//
//        val text = holder.content.text.toString()
//
//        assertEquals(
//            expected = paragraph.text,
//            actual = text
//        )
//    }
//
//    @Test
//    fun `should set text color for paragraph holder`() {
//
//        // Setup
//
//        val paragraph = BlockView.Paragraph(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid(),
//            color = ThemeColor.BLUE.title
//        )
//
//        val views = listOf(paragraph)
//
//        val adapter = adapter(views = views)
//
//        val recycler = recycler(adapter)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
//
//        // Testing
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is ParagraphViewHolder)
//
//        val color = holder.content.currentTextColor
//
//        assertEquals(
//            expected = ThemeColor.BLUE.text,
//            actual = color
//        )
//    }
//
//    @Test
//    fun `should update paragraph holder with new text`() {
//
//        // Setup
//
//        val paragraph = BlockView.Paragraph(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid()
//        )
//
//        val updated = paragraph.copy(
//            text = MockDataFactory.randomString()
//        )
//
//        val payloads: MutableList<Any> = mutableListOf(
//            BlockViewDiffUtil.Payload(
//                changes = listOf(BlockViewDiffUtil.TEXT_CHANGED)
//            )
//        )
//
//        val views = listOf(paragraph)
//
//        val adapter = adapter(views)
//
//        val recycler = recycler(adapter)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is ParagraphViewHolder)
//
//        // Testing
//
//        assertEquals(
//            expected = paragraph.text,
//            actual = holder.content.text.toString()
//        )
//
//        adapter.updateWithDiffUtil(listOf(updated))
//
//        adapter.onBindViewHolder(holder, 0, payloads)
//
//        assertEquals(
//            expected = updated.text,
//            actual = holder.content.text.toString()
//        )
//    }
//
//    @Test
//    fun `should update paragraph background color`() {
//
//        // Setup
//
//        val paragraph = BlockView.Paragraph(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid()
//        )
//
//        val updated = paragraph.copy(
//            backgroundColor = ThemeColor.PURPLE.title
//        )
//
//        val payloads: MutableList<Any> = mutableListOf(
//            BlockViewDiffUtil.Payload(
//                changes = listOf(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)
//            )
//        )
//
//        val views = listOf(paragraph)
//
//        val adapter = adapter(views)
//
//        val recycler = recycler(adapter)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is ParagraphViewHolder)
//
//        // Testing
//
//        assertEquals(
//            expected = null,
//            actual = holder.root.background
//        )
//
//        adapter.updateWithDiffUtil(listOf(updated))
//
//        adapter.onBindViewHolder(holder, 0, payloads)
//
//        assertEquals(
//            expected = ThemeColor.PURPLE.background,
//            actual = (holder.root.background as ColorDrawable).color
//        )
//    }
//
//    @Test
//    fun `should update paragraph holder with new text color`() {
//
//        // Setup
//
//        val paragraph = BlockView.Paragraph(
//            text = MockDataFactory.randomString(),
//            id = MockDataFactory.randomUuid(),
//            color = ThemeColor.BLUE.title
//        )
//
//        val updated = paragraph.copy(
//            color = ThemeColor.GREEN.title
//        )
//
//        val payloads: MutableList<Any> = mutableListOf(
//            BlockViewDiffUtil.Payload(
//                changes = listOf(BlockViewDiffUtil.TEXT_COLOR_CHANGED)
//            )
//        )
//
//        val views = listOf(paragraph)
//
//        val adapter = adapter(views)
//
//        val recycler = recycler(adapter)
//
//        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)
//
//        adapter.onBindViewHolder(holder, 0)
//
//        check(holder is ParagraphViewHolder)
//
//        // Testing
//
//        assertEquals(
//            expected = ThemeColor.BLUE.text,
//            actual = holder.content.currentTextColor
//        )
//
//        adapter.updateWithDiffUtil(listOf(updated))
//
//        adapter.onBindViewHolder(holder, 0, payloads)
//
//        assertEquals(
//            expected = ThemeColor.GREEN.text,
//            actual = holder.content.currentTextColor
//        )
//    }
//}