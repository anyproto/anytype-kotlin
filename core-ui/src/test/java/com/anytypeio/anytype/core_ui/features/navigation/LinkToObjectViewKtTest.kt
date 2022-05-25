package com.anytypeio.anytype.core_ui.features.navigation

import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.filterBy
import com.anytypeio.anytype.presentation.navigation.isContainsText
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Assert.*
import org.junit.Test

class LinkToObjectViewKtTest {

    @Test
    fun `should contain text`() {
        val pageLink = ObjectView(
            id = MockDataFactory.randomUuid(),
            subtitle = "Subtitle first",
            title = "Title first",
            icon = ObjectIcon.None
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
            icon = ObjectIcon.None
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
                icon = ObjectIcon.None
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString() + text,
                icon = ObjectIcon.None
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString() + text,
                icon = ObjectIcon.None
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
            icon = ObjectIcon.None
        )
        val pageLink3 = ObjectView(
            id = MockDataFactory.randomUuid(),
            subtitle = MockDataFactory.randomString() + text + MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            icon = ObjectIcon.None
        )
        val list = listOf(
            pageLink1,
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                icon = ObjectIcon.None
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
                icon = ObjectIcon.None
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                icon = ObjectIcon.None
            ),
            ObjectView(
                id = MockDataFactory.randomUuid(),
                subtitle = MockDataFactory.randomString(),
                title = MockDataFactory.randomString(),
                icon = ObjectIcon.None
            )
        )

        val result = list.filterBy(text)

        val expected = listOf<ObjectView>()
        assertEquals(expected, result)
    }
}