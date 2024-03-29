package com.anytypeio.anytype.mocking

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent

object MockUiTests {

    val BLOCK_H1 = Block(
        id = "id_h1",
        children = emptyList(),
        content = StubTextContent(
            text = "H1 block, marks should not render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.BOLD,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.H1
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_H2 = Block(
        id = "id_h2",
        children = emptyList(),
        content = StubTextContent(
            text = "H2 block, marks should not render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.H2
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_H3 = Block(
        id = "id_h3",
        children = emptyList(),
        content = StubTextContent(
            text = "H3 block, marks should not render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.H3
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_PARAGRAPH = Block(
        id = "id_paragraph",
        children = emptyList(),
        content = StubTextContent(
            text = "Paragraph block, boldMarkup, italicMarkup, strikethroughMarkup, keyboardMarkup, linkMarkup, backgroundColorMarkup, textColorMarkup",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.BOLD,
                    range = IntRange(17, 27)
                ),
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.ITALIC,
                    range = IntRange(29, 41)
                ),
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(43, 62)
                ),
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.KEYBOARD,
                    range = IntRange(64, 78),
                    param = "#F3F2EC"
                ),
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.LINK,
                    range = IntRange(80, 90),
                    param = "https://anytype.io"
                )
            ),
            style = Block.Content.Text.Style.P
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_PARAGRAPH_1 = Block(
        id = "id_paragraph_1",
        children = emptyList(),
        content = StubTextContent(
            text = "Paragraph block.",
            marks = listOf(),
            style = Block.Content.Text.Style.P
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_HIGHLIGHT = Block(
        id = "id_quote",
        children = emptyList(),
        content = StubTextContent(
            text = "Quote block, marks should render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.QUOTE
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_BULLET = Block(
        id = "id_bullet",
        children = emptyList(),
        content = StubTextContent(
            text = "Bullet block, marks should render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.BULLET
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_NUMBERED_1 = Block(
        id = "id_numbered_1",
        children = emptyList(),
        content = StubTextContent(
            text = "Numbered 1 block, marks should not render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.NUMBERED
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_TOGGLE = Block(
        id = "id_toggle",
        children = emptyList(),
        content = StubTextContent(
            text = "Toggle block, marks should not render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.TOGGLE
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )

    val BLOCK_CHECKBOX = Block(
        id = "id_checkbox",
        children = emptyList(),
        content = StubTextContent(
            text = "Checkbox block, marks should render",
            marks = listOf(
                Block.Content.Text.Mark(
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH,
                    range = IntRange(0, 15)
                )
            ),
            style = Block.Content.Text.Style.CHECKBOX
        ),
        fields = Block.Fields(
            mapOf("name" to "NAME")
        )
    )
}