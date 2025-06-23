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
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private val json = Json {
        ignoreUnknownKeys = true
    }

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
        assertTrue(pushKey.isEmpty())
    }

    @Test
    fun `savePushKey should store the key and keyId in shared preferences when channel emits`() =
        runTest(dispatcher) {
            val pushKeyId = "test_push_key_id"
            val pushKey = "test_push_key"

            // Simulate the event emission from the channel
            val channelFlow = MutableSharedFlow<PushKeyUpdate>(replay = 0)
            mockChannel.stub {
                on { observe() } doReturn channelFlow
            }

            val provider = createPushKeyProvider() // This will start observing the channel
            provider.start()
            dispatcher.scheduler.advanceUntilIdle() // Allow the observation coroutine to run

            // Emit the event
            channelFlow.emit(PushKeyUpdate(encryptionKey = pushKey, encryptionKeyId = pushKeyId))
            dispatcher.scheduler.advanceUntilIdle() // Allow the processing coroutine to run

            // Verify the stored values in SharedPreferences
            val storedKeysJson =
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEYS, "{}") ?: "{}"
            val storedKeys: Map<String, PushKey> = json.decodeFromString(storedKeysJson)

            assertTrue(storedKeys.containsKey(pushKeyId))
            assertEquals(pushKeyId, storedKeys[pushKeyId]?.id)
            assertEquals(pushKey, storedKeys[pushKeyId]?.value)
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

            val provider = createPushKeyProvider() // Start observing
            provider.start()
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
            val storedKeysJson =
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEYS, "{}") ?: "{}"
            val storedKeys: Map<String, PushKey> =
                json.decodeFromString(storedKeysJson)
            assertTrue(storedKeys.containsKey(initialKeyId))
            assertEquals(initialKeyId, storedKeys[initialKeyId]?.id)
            assertEquals(initialKey, storedKeys[initialKeyId]?.value)

            // Emit updated event
            channelFlow.emit(
                PushKeyUpdate(
                    encryptionKey = updatedKey,
                    encryptionKeyId = updatedKeyId
                )
            )
            dispatcher.scheduler.advanceUntilIdle() // Process emission

            // Verify updated storage
            val updatedStoredKeysJson =
                sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEYS, "{}") ?: "{}"
            val updatedStoredKeys: Map<String, PushKey> = json.decodeFromString(updatedStoredKeysJson)
            assertTrue(updatedStoredKeys.containsKey(updatedKeyId))
            assertEquals(updatedKeyId, updatedStoredKeys[updatedKeyId]?.id)
            assertEquals(updatedKey, updatedStoredKeys[updatedKeyId]?.value)
        }

    @Test
    fun `emit key1 to value1 and then update key1 to value2`() = runTest(dispatcher) {
        val key1 = "key1"
        val value1 = "value11"
        val value2 = "value22"

        // Simulate the event emission from the channel
        val channelFlow = MutableSharedFlow<PushKeyUpdate>(replay = 0)
        mockChannel.stub {
            on { observe() } doReturn channelFlow
        }

        // Start observing the channel (create provider)
        val provider = createPushKeyProvider()
        provider.start() // <-- Start the observation coroutine
        dispatcher.scheduler.advanceUntilIdle() // Allow the observation coroutine to run

        // Emit first event: (key1 to value1)
        channelFlow.emit(PushKeyUpdate(encryptionKey = value1, encryptionKeyId = key1))
        dispatcher.scheduler.advanceUntilIdle() // Allow the processing coroutine to run

        // Verify first event: (key1 to value1) has been saved in SharedPreferences
        val storedKeysJson1 =
            sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEYS, "{}") ?: "{}"
        val storedKeys1: Map<String, PushKey> = json.decodeFromString(storedKeysJson1)

        assertTrue(storedKeys1.containsKey(key1))
        assertEquals(key1, storedKeys1[key1]?.id)
        assertEquals(value1, storedKeys1[key1]?.value)

        assertEquals(1, storedKeys1.size) // Only one key should be present

        // Emit second event: (key1 to value2)
        channelFlow.emit(PushKeyUpdate(encryptionKey = value2, encryptionKeyId = key1))
        dispatcher.scheduler.advanceUntilIdle() // Allow the processing coroutine to run

        // Verify second event: (key1 to value2) has updated the value for the same key in SharedPreferences
        val storedKeysJson2 =
            sharedPreferences.getString(PushKeyProviderImpl.PREF_PUSH_KEYS, "{}") ?: "{}"
        val storedKeys2: Map<String, PushKey> = json.decodeFromString(storedKeysJson2)

        // Ensure the value for key1 has been updated to value2
        assertTrue(storedKeys2.containsKey(key1))
        assertEquals(key1, storedKeys2[key1]?.id) // Verify the updated value
        assertEquals(value2, storedKeys2[key1]?.value)

        // Ensure no other keys were affected
        assertEquals(1, storedKeys2.size) // Still only one key should be present
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
            scope = testScope,
            json = json
        )
    }

}