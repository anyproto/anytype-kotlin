package com.anytypeio.anytype.domain.dashboard

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.util.dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class OpenDashboardTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var auth: AuthRepository

    @Mock
    lateinit var configStorage: ConfigStorage

    private lateinit var usecase: OpenDashboard

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        usecase = OpenDashboard(
            repo = repo,
            auth = auth,
            provider = configStorage,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `should open a home dashboard if there are no params`() = runBlockingTest {

        val config = StubConfig()

        configStorage.stub {
            onBlocking { get() } doReturn config
        }

        usecase.run(Unit)

        verify(configStorage, times(1)).get()
        verify(repo, times(1)).openDashboard(
            contextId = config.home,
            id = config.home
        )
        verifyNoMoreInteractions(repo)
    }
}