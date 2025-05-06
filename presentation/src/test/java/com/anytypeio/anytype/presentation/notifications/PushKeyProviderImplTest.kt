package com.anytypeio.anytype.presentation.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PushKeyProviderImplTest {

    @Mock
    private lateinit var mockChannel: PushKeyChannel

    private lateinit var sharedPreferences: SharedPreferences
    private val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")
    private val testScope = TestScope(dispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Use a real SharedPreferences from Robolectric
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

        // Clear shared preferences before each test
        sharedPreferences.edit().clear().apply()
    }

    @After
    fun tearDown() {
        // Clean up coroutines
        testScope.cancel()
        // Clear shared preferences after each test
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun `getPushKey should return empty PushKey when no key is set`() = runTest {
        val pushKeyProvider = createPushKeyProvider()
        val pushKey = pushKeyProvider.getPushKey()
        assert(pushKey == PushKey.EMPTY)
    }

    @Test
    fun `savePushKey should store the key and keyId in shared preferences when channel emits`() =
        runTest(dispatcher) {
            val pushKey = "test_push_key"
            val pushKeyId = "test_push_key_id"

            // Simulate the event emission from the channel
            val channelFlow = MutableSharedFlow<PushKeyUpdate>(replay = 0)
            mockChannel.stub {
                on { observe() } doReturn channelFlow
            }

            createPushKeyProvider() // This will start observing the channel
            dispatcher.scheduler.advanceUntilIdle() // Allow the observation coroutine to run

            // Emit the event
            channelFlow.emit(PushKeyUpdate(encryptionKey = pushKey, encryptionKeyId = pushKeyId))
            dispatcher.scheduler.advanceUntilIdle() // Allow the processing coroutine to run

            // Verify the stored values in SharedPreferences
            val storedKey = sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY, null)
            val storedKeyId =
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY_ID, null)

            assertEquals(pushKey, storedKey)
            assertEquals(pushKeyId, storedKeyId)
        }

    @Test
    fun `getPushKey should return the stored key when a key is set`() = runTest {
        val pushKey = PushKey(key = "test_push_key", id = "test_push_key_id")
        sharedPreferences.edit()
            .putString(PushKeyProviderImpl.PREF_PUSH_KEY_ID, pushKey.id)
            .apply()
        sharedPreferences.edit()
            .putString(PushKeyProviderImpl.PREF_PUSH_KEY, pushKey.key)
            .apply()

        val pushKeyProvider = createPushKeyProvider()
        val retrievedKey = pushKeyProvider.getPushKey()

        assertEquals(pushKey, retrievedKey)
    }

    @Test
    fun `observation should update shared preferences on subsequent channel emissions`() =
        runTest(dispatcher) {
            val initialKey = "initial_key"
            val initialKeyId = "initial_key_id"
            val updatedKey = "updated_key"
            val updatedKeyId = "updated_key_id"

            // Manually control the flow emission
            val channelFlow = MutableSharedFlow<PushKeyUpdate>(replay = 0)
            mockChannel.stub {
                on { observe() } doReturn channelFlow
            }

            createPushKeyProvider() // Start observing
            dispatcher.scheduler.advanceUntilIdle() // Ensure observation is set up

            // Emit initial event
            channelFlow.emit(
                PushKeyUpdate(
                    encryptionKey = initialKey,
                    encryptionKeyId = initialKeyId
                )
            )
            dispatcher.scheduler.advanceUntilIdle() // Process emission

            // Verify initial storage
            assertEquals(
                initialKey,
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY, null)
            )
            assertEquals(
                initialKeyId,
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY_ID, null)
            )

            // Emit updated event
            channelFlow.emit(
                PushKeyUpdate(
                    encryptionKey = updatedKey,
                    encryptionKeyId = updatedKeyId
                )
            )
            dispatcher.scheduler.advanceUntilIdle() // Process emission

            // Verify updated storage
            assertEquals(
                updatedKey,
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY, null)
            )
            assertEquals(
                updatedKeyId,
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEY_ID, null)
            )
        }

    private fun createPushKeyProvider(): PushKeyProviderImpl {
        return PushKeyProviderImpl(
            sharedPreferences = sharedPreferences,
            channel = mockChannel,
            dispatchers = AppCoroutineDispatchers(
                io = dispatcher,
                main = dispatcher,
                computation = dispatcher
            ),
            scope = testScope
        )
    }

}