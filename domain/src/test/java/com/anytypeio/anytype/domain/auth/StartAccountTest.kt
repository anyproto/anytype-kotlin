package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.FlavourConfig
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class StartAccountTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var flavourConfigProvider: FlavourConfigProvider

    lateinit var startAccount: StartAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        startAccount = StartAccount(repo, flavourConfigProvider)
    }

    @Test
    fun `should select account, set it as current user account and save it`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = StartAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val config = FlavourConfig(
            enableDataView = false,
            enableDebug = false,
            enableChannelSwitch = false
        )

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn Triple(account, config, AccountStatus.Active)
        }

        startAccount.run(params)

        verify(repo, times(1)).startAccount(
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

        val params = StartAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val config = FlavourConfig(
            enableDataView = false,
            enableDebug = false,
            enableChannelSwitch = false
        )

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn Triple(account, config, AccountStatus.Active)
        }

        val result = startAccount.run(params)

        assertTrue { result == Either.Right(Pair(account.id, AccountStatus.Active)) }
    }

    @Test
    fun `should set default flavour config`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = StartAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val config = FlavourConfig(
            enableDataView = null,
            enableDebug = null,
            enableChannelSwitch = null
        )

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn Triple(account, config, AccountStatus.Active)
        }

        val result = startAccount.run(params)

        verify(flavourConfigProvider, times(1)).set(
            enableDataView = false,
            enableDebug = false,
            enableChannelSwitch = false,
            enableSpaces = false
        )

        assertTrue { result == Either.Right(Pair(account.id, AccountStatus.Active)) }
    }

    @Test
    fun `should set proper flavour config`() = runBlocking {

        val id = MockDataFactory.randomString()
        val path = MockDataFactory.randomString()

        val params = StartAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val config = FlavourConfig(
            enableDataView = true,
            enableDebug = false,
            enableChannelSwitch = true
        )

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn Triple(account, config, AccountStatus.Active)
        }

        val result = startAccount.run(params)

        verify(flavourConfigProvider, times(1)).set(
            enableDataView = true,
            enableDebug = false,
            enableChannelSwitch = true,
            enableSpaces = false
        )

        assertTrue { result == Either.Right(Pair(account.id, AccountStatus.Active)) }
    }
}