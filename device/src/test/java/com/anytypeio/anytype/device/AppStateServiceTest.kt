package com.anytypeio.anytype.device

import androidx.lifecycle.LifecycleOwner
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.SetAppState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import com.anytypeio.anytype.domain.base.Resultat
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking

/**
 * The last-backgrounded stamp is what expires the "restore into the last opened
 * space" route (DROID-4590). It is written here because this is the app's only
 * ProcessLifecycleOwner observer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppStateServiceTest {

    private val owner: LifecycleOwner = mock()

    @Test
    fun `stamps the time the app went to background`() = runTest {
        val settings: UserSettingsRepository = mock()
        val scope = TestScope(StandardTestDispatcher(testScheduler))
        val service = AppStateService(
            setAppState = stubbedSetAppState(),
            coroutineScope = scope,
            settings = settings,
            clock = { NOW }
        )

        service.onStop(owner)
        advanceUntilIdle()

        verifyBlocking(settings) { setLastBackgroundedAt(NOW) }
    }

    @Test
    fun `does not stamp when the app comes to foreground`() = runTest {
        val settings: UserSettingsRepository = mock()
        val scope = TestScope(StandardTestDispatcher(testScheduler))
        val service = AppStateService(
            setAppState = stubbedSetAppState(),
            coroutineScope = scope,
            settings = settings,
            clock = { NOW }
        )

        service.onStart(owner)
        advanceUntilIdle()

        verifyBlocking(settings, never()) { setLastBackgroundedAt(NOW) }
    }

    /**
     * onStop also fires SetAppState on the same scope. Left unstubbed it returns
     * null from the mock and the resulting failure tears down the shared scope
     * before the stamp coroutine runs.
     */
    private fun stubbedSetAppState(): SetAppState = mock {
        onBlocking { async(any()) } doReturn Resultat.Success(Unit)
    }

    companion object {
        private const val NOW = 1_700_000_000L
    }
}
