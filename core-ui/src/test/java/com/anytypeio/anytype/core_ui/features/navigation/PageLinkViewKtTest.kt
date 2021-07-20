package com.anytypeio.anytype.core_ui.features.navigation

import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.filterBy
import com.anytypeio.anytype.presentation.navigation.isContainsText
import org.junit.Assert.*
import org.junit.Test

class PageLinkViewKtTest {

    @Test
    fun `should contain text`() {
        val pageLink = ObjectView(
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
        val pageLink = ObjectView(
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

    @Test
    fun `should return original list`() {
        val text = "same"
        val list = listOf(
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString() + text,
                title = MockDataFactory.randomString(),
                emoji = null,
                image = null
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString() + text,
                emoji = null,
                image = null
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString() + text,
                emoji = null,
                image = null
            )
        )

        val result = list.filterBy(text)

        assertEquals(list, result)
    }

    @Test
    fun `should return list without one item`() {
        val text = "same"
        val pageLink1 = ObjectView(
            id = MockDataFactory.randomUuid(),
            subtitle = MockDataFactory.randomString() + text,
            title = MockDataFactory.randomString(),
            emoji = null,
            image = null
        )
        val pageLink3 = ObjectView(
            id = MockDataFactory.randomUuid(),
            subtitle = MockDataFactory.randomString() + text + MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            emoji = null,
            image = null
        )
        val list = listOf(
            pageLink1,
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                emoji = null,
                image = null
            ),
            pageLink3
        )

        val result = list.filterBy(text)

        val expected = listOf(pageLink1, pageLink3)
        assertEquals(expected, result)
    }

    @Test
    fun `should return empty list`() {
        val text = "same"
        val list = listOf(
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                emoji = null,
                image = null
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                emoji = null,
                image = null
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                emoji = null,
                image = null
            )
        )

        val result = list.filterBy(text)

        val expected = listOf<ObjectView>()
        assertEquals(expected, result)
    }
}