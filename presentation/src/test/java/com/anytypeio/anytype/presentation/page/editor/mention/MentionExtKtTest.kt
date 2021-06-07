package com.anytypeio.anytype.presentation.page.editor.mention

import MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class MentionExtTest {

    @Test
    fun `should filter mentions by filter 1`() {

        val mention1 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "abc",
            emoji = null,
            image = null
        )

        val mention2 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "Cde",
            emoji = null,
            image = null
        )

        val mention3 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "EfB",
            emoji = null,
            image = null
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@C"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 2`() {

        val mention1 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "abc",
            emoji = null,
            image = null
        )

        val mention2 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "Cde",
            emoji = null,
            image = null
        )

        val mention3 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "EfB",
            emoji = null,
            image = null
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@CD"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 3`() {

        val mention1 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "abc",
            emoji = null,
            image = null
        )

        val mention2 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "Cde",
            emoji = null,
            image = null
        )

        val mention3 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "EfB",
            emoji = null,
            image = null
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2, mention3)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 4`() {

        val mention1 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "abc",
            emoji = null,
            image = null
        )

        val mention2 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "Cde",
            emoji = null,
            image = null
        )

        val mention3 = Mention(
            id = MockDataFactory.randomUuid(),
            title = "EfB",
            emoji = null,
            image = null
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@EfB1"

        val result = mentions.filterMentionsBy(filter)

        val expected = emptyList<Mention>()

        assertEquals(expected, result)
    }
}