package com.agileburo.anytype.domain.auth

import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.common.CoroutineTestRule
import com.agileburo.anytype.domain.common.MockDataFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
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
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null
        )

        repo.stub {
            onBlocking { observeAccounts() } doReturn listOf(account).asFlow()
        }

        val result = observeAccounts.build().single()

        assertTrue { result == listOf(account) }
    }

    @Test
    fun `should collect one account, then two accounts, emitting accumulated results`() =
        runBlocking {

            val accounts = listOf(
                Account(
                    id = MockDataFactory.randomUuid(),
                    name = MockDataFactory.randomString(),
                    avatar = null
                ),
                Account(
                    id = MockDataFactory.randomUuid(),
                    name = MockDataFactory.randomString(),
                    avatar = null
                )
            )

            repo.stub {
                onBlocking { observeAccounts() } doReturn accounts.asFlow()
            }

            observeAccounts.build().collectIndexed { index, value ->
                when (index) {
                    0 -> assertEquals(listOf(accounts.first()), value)
                    1 -> assertEquals(accounts, value)
                }
            }
        }
}