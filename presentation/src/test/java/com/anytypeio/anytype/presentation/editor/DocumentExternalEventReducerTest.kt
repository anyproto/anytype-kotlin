package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Link.IconSize
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubBookmark
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class DocumentExternalEventReducerTest {

    private val reducer = DocumentExternalEventReducer()

    @Test
    fun `should apply bookmark granular changes to the state`() {

        // SETUP

        val title = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            )
        )

        val bookmark = StubBookmark()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(title.id, bookmark.id),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val state = listOf(page, title, bookmark)

        // TESTING

        runBlocking {

            val bookmarkUrl = MockDataFactory.randomString()

            val expected = listOf(
                page, title, bookmark.copy(
                    content = bookmark.content<Block.Content.Bookmark>().copy(
                        url = bookmarkUrl
                    )
                )
            )

            val result = reducer.reduce(
                state = state,
                event = Event.Command.BookmarkGranularChange(
                    url = bookmarkUrl,
                    context = page.id,
                    target = bookmark.id,
                    title = null,
                    description = null,
                    favicon = null,
                    image = null
                )
            )

            assertEquals(expected = expected, actual = result)
        }

        runBlocking {

            val bookmarkUrl = MockDataFactory.randomString()
            val bookmarkTitle = MockDataFactory.randomString()
            val bookmarkDescription = MockDataFactory.randomString()

            val expected = listOf(
                page, title, bookmark.copy(
                    content = bookmark.content<Block.Content.Bookmark>().copy(
                        url = bookmarkUrl,
                        title = bookmarkTitle,
                        description = bookmarkDescription
                    )
                )
            )

            val result = reducer.reduce(
                state = state,
                event = Event.Command.BookmarkGranularChange(
                    url = bookmarkUrl,
                    context = page.id,
                    target = bookmark.id,
                    title = bookmarkTitle,
                    description = bookmarkDescription,
                    favicon = null,
                    image = null
                )
            )

            assertEquals(expected = expected, actual = result)
        }

        runBlocking {

            val bookmarkUrl = MockDataFactory.randomString()
            val bookmarkTitle = MockDataFactory.randomString()
            val bookmarkDescription = MockDataFactory.randomString()
            val imageHash = MockDataFactory.randomString()
            val faviconHash = MockDataFactory.randomString()

            val expected = listOf(
                page, title, bookmark.copy(
                    content = bookmark.content<Block.Content.Bookmark>().copy(
                        url = bookmarkUrl,
                        title = bookmarkTitle,
                        description = bookmarkDescription,
                        image = imageHash,
                        favicon = faviconHash
                    )
                )
            )

            val result = reducer.reduce(
                state = state,
                event = Event.Command.BookmarkGranularChange(
                    url = bookmarkUrl,
                    context = page.id,
                    target = bookmark.id,
                    title = bookmarkTitle,
                    description = bookmarkDescription,
                    favicon = faviconHash,
                    image = imageHash
                )
            )

            assertEquals(expected = expected, actual = result)
        }
    }

    @Test
    fun `should not apply bookmark granular changes if there is not bookmark block matched by id`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            )
        )

        val bookmark = StubBookmark()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(title.id, bookmark.id),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val state = listOf(page, title, bookmark)

        // TESTING

        runBlocking {

            val bookmarkUrl = MockDataFactory.randomString()

            val expected = state.toList()

            val result = reducer.reduce(
                state = state,
                event = Event.Command.BookmarkGranularChange(
                    url = bookmarkUrl,
                    context = page.id,
                    target = MockDataFactory.randomString(),
                    title = null,
                    description = null,
                    favicon = null,
                    image = null
                )
            )

            assertEquals(expected = expected, actual = result)
        }
    }

    @Test
    fun `should update link appearance`() {
        val title = MockBlockFactory.title()
        val link = MockBlockFactory.link(
            content = StubLinkContent(
                iconSize = IconSize.NONE
            )
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(title.id, link.id),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val state = listOf(page, title, link)

        // TESTING

        val updated = link.content.asLink().copy(
            iconSize = IconSize.MEDIUM
        )

        runBlocking {

            val expected = listOf(
                page,
                title,
                link.copy(
                    content = updated
                )
            )

            val result = reducer.reduce(
                state = state,
                event = Event.Command.LinkGranularChange(
                    context = page.id,
                    id = link.id,
                    target = link.content<Block.Content.Link>().target,
                    iconSize = IconSize.MEDIUM,
                    cardStyle = null,
                    description = null,
                    relations = null
                )
            )

            assertEquals(expected = expected, actual = result)
        }
    }
}