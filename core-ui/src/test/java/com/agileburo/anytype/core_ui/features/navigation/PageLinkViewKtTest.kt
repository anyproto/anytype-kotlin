package com.agileburo.anytype.core_ui.features.navigation

import com.agileburo.anytype.core_ui.MockDataFactory
import org.junit.Test

import org.junit.Assert.*

class PageLinkViewKtTest {

    @Test
    fun `should contain text`() {
        val pageLink = PageLinkView(
            id = MockDataFactory.randomUuid(),
            subtitle = "Subtitle first",
            title = "Title first",
            emoji = null,
            image = null
        )
        val text = "IRst"

        val result = pageLink.isContainsText(text)

        assertTrue(result)
    }

    @Test
    fun `should not contain text`() {
        val pageLink = PageLinkView(
            id = MockDataFactory.randomUuid(),
            subtitle = "Subtitle first",
            title = "Title first",
            emoji = null,
            image = null
        )
        val text = "ECO"

        val result = pageLink.isContainsText(text)

        assertFalse(result)
    }
}