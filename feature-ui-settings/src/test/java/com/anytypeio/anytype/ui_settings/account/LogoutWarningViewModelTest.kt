package com.anytypeio.anytype.ui_settings.account

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.ui_settings.account.LogoutWarningViewModel.Command
import com.anytypeio.anytype.ui_settings.util.DefaultCoroutineTestRule
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class LogoutWarningViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var logout: Logout
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var appActionManager: AppActionManager
    @Mock lateinit var globalSubscriptionManager: GlobalSubscriptionManager


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {

    }

    @Test
    fun `should cleanup and emit correct command when logout success`() = runTest {

        whenever(logout.invoke(any())).thenReturn(
            flowOf(Interactor.Status.Success)
        )

        val vm = createViewModel()

        vm.onLogoutClicked()

        vm.commands.test {
            assertEquals(Command.Logout,awaitItem())
        }

        advanceUntilIdle()



        verifyBlocking(logout, times(1)) { invoke(any()) }
        verifyBlocking(analytics, times(1)) {
            registerEvent(any())
        }
        verify(appActionManager).setup(any<AppActionManager.Action>())
        verify(globalSubscriptionManager).onStop()
    }

    @Test
    fun `loggingOut should be true when interactor status is started`() = runTest {
        whenever(logout.invoke(any())).thenReturn(
            flowOf(Interactor.Status.Started)
        )

        val vm = createViewModel()

        vm.onLogoutClicked()

        vm.isLoggingOut.test {
            val first  = awaitItem()
            assertFalse(first)

            val second = awaitItem()
            assertTrue(second)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `command emits show error when logout fails`() = runTest {
        val error = Exception("Test Error")
        whenever(logout.invoke(any())).thenReturn(
            flowOf(Interactor.Status.Error(error))
        )

        val vm = createViewModel()

        vm.onLogoutClicked()

        vm.commands.test {
            assertEquals(Command.ShowError(error.message ?: ""),awaitItem())
        }
    }


    private fun createViewModel(): LogoutWarningViewModel {
        return LogoutWarningViewModel(
            logout = logout,
            analytics = analytics,
            appActionManager = appActionManager,
            globalSubscriptionManager = globalSubscriptionManager
        )
    }

}