package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.nextSearchTarget
import com.anytypeio.anytype.core_ui.features.page.previousSearchTarget
import org.junit.Test
import kotlin.test.assertEquals

class BlockViewSearchTextTest {

    @Test
    fun `should select all search results from title one after another `() {

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(1..2, 3..4, 5..6),
            target = IntRange.EMPTY
        )

        val views = listOf(title)

        val firstTimeExpected = listOf(title.copy(target = title.highlights[0]))
        val firstTimeResult = views.nextSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(title.copy(target = title.highlights[1]))
        val secondTimeResult = firstTimeResult.nextSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(title.copy(target = title.highlights[2]))
        val thirdTimeResult = secondTimeResult.nextSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)

        val fourthTimeExpected = listOf(title.copy(target = title.highlights[2]))
        val fourthTimeResult = thirdTimeResult.nextSearchTarget()

        assertEquals(expected = fourthTimeExpected, actual = fourthTimeResult)
    }

    @Test
    fun `should select all previous search results from title one after another`() {

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(1..2, 3..4, 5..6),
            target = 5..6
        )

        val views = listOf(title)

        val firstTimeExpected = listOf(title.copy(target = title.highlights[1]))
        val firstTimeResult = views.previousSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(title.copy(target = title.highlights[0]))
        val secondTimeResult = firstTimeResult.previousSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(title.copy(target = title.highlights[0]))
        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }

    @Test
    fun `should select first result from title and then first and second result from paragraph`() {

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(1..2),
            target = IntRange.EMPTY
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val views = listOf(title, paragraph)

        val firstTimeExpected = listOf(title.copy(target = title.highlights[0]), paragraph)
        val firstTimeResult = views.nextSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(title, paragraph.copy(target = 3..4))
        val secondTimeResult = firstTimeResult.nextSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(title, paragraph.copy(target = 5..6))
        val thirdTimeResult = secondTimeResult.nextSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)

        val fourthTimeExpected = listOf(title, paragraph.copy(target = 5..6))
        val fourthTimeResult = thirdTimeResult.nextSearchTarget()

        assertEquals(expected = fourthTimeExpected, actual = fourthTimeResult)
    }

    @Test
    fun `should select first result from paragraph and then last result from paragraph`() {

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(1..2),
            target = IntRange.EMPTY
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = 5..6
        )

        val views = listOf(title, paragraph)

        val firstTimeExpected = listOf(title, paragraph.copy(target = 3..4))
        val firstTimeResult = views.previousSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected =
            listOf(title.copy(target = 1..2), paragraph.copy(target = IntRange.EMPTY))
        val secondTimeResult = firstTimeResult.previousSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected =
            listOf(title.copy(target = 1..2), paragraph.copy(target = IntRange.EMPTY))
        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }

    @Test
    fun `should select previous search result in third paragraph`() {

        val title = BlockView.Title.Document(
            id = "title",
            text = MockDataFactory.randomString(),
            highlights = listOf(1..2),
            target = IntRange.EMPTY
        )

        val p1 = BlockView.Text.Paragraph(
            id = "1",
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val p2 = BlockView.Text.Paragraph(
            id = "2",
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val p3 = BlockView.Text.Paragraph(
            id = "3",
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val p4 = BlockView.Text.Paragraph(
            id = "4",
            text = MockDataFactory.randomString(),
            highlights = listOf(1..3, 5..6),
            target = IntRange.EMPTY
        )

        val p5 = BlockView.Text.Paragraph(
            id = "5",
            text = MockDataFactory.randomString(),
            highlights = listOf(3..4, 5..6),
            target = 5..6
        )

        val views = listOf(title, p1, p2, p3, p4, p5)

        val firstTimeResult = views.previousSearchTarget()

        val firstTimeExpected = listOf(
            title, p1, p2, p3, p4, p5.copy(target = 3..4)
        )

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeResult = firstTimeResult.previousSearchTarget()

        val secondTimeExpected = listOf(
            title, p1, p2, p3, p4.copy(target = 5..6), p5.copy(target = IntRange.EMPTY)
        )

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        val thirdTimeExpected = listOf(
            title, p1, p2, p3, p4.copy(target = 1..3), p5.copy(target = IntRange.EMPTY)
        )

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }
}