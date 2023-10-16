package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.StubAccount
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class ResumeAccountTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var featuresConfigProvider: FeaturesConfigProvider

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var workspaceManager: WorkspaceManager

    @Mock
    lateinit var pathProvider: PathProvider

    @Mock
    lateinit var metricsProvider: MetricsProvider

    lateinit var resumeAccount: ResumeAccount

    private val config = StubConfig()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        resumeAccount = ResumeAccount(
            repository = repo,
            configStorage = configStorage,
            featuresConfigProvider = featuresConfigProvider,
            pathProvider = pathProvider,
            metricsProvider = metricsProvider
        )
    }

    @Test
    fun `should set workspace id after resuming account`() = runTest {

        // SETUP

        val givenAccount = StubAccount()
        val givenAccountSetup = StubAccountSetup(account = givenAccount)
        val givenPath = MockDataFactory.randomString()

        repo.stub {
            onBlocking {
                selectAccount(
                    id = givenAccount.id,
                    path = givenPath
                )
            } doReturn givenAccountSetup
        }

        repo.stub {
            onBlocking {
                getCurrentAccountId()
            } doReturn givenAccount.id
        }

        pathProvider.stub {
            onBlocking {
                providePath()
            } doReturn givenPath
        }

        // TESTING

        resumeAccount.run(BaseUseCase.None)

        verifyBlocking(workspaceManager, times(1)) {
            setCurrentWorkspace(
                givenAccountSetup.config.spaceView
            )
        }
    }
}