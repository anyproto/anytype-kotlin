package com.anytypeio.anytype.feature_os_widgets.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectItemView
import com.anytypeio.anytype.core_models.UrlBuilder
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
class ObjectShortcutWidgetConfigViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val appWidgetId = 42
    private val selectedSpace = StubSpaceView(targetSpaceId = "space-1")

    private val spaceViews: SpaceViewSubscriptionContainer = mock()
    private val urlBuilder: UrlBuilder = mock()
    private val searchObjects: SearchObjects = mock()
    private val configStore: ObjectShortcutWidgetConfigStore = mock()
    private val iconCache: ObjectShortcutIconCache = mock()
    private val widgetUpdater: ObjectShortcutWidgetUpdater = mock()

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
    fun `onObjectSelected emits success and saves config`() = runTest {
        val vm = createViewModel()
        val item = ObjectItemView(
            obj = StubObject(id = "obj-1", name = "Object Name"),
            icon = ObjectIcon.None,
            typeName = ""
        )

        vm.commands.test {
            vm.onSpaceSelected(selectedSpace)
            vm.onObjectSelected(item)
            advanceUntilIdle()

            assertEquals(
                ObjectShortcutWidgetConfigViewModel.Command.FinishWithSuccess(appWidgetId),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }

        verify(configStore).save(any())
        verify(widgetUpdater).update(eq(appWidgetId))
    }

    @Test
    fun `onObjectSelected emits error when save fails`() = runTest {
        whenever(configStore.save(any())).thenThrow(RuntimeException("save failed"))

        val vm = createViewModel()
        val item = ObjectItemView(
            obj = StubObject(id = "obj-1", name = "Object Name"),
            icon = ObjectIcon.None,
            typeName = ""
        )

        vm.commands.test {
            vm.onSpaceSelected(selectedSpace)
            vm.onObjectSelected(item)
            advanceUntilIdle()

            assertEquals(
                ObjectShortcutWidgetConfigViewModel.Command.ShowError("save failed"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }

        verify(widgetUpdater, never()).update(any())
    }

    private fun createViewModel(): ObjectShortcutWidgetConfigViewModel {
        return ObjectShortcutWidgetConfigViewModel(
            appWidgetId = appWidgetId,
            spaceViews = spaceViews,
            urlBuilder = urlBuilder,
            searchObjects = searchObjects,
            configStore = configStore,
            iconCache = iconCache,
            widgetUpdater = widgetUpdater
        )
    }
}
