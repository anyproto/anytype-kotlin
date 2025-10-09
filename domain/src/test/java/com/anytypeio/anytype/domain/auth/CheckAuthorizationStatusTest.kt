package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class CheckAuthorizationStatusTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    private val dispatchers = AppCoroutineDispatchers(
        io = rule.dispatcher,
        computation = rule.dispatcher,
        main = rule.dispatcher
    )

    @Mock
    lateinit var repo: AuthRepository

    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this).close()
        checkAuthorizationStatus = CheckAuthorizationStatus(repo, dispatchers)
    }

    @Test
    fun `should return unauthorized status if account list is empty`() = runTest {

        whenever(repo.getAccounts()).thenReturn(emptyList())
        whenever(repo.getMnemonic()).thenReturn("mnemonic")

        val result = checkAuthorizationStatus.run(params = Unit)

        assertEquals(AuthStatus.UNAUTHORIZED to null, result)

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is null`() = runTest {

        whenever(repo.getAccounts()).thenReturn(emptyList())
        whenever(repo.getMnemonic()).thenReturn(null)

        val result = checkAuthorizationStatus.asFlow(params = Unit).first()

        assertEquals(AuthStatus.UNAUTHORIZED to null, result)

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is empty`() = runTest {

        whenever(repo.getAccounts()).thenReturn(emptyList())
        whenever(repo.getMnemonic()).thenReturn("")

        val result = checkAuthorizationStatus.asFlow(params = Unit).first()

        assertEquals(AuthStatus.UNAUTHORIZED to null, result)

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return unauthorized status if phrase is blank`() = runTest {

        whenever(repo.getAccounts()).thenReturn(emptyList())
        whenever(repo.getMnemonic()).thenReturn(" ")

        val result = checkAuthorizationStatus.asFlow(params = Unit).first()

        assertEquals(AuthStatus.UNAUTHORIZED to null, result)

        verify(repo, times(1)).getAccounts()
        verify(repo, times(1)).getMnemonic()
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun `should return authorized status if account list is not empty and phrase is not empty`() =
        runTest {

            val account = Account(
                id = MockDataFactory.randomString()
            )

            whenever(repo.getAccounts()).thenReturn(listOf(account))
            whenever(repo.getMnemonic()).thenReturn("1")


            val result = checkAuthorizationStatus.asFlow(params = Unit).first()

            assertEquals(AuthStatus.AUTHORIZED to account, result)

            verify(repo, times(1)).getAccounts()
            verify(repo, times(1)).getMnemonic()
            verifyNoMoreInteractions(repo)
        }

}