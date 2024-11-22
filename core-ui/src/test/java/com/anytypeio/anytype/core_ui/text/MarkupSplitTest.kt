package com.anytypeio.anytype.core_ui.text

import com.anytypeio.anytype.core_models.Block
import kotlin.test.Test

class MarkupSplitTest {

    @Test
    fun test() {

        val givenString = "Hello World"

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                param = null,
                range = 0..0
            ),
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.ITALIC,
                param = null,
                range = 0..0
            ),
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                param = null,
                range = 1..1
            )
        )


        val result = givenString.splitBy(
            marks = marks
        )

        print(result)


    }

}