package com.agileburo.anytype.domain.auth

import com.agileburo.anytype.domain.auth.interactor.SelectAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.common.CoroutineTestRule
import com.agileburo.anytype.domain.common.DataFactory
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class SelectAccountTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var selectAccount: SelectAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        selectAccount = SelectAccount(repo)
    }

    @Test
    fun `should select account and save it`() = runBlocking {

        val id = DataFactory.randomString()
        val path = DataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = DataFactory.randomString(),
            avatar = null
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn account
        }

        selectAccount.run(params)

        verify(repo, times(1)).selectAccount(
            id = id,
            path = path
        )

        verify(repo, times(1)).saveAccount(account)

        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unit when use case is successfully completed`() = runBlocking {

        val id = DataFactory.randomString()
        val path = DataFactory.randomString()

        val params = SelectAccount.Params(
            id = id,
            path = path
        )

        val account = Account(
            id = id,
            name = DataFactory.randomString(),
            avatar = null
        )

        repo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doReturn account
        }

        val result = selectAccount.run(params)

        assertTrue { result == Either.Right(Unit) }
    }
}