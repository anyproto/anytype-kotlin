package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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

class StartAccountTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var workspaceManager: WorkspaceManager

    @Mock
    lateinit var metricsProvider: MetricsProvider

    @Mock
    lateinit var awaitAccountStartManager: AwaitAccountStartManager

    lateinit var selectAccount: SelectAccount

    private val config = StubConfig()

    private val platform = MockDataFactory.randomString()
    private val version = MockDataFactory.randomString()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        selectAccount = SelectAccount(
            repository = repo,
            configStorage = configStorage,
            metricsProvider = metricsProvider,
            awaitAccountStartManager = awaitAccountStartManager
        )
    }

    @Test
    fun `should select account, set it as current user account and save it`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.DEFAULT
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.DEFAULT
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        stubMetricsProvider(
            version = version,
            platform = platform
        )

        selectAccount.run(params)

        val command = Command.AccountSelect(
            id = id,
            path = path,
            networkMode = NetworkMode.DEFAULT
        )

        verify(repo, times(1)).getNetworkMode()
        verify(repo, times(1)).selectAccount(command)

        verify(repo, times(1)).saveAccount(account)

        verify(repo, times(1)).setCurrentAccount(account.id)

        verify(repo, times(1)).setMetrics(
            platform = platform,
            version = version
        )

        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unit when use case is successfully completed`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.DEFAULT
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.DEFAULT
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
    }

    @Test
    fun `should set default flavour config`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.DEFAULT
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.DEFAULT
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
    }

    @Test
    fun `should set proper flavour config`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.DEFAULT
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.DEFAULT
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
    }

    @Test
    fun `should send local mode config on account select`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.LOCAL
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.LOCAL
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
    }

    @Test
    fun `should send custom mode config with path on account select`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val storedFilePath = MockDataFactory.randomString()
        val userPath = MockDataFactory.randomString()

        repo.stub {
            onBlocking {
                getNetworkMode()
            } doReturn NetworkModeConfig(
                networkMode = NetworkMode.CUSTOM,
                storedFilePath = storedFilePath,
                userFilePath = userPath
            )
        }

        repo.stub {
            onBlocking {
                val command = Command.AccountSelect(
                    id = id,
                    path = path,
                    networkMode = NetworkMode.CUSTOM,
                    networkConfigFilePath = storedFilePath
                )
                selectAccount(command)
            } doReturn AccountSetup(
                account = account,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
    }

    private fun stubMetricsProvider(version: String, platform: String) {
        metricsProvider.stub {
            onBlocking {
                getVersion()
            } doReturn version
            onBlocking {
                getPlatform()
            } doReturn platform
        }
    }
}