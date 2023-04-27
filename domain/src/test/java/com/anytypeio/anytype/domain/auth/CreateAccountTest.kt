package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
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

    private lateinit var createAccount: CreateAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        createAccount = CreateAccount(
            repository = repo,
            configStorage = configStorage,
            workspaceManager = workspaceManager
        )
    }

    @Test
    fun `should create account and save it and set as current user account and save config in storage`() = runTest {
            val name = MockDataFactory.randomString()
            val path = null
            val code = "code"
            val icon = 1
            val setup = StubAccountSetup()
            val param = CreateAccount.Params(
                name = name,
                avatarPath = path,
                invitationCode = code,
                icon = icon
            )

            repo.stub {
                onBlocking { createAccount(name, path, code, icon) } doReturn setup
            }

            createAccount.run(param)

            verify(repo, times(1)).createAccount(name, path, code, icon)
            verify(repo, times(1)).saveAccount(setup.account)
            verify(repo, times(1)).setCurrentAccount(setup.account.id)
            verifyNoMoreInteractions(repo)
            verify(configStorage, times(1)).set(setup.config)
    }

    @Test
    fun `should set current workspace id after creating account`() = runTest {

        val name = MockDataFactory.randomString()
        val path = null
        val code = "code"
        val icon = 1
        val setup = StubAccountSetup()

        val param = CreateAccount.Params(
            name = name,
            avatarPath = path,
            invitationCode = code,
            icon = icon
        )

        repo.stub {
            onBlocking { createAccount(name, path, code, icon) } doReturn setup
        }

        createAccount.run(param)

        verify(workspaceManager, times(1)).setCurrentWorkspace(
            setup.config.workspace
        )
    }
}