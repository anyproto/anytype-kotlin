package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CreateAccountTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var createAccount: CreateAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        createAccount = CreateAccount(repository = repo)
    }

    @Test
    fun `should create account and save it and set as current user account`() = runBlocking {

        val name = MockDataFactory.randomString()

        val path = null

        val code = "code"

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val param = CreateAccount.Params(
            name = name,
            avatarPath = path,
            invitationCode = code
        )

        repo.stub {
            onBlocking { createAccount(name, path, code) } doReturn account
        }

        createAccount.run(param)

        verify(repo, times(1)).createAccount(name, path, code)
        verify(repo, times(1)).saveAccount(account)
        verify(repo, times(1)).setCurrentAccount(account.id)
        verifyNoMoreInteractions(repo)
    }
}