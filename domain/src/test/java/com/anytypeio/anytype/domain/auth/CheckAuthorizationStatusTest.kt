package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
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
import kotlin.test.assertTrue

class CheckAuthorizationStatusTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        checkAuthorizationStatus = CheckAuthorizationStatus(repo)
    }

    @Test
    fun `should return unauthorized status if account list is empty`() = runBlocking {

        repo.stub {
            onBlocking { getAccounts() } doReturn emptyList()
        }

        repo.stub {
            onBlocking { getMnemonic() } doReturn "mnemonic"
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.UNAUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is null`() = runBlocking {

        repo.stub {
            onBlocking { getAccounts() } doReturn emptyList()
        }

        repo.stub {
            onBlocking { getMnemonic() } doReturn null
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.UNAUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is empty`() = runBlocking {

        repo.stub {
            onBlocking { getAccounts() } doReturn emptyList()
        }

        repo.stub {
            onBlocking { getMnemonic() } doReturn ""
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.UNAUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is blank`() = runBlocking {

        repo.stub {
            onBlocking { getAccounts() } doReturn emptyList()
        }

        repo.stub {
            onBlocking { getMnemonic() } doReturn " "
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.UNAUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return authorized status if account list is not empty and phrase is not empty`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomString()
        )

        repo.stub {
            onBlocking { getAccounts() } doReturn listOf(account)
        }
        repo.stub {
            onBlocking { getMnemonic() } doReturn "1"
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.AUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

}