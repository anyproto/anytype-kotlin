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