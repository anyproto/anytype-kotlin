package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.platform.MetricsProvider
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class CreateAccountTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var configStorage: ConfigStorage

    lateinit var dispatchers: AppCoroutineDispatchers

    @Mock
    lateinit var metricsProvider: MetricsProvider

    private lateinit var createAccount: CreateAccount


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        createAccount = CreateAccount(
            repository = repo,
            configStorage = configStorage,
            metricsProvider = metricsProvider,
            dispatcher = dispatchers
        )
    }

    @Test
    fun `should create account and save it and set as current user account and save config in storage`() =
        runTest {
            val name = MockDataFactory.randomString()
            val path = null
            val icon = 1
            val setup = StubAccountSetup()
            val param = CreateAccount.Params(
                name = name,
                avatarPath = path,
                iconGradientValue = icon
            )

            repo.stub {
                onBlocking {
                    getNetworkMode()
                } doReturn NetworkModeConfig(
                    networkMode = NetworkMode.DEFAULT
                )
            }

            repo.stub {
                val command = Command.AccountCreate(
                    name = name,
                    avatarPath = path,
                    icon = icon,
                    networkMode = NetworkMode.DEFAULT
                )
                onBlocking { createAccount(command) } doReturn setup
            }

            val version = MockDataFactory.randomString()
            val platform = MockDataFactory.randomString()

            stubMetricsProvider(version, platform)

            createAccount.run(param)

            val command = Command.AccountCreate(
                name = name,
                avatarPath = path,
                icon = icon,
                networkMode = NetworkMode.DEFAULT
            )
            verify(repo, times(1)).getNetworkMode()
            verify(repo, times(1)).createAccount(command)
            verify(repo, times(1)).saveAccount(setup.account)
            verify(repo, times(1)).setCurrentAccount(setup.account.id)
            verify(repo, times(1)).setMetrics(
                platform = platform,
                version = version
            )
            verifyNoMoreInteractions(repo)
            verify(configStorage, times(1)).set(setup.config)
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