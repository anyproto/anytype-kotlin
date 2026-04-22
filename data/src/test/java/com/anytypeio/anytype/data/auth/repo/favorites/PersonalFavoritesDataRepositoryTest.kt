package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class PersonalFavoritesDataRepositoryTest {

    @Mock
    lateinit var blocks: BlockRepository

    private lateinit var repo: PersonalFavoritesDataRepository

    private val space = SpaceId("space-1.context")
    private val ctx = personalWidgetsId(space)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repo = PersonalFavoritesDataRepository(blocks)
    }

    @Test
    fun `add creates a Link widget at INNER_FIRST in the personal-widgets doc`() = runTest {
        repo.add(space, target = "obj-1")

        verify(blocks).createWidget(
            ctx = ctx,
            source = "obj-1",
            layout = Block.Content.Widget.Layout.LINK,
            target = null,
            position = Position.INNER_FIRST
        )
    }

    @Test
    fun `remove unlinks every inner link whose target matches`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-1", linkId = "l-1", target = "obj-1"),
                    WrapperLink(wrapperId = "w-2", linkId = "l-2", target = "obj-2"),
                    WrapperLink(wrapperId = "w-3", linkId = "l-3", target = "obj-1")
                )
            )
        )

        repo.remove(space, target = "obj-1")

        val captor = argumentCaptor<Command.Unlink>()
        verify(blocks).unlink(captor.capture())
        val captured = captor.firstValue
        assertEquals(ctx, captured.context)
        assertEquals(listOf("l-1", "l-3"), captured.targets)
    }

    @Test
    fun `remove is a no-op when target is not present`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-1", linkId = "l-1", target = "obj-1")
                )
            )
        )

        repo.remove(space, target = "missing")

        verify(blocks).openObject(ctx, space)
        verify(blocks, never()).unlink(any())
    }

    @Test
    fun `reorder is a no-op when list is empty`() = runTest {
        repo.reorder(space, orderedTargets = emptyList())
        verifyNoInteractions(blocks)
    }

    @Test
    fun `reorder moves each wrapper to end of root in order`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                root = "root-1",
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-a", linkId = "l-a", target = "obj-a"),
                    WrapperLink(wrapperId = "w-b", linkId = "l-b", target = "obj-b"),
                    WrapperLink(wrapperId = "w-c", linkId = "l-c", target = "obj-c")
                )
            )
        )

        repo.reorder(space, orderedTargets = listOf("obj-c", "obj-a"))

        val captor = argumentCaptor<Command.Move>()
        verify(blocks, times(2)).move(captor.capture())
        val calls = captor.allValues
        assertEquals(listOf("w-c"), calls[0].blockIds)
        assertEquals("root-1", calls[0].targetId)
        assertEquals(Position.INNER, calls[0].position)
        assertEquals(ctx, calls[0].ctx)
        assertEquals(ctx, calls[0].targetContextId)
        assertEquals(listOf("w-a"), calls[1].blockIds)
        assertEquals("root-1", calls[1].targetId)
    }

    @Test
    fun `reorder skips targets that are not present`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                root = "root-1",
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-a", linkId = "l-a", target = "obj-a")
                )
            )
        )

        repo.reorder(space, orderedTargets = listOf("missing", "obj-a"))

        val captor = argumentCaptor<Command.Move>()
        verify(blocks, times(1)).move(captor.capture())
        assertEquals(listOf("w-a"), captor.firstValue.blockIds)
    }

    // --- helpers ---

    private data class WrapperLink(val wrapperId: String, val linkId: String, val target: String)

    private fun objectViewWith(
        root: String = "root",
        wrapperLinks: List<WrapperLink>
    ): ObjectView {
        val rootBlock = Block(
            id = root,
            children = wrapperLinks.map { it.wrapperId },
            content = Block.Content.Smart,
            fields = Block.Fields(emptyMap())
        )
        val wrappers = wrapperLinks.map { wl ->
            Block(
                id = wl.wrapperId,
                children = listOf(wl.linkId),
                content = Block.Content.Widget(
                    layout = Block.Content.Widget.Layout.LINK
                ),
                fields = Block.Fields(emptyMap())
            )
        }
        val links = wrapperLinks.map { wl ->
            Block(
                id = wl.linkId,
                children = emptyList(),
                content = Block.Content.Link(
                    target = wl.target,
                    type = Block.Content.Link.Type.PAGE,
                    iconSize = Block.Content.Link.IconSize.NONE,
                    cardStyle = Block.Content.Link.CardStyle.TEXT,
                    description = Block.Content.Link.Description.NONE
                ),
                fields = Block.Fields(emptyMap())
            )
        }
        return ObjectView(
            root = root,
            blocks = listOf(rootBlock) + wrappers + links,
            details = emptyMap(),
            objectRestrictions = emptyList(),
            dataViewRestrictions = emptyList()
        )
    }
}
