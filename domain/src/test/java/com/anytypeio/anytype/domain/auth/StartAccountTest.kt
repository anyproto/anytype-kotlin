package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.FeaturesConfig
import com.anytypeio.anytype.core_models.StubAccount
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions

class StartAccountTest {

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

    lateinit var selectAccount: SelectAccount

    private val config = StubConfig()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        selectAccount = SelectAccount(
            repository = repo,
            configStorage = configStorage,
            featuresConfigProvider = featuresConfigProvider,
            workspaceManager = workspaceManager
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

        val featuresConfig = FeaturesConfig(
            enableDataView = false,
            enableDebug = false,
            enablePrereleaseChannel = false
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn AccountSetup(
                account = account,
                features = featuresConfig,
                status = AccountStatus.Active,
                config = config
            )
        }

        selectAccount.run(params)

        verify(repo, times(1)).selectAccount(
            id = id,
            path = path
        )

        verify(repo, times(1)).saveAccount(account)

        verify(repo, times(1)).setCurrentAccount(account.id)

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

        val featuresConfig = FeaturesConfig(
            enableDataView = false,
            enableDebug = false,
            enablePrereleaseChannel = false
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn AccountSetup(
                account = account,
                features = featuresConfig,
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

        val featuresConfig = FeaturesConfig(
            enableDataView = null,
            enableDebug = null,
            enablePrereleaseChannel = null
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn AccountSetup(
                account = account,
                features = featuresConfig,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        verify(featuresConfigProvider, times(1)).set(
            enableDataView = false,
            enableDebug = false,
            enableChannelSwitch = false,
            enableSpaces = false
        )

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

        val featuresConfig = FeaturesConfig(
            enableDataView = true,
            enableDebug = false,
            enablePrereleaseChannel = true
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn AccountSetup(
                account = account,
                features = featuresConfig,
                status = AccountStatus.Active,
                config = config
            )
        }

        val result = selectAccount.run(params)

        verify(featuresConfigProvider, times(1)).set(
            enableDataView = true,
            enableDebug = false,
            enableChannelSwitch = true,
            enableSpaces = false
        )

        assertTrue { result == Either.Right(Pair(config.analytics, AccountStatus.Active)) }
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

        val givenParams = SelectAccount.Params(
            id = givenAccount.id,
            path = givenPath
        )

        // TESTING

        selectAccount.run(givenParams)

        verifyBlocking(workspaceManager, times(1)) {
            setCurrentWorkspace(
                givenAccountSetup.config.workspace
            )
        }
    }
}