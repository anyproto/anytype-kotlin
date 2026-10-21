package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetSubscriptionIdentityTest : ObjectSetViewModelTestSetup() {
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        dataViewSubscription = mock()
        val viewers = listOf("a", "b").map { id ->
            DVViewer(id = id, name = id, type = DVViewerType.LIST,
                sorts = emptyList(), filters = emptyList(), viewerRelations = emptyList())
        }
        stubOpenObject(
            doc = listOf(StubTitle(), StubDataView(views = viewers, isCollection = true)),
            details = ObjectViewDetails(mapOf(root to mapOf(
                Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
            )))
        )
    }

    @Test
    fun `delayed viewer response never exposes previous viewer IDs under the new viewer identity`() = runTest {
        objectStore.merge(
            objects = listOf(StubObject(id = "a-record"), StubObject(id = "b-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test")
        )
        val delayedB = CompletableDeferred<DataViewState>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            if (invocation.getArgument<String?>(4) == "b") flow<DataViewState> { emit(delayedB.await()) }
            else flowOf<DataViewState>(DataViewState.Loaded(objects = listOf("a-record")))
        }
        val vm = givenViewModel()
        vm.onStart(view = "a")
        advanceUntilIdle()
        assertEquals("a", (vm.currentViewer.value as DataViewViewState.Collection.Default).viewer?.id)

        session.currentViewerId.value = "b"
        advanceUntilIdle()
        // This was previously Viewer(id=b, items=[a-record]), causing B's saved anchor
        // to be considered deleted before its subscription had actually returned.
        val whileLoading = (vm.currentViewer.value as DataViewViewState.Collection.Default).viewer as Viewer.ListView
        assertEquals("a", whileLoading.id)
        assertEquals(listOf("a-record"), whileLoading.items.map { it.objectId })

        delayedB.complete(DataViewState.Loaded(objects = listOf("b-anchor")))
        advanceUntilIdle()
        val loaded = (vm.currentViewer.value as DataViewViewState.Collection.Default).viewer as Viewer.ListView
        assertEquals("b", loaded.id)
        assertEquals(listOf("b-anchor"), loaded.items.map { it.objectId })
        vm.onStop()
    }

    @Test
    fun `restored paginator selection is used by the first record subscription`() = runTest {
        objectStore.merge(objects = listOf(StubObject(id = "page-three-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test"))
        val offsets = mutableListOf<Long>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            offsets += invocation.getArgument<Long>(5)
            flowOf(DataViewState.Loaded(objects = listOf("page-three-anchor")))
        }
        val vm = givenViewModel()
        // Same ordering used by ObjectSetFragment when its saved Bundle contains page 2.
        vm.onPaginatorToolbarNumberClicked(number = 2, isSelected = false)
        vm.onStart(view = "a")
        advanceUntilIdle()
        assertEquals(listOf(2L * ObjectSetConfig.DEFAULT_LIMIT), offsets)
        val viewer = (vm.currentViewer.value as DataViewViewState.Collection.Default).viewer as Viewer.ListView
        assertEquals(listOf("page-three-anchor"), viewer.items.map { it.objectId })
        vm.onStop()
    }

    @Test
    fun `viewer switch restores its own saved page before the next subscription`() = runTest {
        objectStore.merge(objects = listOf(StubObject(id = "a-anchor"), StubObject(id = "b-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test"))
        val requests = mutableListOf<Pair<String?, Long>>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            val viewer = invocation.getArgument<String?>(4)
            val offset = invocation.getArgument<Long>(5)
            requests += viewer to offset
            flowOf<DataViewState>(DataViewState.Loaded(objects = listOf("$viewer-anchor")))
        }
        whenever(viewerDelegate.onEvent(any())).thenAnswer { invocation ->
            val event = invocation.getArgument<ViewerEvent>(0)
            if (event is ViewerEvent.SetActive) session.currentViewerId.value = event.viewer
            Unit
        }
        val vm = givenViewModel()
        vm.onPaginatorToolbarNumberClicked(number = 2, isSelected = false)
        vm.onStart(view = "a")
        advanceUntilIdle()
        vm.onViewersWidgetAction(ViewersWidgetUi.Action.SetActive("b", DVViewerType.LIST), restoredPage = 3)
        advanceUntilIdle()
        vm.onViewersWidgetAction(ViewersWidgetUi.Action.SetActive("a", DVViewerType.LIST), restoredPage = 2)
        advanceUntilIdle()
        assertEquals(listOf<Pair<String?, Long>>("a" to 2L * ObjectSetConfig.DEFAULT_LIMIT,
            "b" to 3L * ObjectSetConfig.DEFAULT_LIMIT, "a" to 2L * ObjectSetConfig.DEFAULT_LIMIT), requests)
        assertEquals(2, vm.selectedPageIndex)
        val restored = (vm.currentViewer.value as DataViewViewState.Collection.Default).viewer as Viewer.ListView
        assertEquals("a", restored.id)
        assertEquals(listOf("a-anchor"), restored.items.map { it.objectId })
        vm.onStop()
    }

    @Test
    fun `recreated collector rejects displayed A while requested B page is pending`() = runTest {
        objectStore.merge(objects = listOf(StubObject(id = "a-anchor"), StubObject(id = "b-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test"))
        val delayedB = CompletableDeferred<DataViewState>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            if (invocation.getArgument<String?>(4) == "b") flow<DataViewState> { emit(delayedB.await()) }
            else flowOf<DataViewState>(DataViewState.Loaded(objects = listOf("a-anchor")))
        }
        val vm = givenViewModel()
        vm.onPaginatorToolbarNumberClicked(number = 2, isSelected = false)
        vm.onStart(view = "a")
        advanceUntilIdle()
        val displayedA = vm.viewerContent.value
        assertEquals(2, vm.renderedPageIndex(displayedA))

        vm.onPaginatorToolbarNumberClicked(number = 3, isSelected = false)
        session.currentViewerId.value = "b"
        advanceUntilIdle()
        vm.onStop()
        advanceUntilIdle()
        // The requested identity is available before combine's collector has even started.
        val requestedViewer = vm.selectedViewerId
        assertEquals("b", requestedViewer)
        assertEquals(3, vm.selectedPageIndex)
        val replay = vm.viewerContent.first()
        assertEquals("a", replay.viewerId)
        assertNull(vm.renderedPageIndex(replay))
        assertEquals(2, replay.pageIndex) // Never relabel A's anchor with B's requested page.
        vm.onStart(view = requestedViewer)
        advanceUntilIdle()
        assertEquals("b", session.currentViewerId.value)
        delayedB.complete(DataViewState.Loaded(objects = listOf("b-anchor")))
        advanceUntilIdle()
        assertEquals("b", vm.viewerContent.value.viewerId)
        assertEquals(3, vm.renderedPageIndex(vm.viewerContent.value))
        assertNull(vm.renderedPageIndex(displayedA))
        vm.onStop()
    }

    @Test
    fun `recreated collector rejects old page of the same viewer until its result commits`() = runTest {
        objectStore.merge(objects = listOf(StubObject(id = "old-anchor"), StubObject(id = "new-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test"))
        val delayedPage = CompletableDeferred<DataViewState>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            if (invocation.getArgument<Long>(5) == 3L * ObjectSetConfig.DEFAULT_LIMIT)
                flow<DataViewState> { emit(delayedPage.await()) }
            else flowOf<DataViewState>(DataViewState.Loaded(objects = listOf("old-anchor")))
        }
        val vm = givenViewModel()
        vm.onPaginatorToolbarNumberClicked(number = 2, isSelected = false)
        vm.onStart(view = "a")
        advanceUntilIdle()
        vm.onPaginatorToolbarNumberClicked(number = 3, isSelected = false)
        advanceUntilIdle()
        val replay = vm.viewerContent.first()
        assertEquals("a", replay.viewerId)
        assertEquals(2, replay.pageIndex)
        assertNull(vm.renderedPageIndex(replay))
        delayedPage.complete(DataViewState.Loaded(objects = listOf("new-anchor")))
        advanceUntilIdle()
        assertEquals(3, vm.renderedPageIndex(vm.viewerContent.value))
        vm.onStop()
    }

    @Test
    fun `equal visible content on another page still publishes its authoritative page`() = runTest {
        objectStore.merge(objects = listOf(StubObject(id = "shared-anchor")),
            dependencies = emptyList(), subscriptions = listOf("test"))
        val delayedPage = CompletableDeferred<DataViewState>()
        whenever(dataViewSubscription.startObjectCollectionSubscription(
            any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer { invocation ->
            if (invocation.getArgument<Long>(5) == 3L * ObjectSetConfig.DEFAULT_LIMIT)
                flow<DataViewState> { emit(delayedPage.await()) }
            else flowOf<DataViewState>(DataViewState.Loaded(objects = listOf("shared-anchor")))
        }
        val vm = givenViewModel()
        vm.onPaginatorToolbarNumberClicked(number = 2, isSelected = false)
        vm.onStart(view = "a")
        advanceUntilIdle()
        val original = vm.viewerContent.first()
        vm.onPaginatorToolbarNumberClicked(number = 3, isSelected = false)
        advanceUntilIdle()
        assertNull(vm.renderedPageIndex(vm.viewerContent.first()))
        delayedPage.complete(DataViewState.Loaded(objects = listOf("shared-anchor")))
        advanceUntilIdle()
        val committed = vm.viewerContent.first()
        assertEquals(original.state, committed.state)
        assertNotSame(original, committed)
        assertEquals(3, vm.renderedPageIndex(committed))
        assertNull(vm.renderedPageIndex(original))
        vm.onStop()
    }
}
