package com.anytypeio.anytype.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.anytypeio.anytype.persistence.db.AnytypeDatabase
import com.anytypeio.anytype.persistence.repo.DefaultAuthCache
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DefaultAuthCacheTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().context,
        AnytypeDatabase::class.java
    ).allowMainThreadQueries().build()

    private val encryptedPrefs = RuntimeEnvironment.getApplication().getSharedPreferences(
        "Encrypted prefs",
        Context.MODE_PRIVATE
    )

    private val defaultPrefs = RuntimeEnvironment.getApplication().getSharedPreferences(
        "Default prefs",
        Context.MODE_PRIVATE
    )

    @After
    fun after() {
        database.close()
    }

    @Test
    fun `should save mnemonic and clear mnemonic on logout`() = runTest {

        val givenMnemonic = MockDataFactory.randomString()

        val cache = DefaultAuthCache(
            db = database,
            encryptedPrefs = encryptedPrefs,
            defaultPrefs = defaultPrefs
        )

        cache.saveMnemonic(mnemonic = givenMnemonic)

        assertTrue { encryptedPrefs.contains(DefaultAuthCache.MNEMONIC_KEY) }

        assertEquals(
            expected = givenMnemonic,
            actual = cache.getMnemonic()
        )

        cache.logout()

        assertFalse { encryptedPrefs.contains(DefaultAuthCache.MNEMONIC_KEY) }
        assertFalse { defaultPrefs.contains(DefaultAuthCache.MNEMONIC_KEY) }
    }

}