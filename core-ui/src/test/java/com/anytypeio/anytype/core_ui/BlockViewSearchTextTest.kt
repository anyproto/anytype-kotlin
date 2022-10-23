package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.ext.clearSearchHighlights
import com.anytypeio.anytype.presentation.editor.editor.ext.highlight
import com.anytypeio.anytype.presentation.editor.editor.ext.nextSearchTarget
import com.anytypeio.anytype.presentation.editor.editor.ext.previousSearchTarget
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.search.search
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import java.util.regex.Pattern
import kotlin.test.assertEquals

class BlockViewSearchTextTest {

    @Test
    fun `should select all search results from title one after another `() {

        val field = BlockView.Searchable.Field(
            key = MockDataFactory.randomUuid(),
            highlights = listOf(1..2, 3..4, 5..6),
            target = IntRange.EMPTY
        )

        val title = BlockView.Title.Basic(
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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

        val page = BlockView.LinkToObject.Default.Text(
            id = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(field1),
            indent = 0,
            icon = ObjectIcon.None
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

        val page = BlockView.LinkToObject.Archived(
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
            url = MockDataFactory.randomString().take(2),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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

        val title = BlockView.Title.Basic(
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

        val title = BlockView.Title.Basic(
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

        val title = BlockView.Title.Basic(
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

        val title = BlockView.Title.Basic(
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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
    fun `should find search result in the first highlight of the second field of the first next view having highlights`() {

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

        val p2Field1 = BlockView.Searchable.Field(
            highlights = emptyList(),
            target = IntRange.EMPTY
        )

        val p2Field2 = BlockView.Searchable.Field(
            highlights = listOf(3..4, 5..6),
            target = IntRange.EMPTY
        )

        val bookmark = BlockView.Media.Bookmark(
            id = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            searchFields = listOf(field1, field2),
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
        )

        val p1 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p1Field)
        )

        val p2 = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            searchFields = listOf(p2Field1, p2Field2)
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
                    p2Field1,
                    p2Field2.copy(
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
            url = MockDataFactory.randomString(),
            isPreviousBlockMedia = false
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


    @Test
    fun `should update search fields ranges in table cells`() {

        val rowId1 = "rowId1"
        val rowId2 = "rowId2"
        val columnId1 = "columnId1"
        val columnId2 = "columnId2"
        val columnId3 = "columnId3"

        val query = "bc"

        val row1Block1 = StubParagraph(id = "$rowId1-$columnId1", text = "ab1")
        val row1Block2 = StubParagraph(id = "$rowId1-$columnId2", text = "ab2")
        val row1Block3 = StubParagraph(id = "$rowId1-$columnId3", text = "ac3")
        val row2Block1 = StubParagraph(id = "$rowId2-$columnId1", text = "bc1")
        val row2Block2 = StubParagraph(id = "$rowId2-$columnId2", text = "bb2")
        val row2Block3 = StubParagraph(id = "$rowId2-$columnId3", text = "bc3")

        val cells = listOf(
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block3.id,
                    text = row1Block3.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block1.id,
                    text = row2Block1.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block2.id,
                    text = row2Block2.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block3.id,
                    text = row2Block3.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2)
            )
        )

        val columns = listOf(
            BlockView.Table.Column(id = columnId1, background = ThemeColor.DEFAULT),
            BlockView.Table.Column(id = columnId2, background = ThemeColor.DEFAULT),
            BlockView.Table.Column(id = columnId3, background = ThemeColor.DEFAULT)
        )

        val tableId = MockDataFactory.randomUuid()

        val views = listOf<BlockView>(
            BlockView.Table(
                id = tableId,
                cells = cells,
                columns = columns,
                rowCount = 2,
                isSelected = false
            )
        )

        val expectedCells = listOf(
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = emptyList(),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId1,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = emptyList(),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId1,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block3.id,
                    text = row1Block3.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = emptyList(),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId1,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block1.id,
                    text = row2Block1.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId2,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block2.id,
                    text = row2Block2.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = emptyList(),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId2,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block3.id,
                    text = row2Block3.content.asText().text,
                    searchFields = listOf(
                        BlockView.Searchable.Field(
                            key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange.EMPTY
                        )
                    )
                ),
                rowId = rowId2,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2)
            )
        )

        val flags = Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
        val escaped = Pattern.quote(query)
        val pattern = Pattern.compile(escaped, flags)

        val actual = views.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val expected = listOf(
            BlockView.Table(
                id = tableId,
                cells = expectedCells,
                columns = columns,
                rowCount = 2,
                isSelected = false
            )
        )

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `should clear search fields ranges in table cells`() {

        val rowId1 = "rowId1"
        val rowId2 = "rowId2"
        val columnId1 = "columnId1"
        val columnId2 = "columnId2"
        val columnId3 = "columnId3"

        val query = "bc"

        val row1Block1 = StubParagraph(id = "$rowId1-$columnId1", text = "ab1")
        val row1Block2 = StubParagraph(id = "$rowId1-$columnId2", text = "ab2")
        val row1Block3 = StubParagraph(id = "$rowId1-$columnId3", text = "ac3")
        val row2Block1 = StubParagraph(id = "$rowId2-$columnId1", text = "bc1")
        val row2Block2 = StubParagraph(id = "$rowId2-$columnId2", text = "bb2")
        val row2Block3 = StubParagraph(id = "$rowId2-$columnId3", text = "bc3")

        val cells = listOf(
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block3.id,
                    text = row1Block3.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block1.id,
                    text = row2Block1.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block2.id,
                    text = row2Block2.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block3.id,
                    text = row2Block3.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2)
            )
        )

        val columns = listOf(
            BlockView.Table.Column(id = columnId1, background = ThemeColor.DEFAULT),
            BlockView.Table.Column(id = columnId2, background = ThemeColor.DEFAULT),
            BlockView.Table.Column(id = columnId3, background = ThemeColor.DEFAULT)
        )

        val tableId = MockDataFactory.randomUuid()

        val views = listOf<BlockView>(
            BlockView.Table(
                id = tableId,
                cells = cells,
                columns = columns,
                rowCount = 2,
                isSelected = false
            )
        )

        val expectedCells = listOf(
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block1.id,
                    text = row1Block1.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block2.id,
                    text = row1Block2.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(1),
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row1Block3.id,
                    text = row1Block3.content.asText().text
                ),
                rowId = rowId1,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(0),
                columnIndex = BlockView.Table.ColumnIndex(2),
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block1.id,
                    text = row2Block1.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId1,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(0)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block2.id,
                    text = row2Block2.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId2,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(1)
            ),
            BlockView.Table.Cell(
                block = BlockView.Text.Paragraph(
                    id = row2Block3.id,
                    text = row2Block3.content.asText().text
                ),
                rowId = rowId2,
                columnId = columnId3,
                rowIndex = BlockView.Table.RowIndex(1),
                columnIndex = BlockView.Table.ColumnIndex(2)
            )
        )

        val flags = Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
        val escaped = Pattern.quote(query)
        val pattern = Pattern.compile(escaped, flags)

        val highlighted = views.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actual = highlighted.clearSearchHighlights()

        val expected = listOf(
            BlockView.Table(
                id = tableId,
                cells = expectedCells,
                columns = columns,
                rowCount = 2,
                isSelected = false
            )
        )

        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun `when no highlighted text is targeted in simple table expect to target row2 column1 cell`() {

        //SETUP
        val pattern = StubPattern(query = "bc")

        val simpleTableBlock = StubTwoRowsThreeColumnsSimpleTable(
            textR1C1 = "ab1",
            textR1C2 = "ab2",
            textR1C3 = "ac3",
            textR2C1 = "bc1",
            textR2C2 = "bb2",
            textR2C3 = "bc3"
        )

        val cellR1C1 = simpleTableBlock.cells[0]
        val cellR1C2 = simpleTableBlock.cells[1]
        val cellR1C3 = simpleTableBlock.cells[2]
        val cellR2C1 = simpleTableBlock.cells[3]
        val cellR2C2 = simpleTableBlock.cells[4]
        val cellR2C3 = simpleTableBlock.cells[5]

        val blocks = listOf<BlockView>(simpleTableBlock)

        //TESTING
        val actualHighlighted = blocks.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actualFirstHighlightedIsTargeted = actualHighlighted.nextSearchTarget()

        //EXPECTING
        val expectedTargetedCell = cellR2C1.copy(
            block = cellR2C1.block?.copy(
                searchFields = listOf(
                    StubBlockViewSearchFiled(
                        highlights = listOf(IntRange(0, 2)),
                        target = IntRange(0, 2)
                    )
                )
            )
        )

        val expectedHighlightedCell = cellR2C3.copy(
            block = cellR2C3.block?.copy(
                searchFields = listOf(
                    StubBlockViewSearchFiled(
                        highlights = listOf(
                            IntRange(0, 2)
                        )
                    )
                )
            )
        )

        val expectedCells = listOf(
            cellR1C1.copy(
                block = cellR1C1.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C2.copy(
                block = cellR1C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C3.copy(
                block = cellR1C3.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            expectedTargetedCell,
            cellR2C2.copy(
                block = cellR2C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            expectedHighlightedCell
        )

        val expectedFirstHighlightedIsTargeted = listOf(
            simpleTableBlock.copy(cells = expectedCells)
        )

        //ASSERT
        assertEquals(
            expected = expectedFirstHighlightedIsTargeted,
            actual = actualFirstHighlightedIsTargeted
        )
    }

    @Test
    fun `when first highlighted text is targeted in simple table expect to target next one`() {

        //SETUP
        val pattern = StubPattern(query = "bc")

        val simpleTableBlock = StubTwoRowsThreeColumnsSimpleTable(
            textR1C1 = "ab1",
            textR1C2 = "ab2",
            textR1C3 = "ac3",
            textR2C1 = "bc1",
            textR2C2 = "bb2",
            textR2C3 = "bc3"
        )

        val cellR1C1 = simpleTableBlock.cells[0]
        val cellR1C2 = simpleTableBlock.cells[1]
        val cellR1C3 = simpleTableBlock.cells[2]
        val cellR2C1 = simpleTableBlock.cells[3]
        val cellR2C2 = simpleTableBlock.cells[4]
        val cellR2C3 = simpleTableBlock.cells[5]

        val blocks = listOf<BlockView>(simpleTableBlock)

        //TESTING
        val actualHighlighted = blocks.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actualFirstHighlightedIsTargeted = actualHighlighted.nextSearchTarget()

        val actualSecondHighlightedIsTargeted = actualFirstHighlightedIsTargeted.nextSearchTarget()

        //EXPECTING
        val expectedCells = listOf(
            cellR1C1.copy(
                block = cellR1C1.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C2.copy(
                block = cellR1C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C3.copy(
                block = cellR1C3.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C1.copy(
                block = cellR2C1.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2))
                        )
                    )
                )
            ),
            cellR2C2.copy(
                block = cellR2C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C3.copy(
                block = cellR2C3.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange(0, 2)
                        )
                    )
                )
            )
        )

        val expectedSecondHighlightedIsTargeted = listOf(
            simpleTableBlock.copy(
                cells = expectedCells
            )
        )

        //ASSERT
        assertEquals(
            expected = expectedSecondHighlightedIsTargeted,
            actual = actualSecondHighlightedIsTargeted
        )
    }

    @Test
    fun `when second highlighted text is targeted in simple table expect to target second one`() {

        //SETUP
        val pattern = StubPattern(query = "bc")

        val simpleTableBlock = StubTwoRowsThreeColumnsSimpleTable(
            textR1C1 = "ab1",
            textR1C2 = "ab2",
            textR1C3 = "ac3",
            textR2C1 = "bc1",
            textR2C2 = "bb2",
            textR2C3 = "bc3"
        )

        val cellR1C1 = simpleTableBlock.cells[0]
        val cellR1C2 = simpleTableBlock.cells[1]
        val cellR1C3 = simpleTableBlock.cells[2]
        val cellR2C1 = simpleTableBlock.cells[3]
        val cellR2C2 = simpleTableBlock.cells[4]
        val cellR2C3 = simpleTableBlock.cells[5]

        val blocks = listOf<BlockView>(simpleTableBlock)

        //TESTING
        val actualHighlighted = blocks.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actualFirstHighlightedIsTargeted = actualHighlighted.nextSearchTarget()
        val actualSecondHighlightedIsTargeted = actualFirstHighlightedIsTargeted.nextSearchTarget()
        val actualSecondHighlightedIsTargeted2 =
            actualSecondHighlightedIsTargeted.nextSearchTarget()

        //EXPECTING
        val expectedCells = listOf(
            cellR1C1.copy(
                block = cellR1C1.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C2.copy(
                block = cellR1C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C3.copy(
                block = cellR1C3.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C1.copy(
                block = cellR2C1.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2))
                        )
                    )
                )
            ),
            cellR2C2.copy(
                block = cellR2C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C3.copy(
                block = cellR2C3.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange(0, 2)
                        )
                    )
                )
            )
        )

        val expectedSecondHighlightedIsTargeted = listOf(
            simpleTableBlock.copy(
                cells = expectedCells
            )
        )

        //ASSERT
        assertEquals(
            expected = expectedSecondHighlightedIsTargeted,
            actual = actualSecondHighlightedIsTargeted2
        )
    }

    @Test
    fun `when second highlighted text is targeted in simple table expect to target first one`() {

        //SETUP
        val pattern = StubPattern(query = "bc")

        val simpleTableBlock = StubTwoRowsThreeColumnsSimpleTable(
            textR1C1 = "ab1",
            textR1C2 = "ab2",
            textR1C3 = "ac3",
            textR2C1 = "bc1",
            textR2C2 = "bb2",
            textR2C3 = "bc3"
        )

        val cellR1C1 = simpleTableBlock.cells[0]
        val cellR1C2 = simpleTableBlock.cells[1]
        val cellR1C3 = simpleTableBlock.cells[2]
        val cellR2C1 = simpleTableBlock.cells[3]
        val cellR2C2 = simpleTableBlock.cells[4]
        val cellR2C3 = simpleTableBlock.cells[5]

        val blocks = listOf<BlockView>(simpleTableBlock)

        //TESTING
        val actualHighlighted = blocks.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actualFirstHighlightedIsTargeted = actualHighlighted.nextSearchTarget()
        val actualSecondHighlightedIsTargeted = actualFirstHighlightedIsTargeted.nextSearchTarget()
        val actualFirstHighlightedIsTargeted2 =
            actualSecondHighlightedIsTargeted.previousSearchTarget()

        //EXPECTING
        val expectedCells = listOf(
            cellR1C1.copy(
                block = cellR1C1.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C2.copy(
                block = cellR1C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C3.copy(
                block = cellR1C3.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C1.copy(
                block = cellR2C1.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange(0, 2)
                        )
                    )
                )
            ),
            cellR2C2.copy(
                block = cellR2C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C3.copy(
                block = cellR2C3.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2))
                        )
                    )
                )
            )
        )

        val expectedSecondHighlightedIsTargeted = listOf(
            simpleTableBlock.copy(
                cells = expectedCells
            )
        )

        //ASSERT
        assertEquals(
            expected = expectedSecondHighlightedIsTargeted,
            actual = actualFirstHighlightedIsTargeted2
        )
    }

    @Test
    fun `when first highlighted text is targeted in simple table expect to still target first one`() {

        //SETUP
        val pattern = StubPattern(query = "bc")

        val simpleTableBlock = StubTwoRowsThreeColumnsSimpleTable(
            textR1C1 = "ab1",
            textR1C2 = "ab2",
            textR1C3 = "ac3",
            textR2C1 = "bc1",
            textR2C2 = "bb2",
            textR2C3 = "bc3"
        )

        val cellR1C1 = simpleTableBlock.cells[0]
        val cellR1C2 = simpleTableBlock.cells[1]
        val cellR1C3 = simpleTableBlock.cells[2]
        val cellR2C1 = simpleTableBlock.cells[3]
        val cellR2C2 = simpleTableBlock.cells[4]
        val cellR2C3 = simpleTableBlock.cells[5]

        val blocks = listOf<BlockView>(simpleTableBlock)

        //TESTING
        val actualHighlighted = blocks.highlight { pairs ->
            pairs.map { (key, txt) ->
                BlockView.Searchable.Field(
                    key = key,
                    highlights = txt.search(pattern)
                )
            }
        }

        val actualFirstHighlightedIsTargeted = actualHighlighted.nextSearchTarget()
        val actualSecondHighlightedIsTargeted = actualFirstHighlightedIsTargeted.nextSearchTarget()
        val actualFirstHighlightedIsTargeted2 =
            actualSecondHighlightedIsTargeted.previousSearchTarget()
        val actualFirstHighlightedIsTargeted3 =
            actualFirstHighlightedIsTargeted2.previousSearchTarget()

        //EXPECTING
        val expectedCells = listOf(
            cellR1C1.copy(
                block = cellR1C1.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C2.copy(
                block = cellR1C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR1C3.copy(
                block = cellR1C3.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C1.copy(
                block = cellR2C1.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2)),
                            target = IntRange(0, 2)
                        )
                    )
                )
            ),
            cellR2C2.copy(
                block = cellR2C2.block?.copy(searchFields = listOf(StubBlockViewSearchFiled()))
            ),
            cellR2C3.copy(
                block = cellR2C3.block?.copy(
                    searchFields = listOf(
                        StubBlockViewSearchFiled(
                            highlights = listOf(IntRange(0, 2))
                        )
                    )
                )
            )
        )

        val expectedSecondHighlightedIsTargeted = listOf(
            simpleTableBlock.copy(
                cells = expectedCells
            )
        )

        //ASSERT
        assertEquals(
            expected = expectedSecondHighlightedIsTargeted,
            actual = actualFirstHighlightedIsTargeted3
        )
    }
}