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
import org.mockito.kotlin.never
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
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
        private var currentSpace: SpaceId?,
        private val lastBackgroundedAt: Long? = null
    ) : UserSettingsRepository by delegate {
        override suspend fun getCurrentSpace(): SpaceId? = currentSpace
        override suspend fun clearCurrentSpace() { currentSpace = null }
        override suspend fun getLastBackgroundedAt(): Long? = lastBackgroundedAt
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        PreferredSpaceIdHolder.Default.clear()
    }

    private fun launchAccountWith(
        currentSpace: SpaceId?,
        lastBackgroundedAt: Long? = null,
        now: Long = NOW
    ): LaunchAccount {
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
            settings = FakeSettings(settingsMock, currentSpace, lastBackgroundedAt),
            awaitAccountStartManager = awaitAccountStartManager,
            preferredSpaceIdHolder = PreferredSpaceIdHolder.Default,
            context = rule.dispatcher,
            clock = { now }
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

    //region last-opened-space route expiry (DROID-4590)

    @Test
    fun `drops the last opened space when backgrounded more than an hour ago`() = runTest {
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = NOW - 3601
        )

        launchAccount.run(BaseUseCase.None)

        assertEquals(null, capturedCommand().preferredSpaceId)
    }

    @Test
    fun `does not set the space manager when the route has expired`() = runTest {
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = NOW - 3601
        )

        launchAccount.run(BaseUseCase.None)

        verifyBlocking(spaceManager, never()) { set(any(), any()) }
    }

    @Test
    fun `keeps the last opened space when backgrounded within the hour`() = runTest {
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = NOW - 600
        )

        launchAccount.run(BaseUseCase.None)

        assertEquals("last-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `keeps the last opened space when nothing was ever stamped`() = runTest {
        // Fresh install, or an upgrade from a build that never wrote the stamp.
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = null
        )

        launchAccount.run(BaseUseCase.None)

        assertEquals("last-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `keeps the last opened space when the clock moved backwards`() = runTest {
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = NOW + 9_000
        )

        launchAccount.run(BaseUseCase.None)

        assertEquals("last-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `holder still wins when the stored route has expired`() = runTest {
        // A push or deeplink names its own space; expiry must not discard it.
        val launchAccount = launchAccountWith(
            currentSpace = SpaceId("last-space"),
            lastBackgroundedAt = NOW - 86_400
        )
        PreferredSpaceIdHolder.Default.set("push-space")

        launchAccount.run(BaseUseCase.None)

        assertEquals("push-space", capturedCommand().preferredSpaceId)
    }

    //endregion

    companion object {
        private const val NOW = 1_700_000_000L
    }
}
