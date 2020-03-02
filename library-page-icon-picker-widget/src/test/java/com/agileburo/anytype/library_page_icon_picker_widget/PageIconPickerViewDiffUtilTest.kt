package com.agileburo.anytype.library_page_icon_picker_widget

import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerView
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerViewDiffUtil
import org.junit.Test
import kotlin.test.assertEquals

class PageIconPickerViewDiffUtilTest {

    @Test
    fun `two emoji-filter items should be considered the same`() {
        val old = listOf(
            PageIconPickerView.EmojiFilter
        )

        val new = listOf(
            PageIconPickerView.EmojiFilter
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
    fun `two upload-photo-action items should be considered the same`() {
        val old = listOf(
            PageIconPickerView.Action.UploadPhoto
        )

        val new = listOf(
            PageIconPickerView.Action.UploadPhoto
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
    fun `two choose-emoji-action items should be considered the same`() {
        val old = listOf(
            PageIconPickerView.Action.ChooseEmoji
        )

        val new = listOf(
            PageIconPickerView.Action.ChooseEmoji
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
    fun `two pick-random-emoji-action items should be considered the same`() {
        val old = listOf(
            PageIconPickerView.Action.PickRandomly
        )

        val new = listOf(
            PageIconPickerView.Action.PickRandomly
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
            PageIconPickerView.Emoji(
                alias = "grining",
                unicode = "U+13131"
            )
        )

        val new = listOf(
            PageIconPickerView.Emoji(
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
            PageIconPickerView.Emoji(
                alias = "smile",
                unicode = "U+13131"
            )
        )

        val new = listOf(
            PageIconPickerView.Emoji(
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