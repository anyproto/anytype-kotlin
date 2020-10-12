package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.isLinksOrMentionsPresent
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkupExtTest {

    @Test
    fun `should find link markup`() {
        val given = listOf(
            Markup.Mark(from = 0, to = 5, type = Markup.Type.BOLD),
            Markup.Mark(from = 23, to = 31, type = Markup.Type.STRIKETHROUGH),
            Markup.Mark(from = 32, to = 43, type = Markup.Type.LINK, param = "www.google.com")
        )

        val actual = given.isLinksOrMentionsPresent()

        assertTrue(actual = actual)
    }

    @Test
    fun `should find mention markup`() {
        val given = listOf(
            Markup.Mark(from = 0, to = 5, type = Markup.Type.BOLD),
            Markup.Mark(from = 23, to = 31, type = Markup.Type.STRIKETHROUGH),
            Markup.Mark(from = 32, to = 43, type = Markup.Type.MENTION, param = "fjdhghdjhj")
        )

        val actual = given.isLinksOrMentionsPresent()

        assertTrue(actual = actual)
    }

    @Test
    fun `should not find link markup`() {
        val given = listOf(
            Markup.Mark(from = 0, to = 5, type = Markup.Type.BOLD),
            Markup.Mark(from = 23, to = 31, type = Markup.Type.STRIKETHROUGH)
        )

        val actual = given.isLinksOrMentionsPresent()

        assertFalse(actual = actual)
    }
}