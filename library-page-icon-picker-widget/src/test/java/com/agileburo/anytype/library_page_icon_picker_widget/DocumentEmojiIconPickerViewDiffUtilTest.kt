package com.agileburo.anytype.library_page_icon_picker_widget

import com.agileburo.anytype.library_page_icon_picker_widget.model.DocumentEmojiIconPickerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerViewDiffUtil
import org.junit.Test
import kotlin.test.assertEquals

class DocumentEmojiIconPickerViewDiffUtilTest {

    @Test
    fun `two emoji-filter items should be considered the same`() {
        val old = listOf(
            DocumentEmojiIconPickerView.EmojiFilter
        )

        val new = listOf(
            DocumentEmojiIconPickerView.EmojiFilter
        )

        val util = PageIconPickerViewDiffUtil(
            old = old,
            new = new
        )

        val result = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = result
        )
    }

    @Test
    fun `two emoji items should be considered the same`() {
        val old = listOf(
            DocumentEmojiIconPickerView.Emoji(
                alias = "grining",
                unicode = "U+13131"
            )
        )

        val new = listOf(
            DocumentEmojiIconPickerView.Emoji(
                alias = "grining",
                unicode = "U+13131"
            )
        )

        val util = PageIconPickerViewDiffUtil(
            old = old,
            new = new
        )

        val result = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = result
        )
    }

    @Test
    fun `two emoji items should be considered different`() {
        val old = listOf(
            DocumentEmojiIconPickerView.Emoji(
                alias = "smile",
                unicode = "U+13131"
            )
        )

        val new = listOf(
            DocumentEmojiIconPickerView.Emoji(
                alias = "grining",
                unicode = "U+13131"
            )
        )

        val util = PageIconPickerViewDiffUtil(
            old = old,
            new = new
        )

        val result = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = false,
            actual = result
        )
    }
}