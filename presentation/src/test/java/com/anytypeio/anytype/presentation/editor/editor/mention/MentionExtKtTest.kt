package com.anytypeio.anytype.presentation.editor.editor.mention

import MockDataFactory
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import kotlin.test.Test
import kotlin.test.assertEquals

class MentionExtTest {

    @Test
    fun `should filter mentions by filter 1`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@C"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 2`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@CD"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 3`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2, mention3)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 4`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@EfB1"

        val result = mentions.filterMentionsBy(filter)

        val expected = emptyList<DefaultObjectView>()

        assertEquals(expected, result)
    }
}