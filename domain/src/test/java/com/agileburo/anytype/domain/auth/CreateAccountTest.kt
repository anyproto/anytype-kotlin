package com.agileburo.anytype.domain.auth

import com.agileburo.anytype.domain.auth.interactor.CreateAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
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
    fun `should create account and save it by calling repository method`() = runBlocking {

        val name = DataFactory.randomString()

        val account = Account(
            id = DataFactory.randomUuid(),
            name = DataFactory.randomString()
        )

        val param = CreateAccount.Params(name)

        repo.stub {
            onBlocking { createAccount(name) } doReturn account
        }

        createAccount.run(param)

        verify(repo, times(1)).createAccount(name)
        verify(repo, times(1)).saveAccount(any())
        verifyNoMoreInteractions(repo)
    }
}