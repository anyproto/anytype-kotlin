package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class LaunchAccountTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    @Mock
    lateinit var pathProvider: PathProvider

    @Mock
    lateinit var initialParamsProvider: InitialParamsProvider

    @Mock
    lateinit var awaitAccountStartManager: AwaitAccountStartManager

    private lateinit var launchAccount: LaunchAccount

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        launchAccount = LaunchAccount(
            repository = repo,
            pathProvider = pathProvider,
            configStorage = configStorage,
            spaceManager = spaceManager,
            initialParamsProvider = initialParamsProvider,
            settings = userSettingsRepository,
            awaitAccountStartManager = awaitAccountStartManager,
            context = rule.dispatcher
        )
    }

    @Test
    fun `should fallback to setup space when last session space fails to open`() = runTest {
        val lastSessionSpace = SpaceId("last-space")
        val defaultSpace = "default-space"
        val setup = StubAccountSetup(config = StubConfig(space = defaultSpace))
        stubAccountLaunch(setup = setup, lastSessionSpace = lastSessionSpace)

        spaceManager.stub {
            onBlocking { set(lastSessionSpace.id) } doReturn Result.failure(Exception("space is not ready"))
            onBlocking { set(defaultSpace) } doReturn Result.success(setup.config)
        }

        val result = launchAccount.run(BaseUseCase.None)

        assertTrue("Expected launch to succeed, got: $result", result.isRight)
        verify(spaceManager, times(1)).set(lastSessionSpace.id)
        verify(spaceManager, times(1)).set(defaultSpace)
        verify(awaitAccountStartManager, times(1)).setState(AwaitAccountStartManager.State.Started)
    }

    @Test
    fun `should open setup space when last session space is missing`() = runTest {
        val defaultSpace = "default-space"
        val setup = StubAccountSetup(config = StubConfig(space = defaultSpace))
        stubAccountLaunch(setup = setup, lastSessionSpace = null)

        spaceManager.stub {
            onBlocking { set(defaultSpace) } doReturn Result.success(setup.config)
        }

        val result = launchAccount.run(BaseUseCase.None)

        assertTrue("Expected launch to succeed, got: $result", result.isRight)
        verify(spaceManager, times(1)).set(defaultSpace)
        verify(awaitAccountStartManager, times(1)).setState(AwaitAccountStartManager.State.Started)
    }

    private suspend fun stubAccountLaunch(
        setup: com.anytypeio.anytype.core_models.AccountSetup,
        lastSessionSpace: SpaceId?
    ) {
        val initialParams = Command.SetInitialParams(
            version = "1.0.0",
            platform = "android",
            workDir = "/tmp",
            defaultLogLevel = "warn"
        )
        initialParamsProvider.stub {
            on { toCommand() } doReturn initialParams
        }
        repo.stub {
            onBlocking { getNetworkMode() } doReturn NetworkModeConfig(networkMode = NetworkMode.DEFAULT)
            onBlocking { getCurrentAccountId() } doReturn setup.account.id
            onBlocking { selectAccount(any()) } doReturn setup
        }
        pathProvider.stub {
            on { providePath() } doReturn "/repo"
        }
        org.mockito.Mockito.doReturn(lastSessionSpace?.id)
            .`when`(userSettingsRepository)
            .getCurrentSpace()
    }
}
