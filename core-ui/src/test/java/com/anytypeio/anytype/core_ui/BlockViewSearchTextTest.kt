package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.nextSearchTarget
import com.anytypeio.anytype.core_ui.features.page.previousSearchTarget
import org.junit.Test
import kotlin.test.assertEquals

class BlockViewSearchTextTest {

    @Test
    fun `should select all search results from title one after another `() {

        val field = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4, 5..6),
            target = IntRange.EMPTY
        )

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(field)
        )

        val views = listOf(title)

        val firstTimeExpected = listOf(
            title.copy(searchFields = listOf(field.copy(target = field.highlights[0])))
        )
        val firstTimeResult = views.nextSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(
            title.copy(searchFields = listOf(field.copy(target = field.highlights[1])))
        )
        val secondTimeResult = firstTimeResult.nextSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(
            title.copy(searchFields = listOf(field.copy(target = field.highlights[2])))
        )
        val thirdTimeResult = secondTimeResult.nextSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)

        val fourthTimeExpected = listOf(
            title.copy(searchFields = listOf(field.copy(target = field.highlights[2])))
        )
        val fourthTimeResult = thirdTimeResult.nextSearchTarget()

        assertEquals(expected = fourthTimeExpected, actual = fourthTimeResult)
    }

    @Test
    fun `should find next target in the first highlight of the first field in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val views = listOf(bookmark)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights.first()),
                    field2
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of the first field in file`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val file = BlockView.Media.File(
            id = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            searchFields = listOf(field1),
            hash = MockDataFactory.randomString(),
            mime = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            indent = 0,
            url = MockDataFactory.randomUuid()
        )

        val views = listOf(file)

        val expected = listOf(
            file.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights.first())
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of the first field in page`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val page = BlockView.Page(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(field1),
            indent = 0,
            emoji = null,
            image = null
        )

        val views = listOf(page)

        val expected = listOf(
            page.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights.first())
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of the first field in archived page`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val page = BlockView.PageArchive(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(field1),
            indent = 0,
            emoji = null,
            image = null
        )

        val views = listOf(page)

        val expected = listOf(
            page.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights.first())
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of paragraph following after in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid().take(3),
            highlights = listOf(1..2, 3..4),
            target = 3..4
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid().take(3),
            highlights = listOf(1..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomUuid().take(3),
            title = MockDataFactory.randomString().take(2),
            description = MockDataFactory.randomString().take(2),
            searchFields = listOf(field1),
            url = MockDataFactory.randomString().take(2)
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid().take(3),
            text = MockDataFactory.randomString().take(2),
            searchFields = listOf(field2)
        )

        val views = listOf(bookmark, paragraph)

        val expected = listOf(
            bookmark.copy(searchFields = listOf(field1.copy(target = IntRange.EMPTY))),
            paragraph.copy(searchFields = listOf(field2.copy(target = 1..4)))
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the second highlight of the first field in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = 1..2
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val views = listOf(bookmark)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights[1]),
                    field2
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of the second field in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = 3..4
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val views = listOf(bookmark)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1.copy(target = IntRange.EMPTY),
                    field2.copy(target = field2.highlights.first())
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should not find next target in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = 3..4
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = emptyList(),
            target = IntRange.EMPTY
        )

        val field3 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = emptyList(),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2, field3),
            url = MockDataFactory.randomString()
        )

        val views = listOf(bookmark)

        val expected = listOf(bookmark.copy())

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find next target in the first highlight of the third field in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = 3..4
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = emptyList(),
            target = IntRange.EMPTY
        )

        val field3 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val title = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2, field3),
            url = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val expected = listOf(
            title.copy(
                searchFields = listOf(
                    field1.copy(target = IntRange.EMPTY),
                    field2.copy(),
                    field3.copy(target = field3.highlights.first())
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should select first result from title and then first and second result from paragraph`() {

        val titleField = BlockView.Searchable.Field(
            highlights = listOf(1..2),
            target = IntRange.EMPTY
        )

        val paragraphField = BlockView.Searchable.Field(
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(titleField)
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(paragraphField)
        )

        val views = listOf(title, paragraph)

        val firstTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 1..2
                    )
                )
            ),
            paragraph
        )

        val firstTimeResult = views.nextSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(
            title,
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = 3..4
                    )
                )
            )
        )
        val secondTimeResult = firstTimeResult.nextSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(
            title,
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = 5..6
                    )
                )
            )
        )
        val thirdTimeResult = secondTimeResult.nextSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)

        val fourthTimeExpected = listOf(
            title,
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = 5..6
                    )
                )
            )
        )

        val fourthTimeResult = thirdTimeResult.nextSearchTarget()

        assertEquals(expected = fourthTimeExpected, actual = fourthTimeResult)
    }

    @Test
    fun `should select all previous search results from title one after another`() {

        val titleField = BlockView.Searchable.Field(
            highlights = listOf(1..2, 3..4, 5..6),
            target = 5..6
        )

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(titleField)
        )

        val views = listOf(title)

        val firstTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 3..4
                    )
                )
            )
        )
        val firstTimeResult = views.previousSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 1..2
                    )
                )
            )
        )
        val secondTimeResult = firstTimeResult.previousSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 1..2
                    )
                )
            )
        )

        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }

    @Test
    fun `should select first result from paragraph and then last result from paragraph`() {

        val titleField = BlockView.Searchable.Field(
            highlights = listOf(1..2),
            target = IntRange.EMPTY
        )

        val paragraphField = BlockView.Searchable.Field(
            highlights = listOf(3..4, 5..6),
            target = 5..6
        )

        val title = BlockView.Title.Document(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(titleField)
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(paragraphField)
        )

        val views = listOf(title, paragraph)

        val firstTimeExpected = listOf(
            title,
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = 3..4
                    )
                )
            )
        )

        val firstTimeResult = views.previousSearchTarget()

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 1..2
                    )
                )
            ),
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = IntRange.EMPTY
                    )
                )
            )
        )
        val secondTimeResult = firstTimeResult.previousSearchTarget()

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeExpected = listOf(
            title.copy(
                searchFields = listOf(
                    titleField.copy(
                        target = 1..2
                    )
                )
            ),
            paragraph.copy(
                searchFields = listOf(
                    paragraphField.copy(
                        target = IntRange.EMPTY
                    )
                )
            )
        )

        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }

    @Test
    fun `should select previous search result in third paragraph`() {

        val title = BlockView.Title.Document(
            id = "title",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    target = IntRange.EMPTY,
                    highlights = listOf(1..2)
                )
            )
        )

        val p1 = BlockView.Text.Paragraph(
            id = "1",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    target = IntRange.EMPTY,
                    highlights = listOf(3..4, 5..6)
                )
            )
        )

        val p2 = BlockView.Text.Paragraph(
            id = "2",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    target = IntRange.EMPTY,
                    highlights = listOf(3..4, 5..6)
                )
            )
        )

        val p3 = BlockView.Text.Paragraph(
            id = "3",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    target = IntRange.EMPTY,
                    highlights = listOf(3..4, 5..6)
                )
            )
        )

        val p4 = BlockView.Text.Paragraph(
            id = "4",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    target = IntRange.EMPTY,
                    highlights = listOf(1..3, 5..6),
                )
            )
        )

        val p5 = BlockView.Text.Paragraph(
            id = "5",
            text = MockDataFactory.randomString(),
            searchFields = listOf(
                BlockView.Searchable.Field(
                    highlights = listOf(3..4, 5..6),
                    target = 5..6
                )
            )
        )

        val views = listOf(title, p1, p2, p3, p4, p5)

        val firstTimeResult = views.previousSearchTarget()

        val firstTimeExpected = listOf(
            title, p1, p2, p3, p4, p5.copy(
                searchFields = listOf(
                    BlockView.Searchable.Field(
                        highlights = listOf(3..4, 5..6),
                        target = 3..4
                    )
                )
            )
        )

        assertEquals(expected = firstTimeExpected, actual = firstTimeResult)

        val secondTimeResult = firstTimeResult.previousSearchTarget()

        val secondTimeExpected = listOf(
            title, p1, p2, p3,
            p4.copy(
                searchFields = listOf(
                    BlockView.Searchable.Field(
                        target = 5..6,
                        highlights = listOf(1..3, 5..6),
                    )
                )
            ),
            p5.copy(
                searchFields = listOf(
                    BlockView.Searchable.Field(
                        highlights = listOf(3..4, 5..6),
                        target = IntRange.EMPTY
                    )
                )
            )
        )

        assertEquals(expected = secondTimeExpected, actual = secondTimeResult)

        val thirdTimeResult = secondTimeResult.previousSearchTarget()

        val thirdTimeExpected = listOf(
            title, p1, p2, p3,
            p4.copy(
                searchFields = listOf(
                    BlockView.Searchable.Field(
                        target = 1..3,
                        highlights = listOf(1..3, 5..6),
                    )
                )
            ),
            p5.copy(
                searchFields = listOf(
                    BlockView.Searchable.Field(
                        highlights = listOf(3..4, 5..6),
                        target = IntRange.EMPTY
                    )
                )
            )
        )

        assertEquals(expected = thirdTimeExpected, actual = thirdTimeResult)
    }

    @Test
    fun `should find search result in the last highlight of the previous search field in bookmark`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = 3..4
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val views = listOf(bookmark)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1.copy(target = field1.highlights.last()),
                    field2.copy(target = IntRange.EMPTY)
                )
            )
        )

        val actual = views.previousSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find search result in the last highlight of the last search field of the previous view having highlights`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val p1Field = BlockView.Searchable.Field(
            highlights = listOf(),
            target = IntRange.EMPTY
        )

        val p2Field = BlockView.Searchable.Field(
            highlights = listOf(3..4, 5..6),
            target = 3..4
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val p1 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p1Field)
        )

        val p2 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p2Field)
        )

        val views = listOf(bookmark, p1, p2)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1,
                    field2.copy(target = field2.highlights.last())
                )
            ),
            p1,
            p2.copy(
                searchFields = listOf(
                    p2Field.copy(
                        target = IntRange.EMPTY
                    )
                )
            )
        )

        val actual = views.previousSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find search result in the first highlight of the first search field of the first next view having highlights`() {

        val field1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4),
            target = IntRange.EMPTY
        )

        val field2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = 5..6
        )

        val p1Field = BlockView.Searchable.Field(
            highlights = listOf(),
            target = IntRange.EMPTY
        )

        val p2Field = BlockView.Searchable.Field(
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString()
        )

        val p1 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p1Field)
        )

        val p2 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p2Field)
        )

        val views = listOf(bookmark, p1, p2)

        val expected = listOf(
            bookmark.copy(
                searchFields = listOf(
                    field1,
                    field2.copy(target = IntRange.EMPTY)
                )
            ),
            p1,
            p2.copy(
                searchFields = listOf(
                    p2Field.copy(
                        target = 3..4
                    )
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should find first result in the first highlight of the second field of the view following paragraph without highlights`() {

        val pField = BlockView.Searchable.Field(
            highlights = listOf(),
            target = IntRange.EMPTY
        )

        val bField1 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(),
            target = IntRange.EMPTY
        )

        val bField2 = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(bField1, bField2),
            url = MockDataFactory.randomString()
        )

        val paragraph = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(pField)
        )

        val views = listOf(paragraph, bookmark)

        val expected = listOf(
            paragraph,
            bookmark.copy(
                searchFields = listOf(
                    bField1,
                    bField2.copy(
                        target = 3..4
                    )
                )
            )
        )

        val actual = views.nextSearchTarget()

        assertEquals(expected = expected, actual = actual)
    }
}