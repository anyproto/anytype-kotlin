package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_ui.common.isLinksOrMentionsPresent
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkupExtTest {

    @Test
    fun `should find link markup`() {
        val given = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Strikethrough(from = 23, to = 31),
            Markup.Mark.Link(from = 32, to = 43, param = "www.google.com")
        )

        val actual = given.isLinksOrMentionsPresent()

        assertTrue(actual = actual)
    }

    @Test
    fun `should find mention markup`() {
        val given = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Strikethrough(from = 23, to = 31),
            Markup.Mark.Mention.Base(from = 32, to = 43, param = "fjdhghdjhj")
        )

        val actual = given.isLinksOrMentionsPresent()

        assertTrue(actual = actual)
    }

    @Test
    fun `should not find link markup`() {
        val given = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Strikethrough(from = 23, to = 31)
        )

        val actual = given.isLinksOrMentionsPresent()

        assertFalse(actual = actual)
    }
}