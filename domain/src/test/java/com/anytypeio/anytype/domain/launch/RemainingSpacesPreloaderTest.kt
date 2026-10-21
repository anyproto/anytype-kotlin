package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class RemainingSpacesPreloaderTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository

    private lateinit var preloader: RemainingSpacesPreloader

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        preloader = RemainingSpacesPreloader(PreloadRemainingSpaces(dispatchers, repo))
    }

    @Test
    fun `fires the use case exactly once despite multiple scheduleOnce calls`() = runTest {
        preloader.scheduleOnce(this, delayMillis = 10)
        preloader.scheduleOnce(this, delayMillis = 10)
        preloader.scheduleOnce(this, delayMillis = 10)
        advanceUntilIdle()
        verify(repo, times(1)).preloadRemainingSpaces()
    }
}
