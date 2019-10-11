package com.agileburo.anytype.feature_login.domain

import com.agileburo.anytype.feature_login.common.CoroutineTestRule
import com.agileburo.anytype.feature_login.common.DataFactory
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount
import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
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
    lateinit var repo: UserRepository

    lateinit var createAccount: CreateAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        createAccount = CreateAccount(userRepository = repo)
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