package com.anytypeio.anytype.presentation.page

import MockDataFactory
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Block.Fields.Companion.NAME_KEY
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.ext.content
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

        val bookmark = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Bookmark(
                url = null,
                description = null,
                title = null,
                favicon = null,
                image = null
            )
        )

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

        val bookmark = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Bookmark(
                url = null,
                description = null,
                title = null,
                favicon = null,
                image = null
            )
        )

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
    fun `should update link fields`() {

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

        val name = MockDataFactory.randomString()

        val link = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                fields = Block.Fields(
                    map = mapOf(NAME_KEY to name)
                ),
                target = MockDataFactory.randomUuid(),
                type = Block.Content.Link.Type.PAGE
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

        val updated = Block.Fields(
            map = mapOf(NAME_KEY to MockDataFactory.randomString())
        )

        runBlocking {

            val expected = listOf(
                page,
                title,
                link.copy(
                    content = link.content<Block.Content.Link>().copy(
                        fields = updated
                    )
                )
            )

            val result = reducer.reduce(
                state = state,
                event = Event.Command.LinkGranularChange(
                    context = page.id,
                    id = link.id,
                    target = link.content<Block.Content.Link>().target,
                    fields = updated
                )
            )

            assertEquals(expected = expected, actual = result)
        }
    }
}