package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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

    lateinit var createAccount: CreateAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        createAccount = CreateAccount(
            repository = repo,
            configStorage = configStorage
        )
    }

    @Test
    fun `should create account and save it and set as current user account and save config in storage`() =
        runBlocking {

            val name = MockDataFactory.randomString()

            val path = null

            val code = "code"

            val setup = StubAccountSetup()

            val param = CreateAccount.Params(
                name = name,
                avatarPath = path,
                invitationCode = code
            )

            repo.stub {
                onBlocking { createAccount(name, path, code) } doReturn setup
            }

            createAccount.run(param)

            verify(repo, times(1)).createAccount(name, path, code)
            verify(repo, times(1)).saveAccount(setup.account)
            verify(repo, times(1)).setCurrentAccount(setup.account.id)
            verifyNoMoreInteractions(repo)
            verify(configStorage, times(1)).set(setup.config)
        }
}