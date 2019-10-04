package com.agileburo.anytype.feature_login.domain

import com.agileburo.anytype.feature_login.common.CoroutineTestRule
import com.agileburo.anytype.feature_login.common.DataFactory
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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
    fun `should create account by calling repository method`() = runBlocking {

        val name = DataFactory.randomString()

        val param = CreateAccount.Params(name)

        createAccount.run(param)

        verify(repo, times(1)).createAccount(name)
        verifyNoMoreInteractions(repo)
    }
}