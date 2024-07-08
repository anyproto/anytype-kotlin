package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAccountsTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var observeAccounts: ObserveAccounts

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        observeAccounts = ObserveAccounts(repo)
    }

    @Test
    fun `should collect one account when stream is called`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomUuid()
        )

        repo.stub {
            onBlocking { observeAccounts() } doReturn listOf(account).asFlow()
        }

        val result = observeAccounts.build().single()

        assertTrue { result == account }
    }

    @Test
    fun `should collect accounts sequentially`() =
        runBlocking {

            val accounts = listOf(
                Account(
                    id = MockDataFactory.randomUuid()
                ),
                Account(
                    id = MockDataFactory.randomUuid()
                )
            )

            repo.stub {
                onBlocking { observeAccounts() } doReturn accounts.asFlow()
            }

            observeAccounts.build().collectIndexed { index, value ->
                when (index) {
                    0 -> assertEquals(accounts.first(), value)
                    1 -> assertEquals(accounts.last(), value)
                }
            }
        }
}