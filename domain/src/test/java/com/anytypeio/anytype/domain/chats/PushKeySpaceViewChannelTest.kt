package com.anytypeio.anytype.domain.chats

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import java.util.Base64
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PushKeySpaceViewChannelTest {

    @Test
    fun `emits PushKeyUpdate for valid spacePushNotificationEncryptionKey`() = runTest {
        val key = Base64.getEncoder().encodeToString("test-key".toByteArray())
        val spaceView = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn key
        }
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn MutableStateFlow(listOf(spaceView))
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            val update = awaitItem()
            val expectedId = key.computePushKeyId()
            assertEquals(PushKeyUpdate(expectedId, key), update)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit for null or empty key`() = runTest {
        val spaceView1 = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn null
        }
        val spaceView2 = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn ""
        }
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn MutableStateFlow(listOf(spaceView1, spaceView2))
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not re-emit when the same key set is re-published by the subscription`() = runTest {
        val key = Base64.getEncoder().encodeToString("stable-key".toByteArray())
        val spaceView = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn key
        }
        // The space-view subscription amends repeatedly with the same key set on cold start.
        val source = MutableStateFlow(listOf(spaceView))
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn source
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            // First publication produces exactly one update...
            assertEquals(PushKeyUpdate(key.computePushKeyId(), key), awaitItem())
            // ...and re-publishing the identical key set must not emit again.
            source.value = listOf(spaceView)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `re-emits the key set when a new key is added (set grows)`() = runTest {
        val keyA = Base64.getEncoder().encodeToString("keyA".toByteArray())
        val keyB = Base64.getEncoder().encodeToString("keyB".toByteArray())
        val spaceA = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn keyA
        }
        val spaceB = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn keyB
        }
        val source = MutableStateFlow(listOf(spaceA))
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn source
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            assertEquals(PushKeyUpdate(keyA.computePushKeyId(), keyA), awaitItem())
            // A new space with a new key changes the set, so the keys are re-published.
            source.value = listOf(spaceA, spaceB)
            assertEquals(PushKeyUpdate(keyA.computePushKeyId(), keyA), awaitItem())
            assertEquals(PushKeyUpdate(keyB.computePushKeyId(), keyB), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not re-emit when only the order of an unchanged key set changes`() = runTest {
        val keyA = Base64.getEncoder().encodeToString("keyA".toByteArray())
        val keyB = Base64.getEncoder().encodeToString("keyB".toByteArray())
        val spaceA = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn keyA
        }
        val spaceB = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn keyB
        }
        val source = MutableStateFlow(listOf(spaceA, spaceB))
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn source
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            assertEquals(PushKeyUpdate(keyA.computePushKeyId(), keyA), awaitItem())
            assertEquals(PushKeyUpdate(keyB.computePushKeyId(), keyB), awaitItem())
            // Same set, different order -> set-based dedup suppresses, no new emissions.
            source.value = listOf(spaceB, spaceA)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits for multiple valid keys`() = runTest {
        val key1 = Base64.getEncoder().encodeToString("key1".toByteArray())
        val key2 = Base64.getEncoder().encodeToString("key2".toByteArray())
        val spaceView1 = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn key1
        }
        val spaceView2 = mock<ObjectWrapper.SpaceView> {
            on { spacePushNotificationEncryptionKey } doReturn key2
        }
        val container = mock<SpaceViewSubscriptionContainer> {
            on { observe() } doReturn MutableStateFlow(listOf(spaceView1, spaceView2))
        }
        val channel = PushKeySpaceViewChannel(container)
        channel.observe().test {
            val update1 = awaitItem()
            val update2 = awaitItem()
            assertEquals(PushKeyUpdate(key1.computePushKeyId(), key1), update1)
            assertEquals(PushKeyUpdate(key2.computePushKeyId(), key2), update2)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 