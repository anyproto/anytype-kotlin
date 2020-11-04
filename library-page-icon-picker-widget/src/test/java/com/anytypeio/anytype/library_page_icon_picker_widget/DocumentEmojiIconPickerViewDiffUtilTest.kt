package com.anytypeio.anytype.library_page_icon_picker_widget

import com.anytypeio.anytype.library_page_icon_picker_widget.model.PageIconPickerViewDiffUtil
import com.anytypeio.anytype.presentation.page.picker.EmojiPickerView
import org.junit.Test
import kotlin.test.assertEquals

class DocumentEmojiIconPickerViewDiffUtilTest {

    @Test
    fun `two emoji items should be considered the same`() {

        val page = 5
        val index = 5

        val old = listOf(
            EmojiPickerView.Emoji(
                unicode = "U+13131",
                page = page,
                index = index
            )
        )

        val new = listOf(
            EmojiPickerView.Emoji(
                unicode = "U+13131",
                page = page,
                index = index
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
}