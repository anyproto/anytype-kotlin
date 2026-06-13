package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchAccountTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository
    @Mock lateinit var pathProvider: PathProvider
    @Mock lateinit var configStorage: ConfigStorage
    @Mock lateinit var spaceManager: SpaceManager
    @Mock lateinit var initialParamsProvider: InitialParamsProvider
    @Mock lateinit var settingsMock: UserSettingsRepository
    @Mock lateinit var awaitAccountStartManager: AwaitAccountStartManager

    /**
     * Returns [currentSpace] from real Kotlin (not Mockito) to dodge the
     * Mockito value-class suspend-stub pitfall with [SpaceId]; everything
     * else delegates to the mock.
     */
    private class FakeSettings(
        delegate: UserSettingsRepository,
        private val currentSpace: SpaceId?
    ) : UserSettingsRepository by delegate {
        override suspend fun getCurrentSpace(): SpaceId? = currentSpace
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        PreferredSpaceIdHolder.Default.clear()
    }

    private fun launchAccountWith(currentSpace: SpaceId?): LaunchAccount {
        val setup = StubAccountSetup()
        repo.stub {
            onBlocking { getNetworkMode() } doReturn NetworkModeConfig(networkMode = NetworkMode.DEFAULT)
            onBlocking { getCurrentAccountId() } doReturn "account-1"
            onBlocking { selectAccount(any()) } doReturn setup
        }
        pathProvider.stub { onBlocking { providePath() } doReturn "/path" }
        return LaunchAccount(
            repository = repo,
            pathProvider = pathProvider,
            configStorage = configStorage,
            spaceManager = spaceManager,
            initialParamsProvider = initialParamsProvider,
            settings = FakeSettings(settingsMock, currentSpace),
            awaitAccountStartManager = awaitAccountStartManager,
            preferredSpaceIdHolder = PreferredSpaceIdHolder.Default,
            context = rule.dispatcher
        )
    }

    private suspend fun capturedCommand(): Command.AccountSelect {
        val captor = argumentCaptor<Command.AccountSelect>()
        verify(repo).selectAccount(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `uses holder value when present`() = runTest {
        val launchAccount = launchAccountWith(currentSpace = SpaceId("last-space"))
        PreferredSpaceIdHolder.Default.set("deeplink-space")

        launchAccount.run(BaseUseCase.None)

        assertEquals("deeplink-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `falls back to last opened space when holder empty`() = runTest {
        val launchAccount = launchAccountWith(currentSpace = SpaceId("last-space"))

        launchAccount.run(BaseUseCase.None)

        assertEquals("last-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `preferredSpaceId is null when neither holder nor last space present`() = runTest {
        val launchAccount = launchAccountWith(currentSpace = null)

        launchAccount.run(BaseUseCase.None)

        assertEquals(null, capturedCommand().preferredSpaceId)
    }
}
