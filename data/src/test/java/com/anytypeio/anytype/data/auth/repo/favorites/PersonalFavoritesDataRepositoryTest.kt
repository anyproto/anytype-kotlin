package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
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
    fun `add creates a Link widget at INNER_FIRST and returns the response Payload`() = runTest {
        val expected = payload("e-add")
        given(
            blocks.createWidget(
                ctx = eq(ctx),
                source = eq("obj-1"),
                layout = eq(Block.Content.Widget.Layout.LINK),
                target = eq(null),
                position = eq(Position.INNER_FIRST)
            )
        ).willReturn(expected)

        val actual = repo.add(space, target = "obj-1")

        // The Payload returned by the underlying createWidget RPC must be passed
        // through verbatim — VMs dispatch it through PayloadDelegator so the
        // personal-widgets observer sees the same events MW would have pushed.
        assertSame(expected, actual)
    }

    @Test
    fun `remove unlinks every inner link whose target matches and returns Payload`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-1", linkId = "l-1", target = "obj-1"),
                    WrapperLink(wrapperId = "w-2", linkId = "l-2", target = "obj-2"),
                    WrapperLink(wrapperId = "w-3", linkId = "l-3", target = "obj-1")
                )
            )
        )
        val expected = payload("e-remove")
        given(blocks.unlink(any())).willReturn(expected)

        val actual = repo.remove(space, target = "obj-1")

        val captor = argumentCaptor<Command.Unlink>()
        verify(blocks).unlink(captor.capture())
        val captured = captor.firstValue
        assertEquals(ctx, captured.context)
        // Per anytype-heart GO-6962: ListDelete on the personal-widgets doc takes
        // the INNER LINK block IDs, and the middleware cascades the wrapper
        // deletion internally. Passing wrapper IDs is not the documented contract.
        assertEquals(listOf("l-1", "l-3"), captured.targets)
        // The returned Payload is what the VM dispatches into PayloadDelegator —
        // dropping it here is exactly the bug the v2 fix addresses.
        assertSame(expected, actual)
    }

    @Test
    fun `remove returns null and skips RPC when target is not present`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-1", linkId = "l-1", target = "obj-1")
                )
            )
        )

        val actual = repo.remove(space, target = "missing")

        verify(blocks).openObject(ctx, space)
        verify(blocks, never()).unlink(any())
        // Null distinguishes "no MW mutation happened" from "mutation succeeded";
        // the VM uses this to skip dispatching a meaningless Payload.
        assertNull(actual)
    }

    @Test
    fun `reorder is a no-op and returns empty list when input is empty`() = runTest {
        val actual = repo.reorder(space, orderedTargets = emptyList())
        verifyNoInteractions(blocks)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun `reorder is a no-op and returns empty list when input has a single item`() = runTest {
        val actual = repo.reorder(space, orderedTargets = listOf("obj-a"))
        // Nothing to move relative to — no RPC should fire.
        verifyNoInteractions(blocks)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun `reorder anchors each wrapper BOTTOM of previous and returns Payload list`() = runTest {
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
        val p1 = payload("e-move-1")
        val p2 = payload("e-move-2")
        given(blocks.move(any())).willReturn(p1, p2)

        val actual = repo.reorder(space, orderedTargets = listOf("obj-c", "obj-a", "obj-b"))

        val captor = argumentCaptor<Command.Move>()
        verify(blocks, times(2)).move(captor.capture())
        val calls = captor.allValues
        // First: move w-a to BOTTOM of w-c → children become [..., w-c, w-a, ...]
        assertEquals(listOf("w-a"), calls[0].blockIds)
        assertEquals("w-c", calls[0].targetId)
        assertEquals(Position.BOTTOM, calls[0].position)
        assertEquals(ctx, calls[0].ctx)
        assertEquals(ctx, calls[0].targetContextId)
        // Second: move w-b to BOTTOM of w-a → children become [..., w-c, w-a, w-b]
        assertEquals(listOf("w-b"), calls[1].blockIds)
        assertEquals("w-a", calls[1].targetId)
        assertEquals(Position.BOTTOM, calls[1].position)
        // One Payload per move RPC, in dispatch order.
        assertEquals(listOf(p1, p2), actual)
    }

    @Test
    fun `reorder skips targets that are not present`() = runTest {
        given(blocks.openObject(ctx, space)).willReturn(
            objectViewWith(
                root = "root-1",
                wrapperLinks = listOf(
                    WrapperLink(wrapperId = "w-a", linkId = "l-a", target = "obj-a"),
                    WrapperLink(wrapperId = "w-b", linkId = "l-b", target = "obj-b")
                )
            )
        )
        val p1 = payload("e-move-1")
        given(blocks.move(any())).willReturn(p1)

        val actual = repo.reorder(space, orderedTargets = listOf("missing", "obj-b", "obj-a"))

        val captor = argumentCaptor<Command.Move>()
        // "missing" is filtered out → wrappers list = [w-b, w-a] → one move RPC.
        verify(blocks, times(1)).move(captor.capture())
        assertEquals(listOf("w-a"), captor.firstValue.blockIds)
        assertEquals("w-b", captor.firstValue.targetId)
        assertEquals(Position.BOTTOM, captor.firstValue.position)
        assertEquals(listOf(p1), actual)
    }

    // --- helpers ---

    private data class WrapperLink(val wrapperId: String, val linkId: String, val target: String)

    private fun payload(marker: String): Payload =
        Payload(
            context = ctx,
            events = listOf(Event.Command.DeleteBlock(context = ctx, targets = listOf(marker)))
        )

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
