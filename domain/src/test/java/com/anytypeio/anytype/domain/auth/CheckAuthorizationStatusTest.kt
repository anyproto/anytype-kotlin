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

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.UNAUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return authorized status if account list is not empty`() = runBlocking {

        val account = Account(
            name = MockDataFactory.randomString(),
            id = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        repo.stub {
            onBlocking { getAccounts() } doReturn listOf(account)
        }

        val result = checkAuthorizationStatus.run(params = Unit)

        assertTrue { result == Either.Right(AuthStatus.AUTHORIZED) }

        verify(repo, times(1)).getAccounts()
        verifyNoMoreInteractions(repo)
    }

}