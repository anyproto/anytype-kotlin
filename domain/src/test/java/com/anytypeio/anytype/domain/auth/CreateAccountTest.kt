package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class CreateAccountTest {

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

    private lateinit var createAccount: CreateAccount


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        createAccount = CreateAccount(
            repository = repo,
            configStorage = configStorage,
            metricsProvider = metricsProvider
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
                onBlocking { createAccount(name, path, icon) } doReturn setup
            }

            val version = MockDataFactory.randomString()
            val platform = MockDataFactory.randomString()

            stubMetricsProvider(version, platform)

            createAccount.run(param)

            verify(repo, times(1)).createAccount(name, path, icon)
            verify(repo, times(1)).saveAccount(setup.account)
            verify(repo, times(1)).setCurrentAccount(setup.account.id)
            verify(repo, times(1)).setMetrics(
                platform = platform,
                version = version
            )
            verifyNoMoreInteractions(repo)
            verify(configStorage, times(1)).set(setup.config)
        }

    @Test
    fun `should set current workspace id after creating account`() = runTest {

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
            onBlocking { createAccount(name, path, icon) } doReturn setup
        }

        createAccount.run(param)

        verify(workspaceManager, times(1)).setCurrentWorkspace(
            setup.config.spaceView
        )
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