package com.agileburo.anytype

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.agileburo.anytype.persistence.db.AnytypeDatabase
import com.agileburo.anytype.persistence.model.AccountTable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AccountDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().context,
        AnytypeDatabase::class.java
    ).allowMainThreadQueries().build()

    @After
    fun after() {
        database.close()
    }

    @Test
    fun `should return empty list if there are no last account in db`() {
        runBlocking {
            val accounts = database.accountDao().lastAccount()
            assertTrue { accounts.isEmpty() }
        }
    }

    @Test
    fun `should return last account`() = runBlocking {

        val firstAccount = AccountTable(
            id = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            timestamp = System.currentTimeMillis()
        )

        delay(1)

        val secondAccount = AccountTable(
            id = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            timestamp = System.currentTimeMillis()
        )

        database.accountDao().insert(firstAccount)
        database.accountDao().insert(secondAccount)

        val result = database.accountDao().lastAccount()

        assertTrue { result.size == 1 }
        assertTrue { result.first() == secondAccount }
    }

    @Test
    fun `should return expected account when queried using account id`() = runBlocking {

        val account = AccountTable(
            id = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            timestamp = System.currentTimeMillis()
        )

        database.accountDao().insert(account)

        val result = database.accountDao().getAccount(account.id)

        assertEquals(account, result)
    }
}