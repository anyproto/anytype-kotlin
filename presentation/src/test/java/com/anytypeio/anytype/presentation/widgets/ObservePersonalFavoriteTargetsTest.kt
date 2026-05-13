package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class ObservePersonalFavoriteTargetsTest {

    @Mock
    lateinit var openObject: OpenObject

    @Mock
    lateinit var interceptEvents: InterceptEvents

    private lateinit var payloadDelegator: FakePayloadDelegator
    private lateinit var observe: ObservePersonalFavoriteTargets

    private val space = SpaceId("space-1.context")
    private val docId = personalWidgetsId(space)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        payloadDelegator = FakePayloadDelegator()
        observe = ObservePersonalFavoriteTargets(openObject, interceptEvents, payloadDelegator)
    }

    @Test
    fun `emits ordered real-object targets from initial tree`() = runTest {
        stubOpen(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink("w-1", "l-1", "obj-a"),
                    WrapperLink("w-2", "l-2", "obj-b")
                )
            )
        )
        stubEvents(emptyFlow())

        val result = observe(space).first()

        assertEquals(listOf("obj-a", "obj-b"), result)
    }

    @Test
    fun `filters out built-in targets like favorite and allObjects`() = runTest {
        stubOpen(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink("w-1", "l-1", "favorite"),      // built-in
                    WrapperLink("w-2", "l-2", "obj-real"),      // real
                    WrapperLink("w-3", "l-3", "allObjects")     // built-in
                )
            )
        )
        stubEvents(emptyFlow())

        val result = observe(space).first()

        assertEquals(listOf("obj-real"), result)
    }

    @Test
    fun `returns empty list when no wrapper children`() = runTest {
        stubOpen(objectViewWith(wrapperLinks = emptyList()))
        stubEvents(emptyFlow())

        val result = observe(space).first()

        assertEquals(emptyList(), result)
    }

    /**
     * DROID-4397: the helper itself doesn't swallow exceptions — callers are
     * expected to (and do, in HomeScreenViewModel.favoriteTargets and
     * PersonalFavoritesWidgetContainer.view, both of which wrap with .catch).
     * This test documents that contract: when OpenObject fails, the flow
     * propagates the exception rather than silently emitting, so callers get
     * a clear signal to fall back.
     */
    /**
     * DROID-4397: locally-initiated mutations on the personal-widgets doc come
     * back as events embedded in the RPC response — middleware does NOT also
     * push them on the global event stream. The observer merges PayloadDelegator
     * events with InterceptEvents so VMs can dispatch the response Payload and
     * have the same reducer pipeline pick it up.
     */
    @Test
    fun `merges PayloadDelegator events into the reducer`() = runTest {
        stubOpen(
            objectViewWith(
                wrapperLinks = listOf(
                    WrapperLink("w-1", "l-1", "obj-a"),
                    WrapperLink("w-2", "l-2", "obj-b")
                )
            )
        )
        stubEvents(emptyFlow())

        val emissions = mutableListOf<List<Id>>()
        val job = launch {
            observe(space).collect { emissions += it }
        }
        // Let the initial OpenObject emit before dispatching.
        runCurrent()

        // Simulate the VM dispatching the Payload returned by Rpc.Block.ListDelete:
        // remove "obj-a" by deleting its wrapper and updating root's children.
        payloadDelegator.dispatch(
            Payload(
                context = docId,
                events = listOf(
                    Event.Command.DeleteBlock(context = docId, targets = listOf("w-1", "l-1")),
                    Event.Command.UpdateStructure(
                        context = docId,
                        id = "root",
                        children = listOf("w-2")
                    )
                )
            )
        )
        runCurrent()
        job.cancel()

        // Observer must have re-emitted with obj-a removed by the local payload.
        assertEquals(listOf("obj-a", "obj-b"), emissions.first())
        assertEquals(listOf("obj-b"), emissions.last())
    }

    @Test
    fun `propagates openObject failure so callers can catch`() = runTest {
        val boom = RuntimeException("middleware not ready")
        openObject.stub {
            onBlocking { async(any()) } doReturn Resultat.failure(boom)
        }
        stubEvents(emptyFlow())

        val thrown = kotlin.runCatching { observe(space).first() }.exceptionOrNull()

        assertTrue(thrown === boom, "Expected OpenObject exception to propagate")
    }

    // --- helpers ---

    private suspend fun stubOpen(tree: ObjectView) {
        openObject.stub {
            onBlocking { async(any()) } doReturn Resultat.success(tree)
        }
    }

    private fun stubEvents(events: kotlinx.coroutines.flow.Flow<List<Event>>) {
        interceptEvents.stub {
            on { build(any()) } doReturn events
        }
    }

    /**
     * In-memory PayloadDelegator backed by a SharedFlow. Mirrors the real
     * DefaultPayloadDelegator's filtering by `payload.context`.
     *
     * Note: production [PayloadDelegator.Default] uses `extraBufferCapacity = 0`,
     * so dispatch() suspends until every subscriber consumes the item. Here we
     * give it a small buffer so tests can dispatch from the same coroutine that
     * collects without deadlocking on runCurrent. Production timing differs
     * (subscriber is already attached via viewModelScope before dispatch fires).
     */
    private class FakePayloadDelegator : PayloadDelegator {
        private val shared = MutableSharedFlow<Payload>(replay = 0, extraBufferCapacity = 8)
        override suspend fun dispatch(payload: Payload) {
            shared.emit(payload)
        }
        override fun intercept(ctx: Id): Flow<Payload> = shared.filter { it.context == ctx }
    }

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
            dataViewRestrictions = emptyList<DataViewRestrictions>()
        )
    }
}
