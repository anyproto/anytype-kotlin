package com.anytypeio.anytype.feature_os_widgets.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.persistence.DataViewItemsFetcher
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewItemEntity
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectItemView
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DataViewWidgetConfigViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val appWidgetId = 73
    private val selectedSpace = StubSpaceView(targetSpaceId = "space-1")

    private val spaceViews: SpaceViewSubscriptionContainer = mock()
    private val urlBuilder: UrlBuilder = mock()
    private val searchObjects: SearchObjects = mock()
    private val getObject: GetObject = mock()
    private val dataStore: DataViewWidgetConfigStore = mock()
    private val itemsFetcher: DataViewItemsFetcher = mock()
    private val widgetUpdater: DataViewWidgetUpdater = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(spaceViews.get()).thenReturn(listOf(selectedSpace))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onViewerSelected emits success and saves config`() = runTest {
        val objectItem = ObjectItemView(
            obj = StubObject(id = "obj-1", name = "My Set"),
            icon = ObjectIcon.None,
            typeName = ""
        )
        val objectView = objectViewWithViewer(objectId = objectItem.obj.id, viewerId = "viewer-1")
        whenever(getObject.run(any())).thenReturn(objectView)
        whenever(itemsFetcher.fetchItems(any(), any(), any(), any(), any())).thenReturn(
            listOf(OsWidgetDataViewItemEntity(id = "item-1", name = "Record", typeName = "Page"))
        )

        val vm = createViewModel()
        vm.onSpaceSelected(selectedSpace)
        vm.onObjectSelected(objectItem)
        advanceUntilIdle()
        val viewer = vm.viewers.value.first()

        vm.commands.test {
            vm.onViewerSelected(viewer)
            advanceUntilIdle()

            assertEquals(
                DataViewWidgetConfigViewModel.Command.FinishWithSuccess(appWidgetId),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }

        verify(dataStore).save(any())
        verify(widgetUpdater).update(eq(appWidgetId))
    }

    @Test
    fun `onViewerSelected emits error when items fetch fails`() = runTest {
        val objectItem = ObjectItemView(
            obj = StubObject(id = "obj-1", name = "My Set"),
            icon = ObjectIcon.None,
            typeName = ""
        )
        val objectView = objectViewWithViewer(objectId = objectItem.obj.id, viewerId = "viewer-1")
        whenever(getObject.run(any())).thenReturn(objectView)
        whenever(itemsFetcher.fetchItems(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("fetch failed"))

        val vm = createViewModel()
        vm.onSpaceSelected(selectedSpace)
        vm.onObjectSelected(objectItem)
        advanceUntilIdle()
        val viewer = vm.viewers.value.first()

        vm.commands.test {
            vm.onViewerSelected(viewer)
            advanceUntilIdle()

            assertEquals(
                DataViewWidgetConfigViewModel.Command.ShowError("fetch failed"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }

        verify(dataStore, never()).save(any())
    }

    private fun objectViewWithViewer(objectId: String, viewerId: String): ObjectView {
        val viewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "All",
            type = Block.Content.DataView.Viewer.Type.LIST,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList()
        )
        val dataView = Block.Content.DataView(viewers = listOf(viewer))
        return StubObjectView(
            root = objectId,
            blocks = listOf(
                Block(
                    id = "dv-block",
                    children = emptyList(),
                    content = dataView,
                    fields = Block.Fields.empty()
                )
            )
        )
    }

    private fun createViewModel(): DataViewWidgetConfigViewModel {
        return DataViewWidgetConfigViewModel(
            appWidgetId = appWidgetId,
            spaceViews = spaceViews,
            urlBuilder = urlBuilder,
            searchObjects = searchObjects,
            getObject = getObject,
            dataStore = dataStore,
            itemsFetcher = itemsFetcher,
            widgetUpdater = widgetUpdater
        )
    }
}
