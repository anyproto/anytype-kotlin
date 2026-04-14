package com.anytypeio.anytype.feature_os_widgets.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectItemView
import com.anytypeio.anytype.core_models.UrlBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
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
    private val launchWallet: LaunchWallet = mock()
    private val launchAccount: LaunchAccount = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(spaceViews.get()).thenReturn(listOf(selectedSpace))
        whenever(spaceViews.observe()).thenReturn(flowOf(listOf(selectedSpace)))
        launchWallet.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
        launchAccount.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Pair("accountId", ""))
        }
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
    fun `cold start shows loading until spaces arrive asynchronously`() = runTest {
        // Simulate the container's real behavior: starts with emptyList,
        // then publishes real data only after middleware resolves subscription.
        val spacesFlow = MutableStateFlow<List<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>>(emptyList())
        whenever(spaceViews.observe()).thenReturn(spacesFlow)

        val vm = createViewModel()
        // Advance inside the warmup window but not past it.
        advanceTimeBy(500)

        // While subscription still holds the initial empty list, the VM should NOT
        // have flipped spaces out of the null (loading) state.
        assertNull(vm.spaces.value)

        // Now subscription resolves with the real list.
        spacesFlow.value = listOf(selectedSpace)
        advanceUntilIdle()

        assertEquals(listOf(selectedSpace), vm.spaces.value)
    }

    @Test
    fun `cold start falls back to empty list after warmup timeout`() = runTest {
        // Subscription never emits anything beyond the initial empty list.
        val spacesFlow = MutableStateFlow<List<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>>(emptyList())
        whenever(spaceViews.observe()).thenReturn(spacesFlow)

        val vm = createViewModel()
        // Fast-forward past the warmup timeout (2s) plus a little margin.
        advanceTimeBy(3_000)
        advanceUntilIdle()

        // After timeout we fall back to collecting the current value.
        assertEquals(emptyList<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>(), vm.spaces.value)
    }

    @Test
    fun `middleware failure emits FinishWithFailure and does not collect spaces`() = runTest {
        launchAccount.stub {
            onBlocking { invoke(any()) } doReturn Either.Left(RuntimeException("account launch failed"))
        }

        val vm = createViewModel()

        vm.commands.test {
            advanceUntilIdle()
            val cmd = awaitItem()
            assert(cmd is ObjectShortcutWidgetConfigViewModel.Command.FinishWithFailure) {
                "Expected FinishWithFailure but was $cmd"
            }
            cancelAndIgnoreRemainingEvents()
        }

        // Spaces never got collected because we returned early.
        assertNull(vm.spaces.value)
        verify(spaceViews, never()).observe()
    }

    @Test
    fun `wallet failure emits FinishWithFailure`() = runTest {
        launchWallet.stub {
            onBlocking { invoke(any()) } doReturn Either.Left(RuntimeException("wallet launch failed"))
        }

        val vm = createViewModel()

        vm.commands.test {
            advanceUntilIdle()
            val cmd = awaitItem()
            assert(cmd is ObjectShortcutWidgetConfigViewModel.Command.FinishWithFailure) {
                "Expected FinishWithFailure but was $cmd"
            }
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(vm.spaces.value)
        verify(spaceViews, never()).observe()
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
            widgetUpdater = widgetUpdater,
            launchWallet = launchWallet,
            launchAccount = launchAccount
        )
    }
}
