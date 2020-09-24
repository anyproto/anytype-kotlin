package com.agileburo.anytype.domain.auth

import com.agileburo.anytype.domain.auth.interactor.StartAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.common.CoroutineTestRule
import com.agileburo.anytype.domain.common.MockDataFactory
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

    lateinit var startAccount: StartAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        startAccount = StartAccount(repo)
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

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn account
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

        repo.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doReturn account
        }

        val result = startAccount.run(params)

        assertTrue { result == Either.Right(account.id) }
    }
}