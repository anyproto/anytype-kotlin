package com.anytypeio.anytype.middleware.interactor

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.middleware.interactor.events.PushKeyMiddlewareChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushKeyMiddlewareChannelTest {

    private val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")
    private val testScope = TestScope(dispatcher)
    lateinit var eventHandlerChannel: EventHandlerChannel
    private lateinit var channel: PushKeyMiddlewareChannel

    @Before
    fun setup() {
        eventHandlerChannel = EventHandlerChannelImpl()
        channel = PushKeyMiddlewareChannel(
            scope = testScope,
            channel = eventHandlerChannel,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `should emit empty initially`() = runTest {
        // Given
        val initialValue = channel.observe().first()

        // Then
        assertEquals(PushKeyUpdate.EMPTY, initialValue)
    }

    @Test
    fun `should emit push key update when receiving two valid events`() = runTest(dispatcher) {

        turbineScope {
            // Given
            val expectedUpdate1 = PushKeyUpdate(
                encryptionKeyId = RandomString.make(),
                encryptionKey = RandomString.make(),
            )

            val event1 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        pushEncryptionKeyUpdate = anytype.Event.PushEncryptionKey.Update(
                            encryptionKeyId = expectedUpdate1.encryptionKeyId,
                            encryptionKey = expectedUpdate1.encryptionKey
                        )
                    )
                )
            )

            val expectedUpdate2 = PushKeyUpdate(
                encryptionKeyId = RandomString.make(),
                encryptionKey = RandomString.make()
            )

            val event2 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        pushEncryptionKeyUpdate = anytype.Event.PushEncryptionKey.Update(
                            encryptionKeyId = expectedUpdate2.encryptionKeyId,
                            encryptionKey = expectedUpdate2.encryptionKey
                        )
                    )
                )
            )

            // When
            channel.start()
            dispatcher.scheduler.advanceUntilIdle()

            // Then
            channel.observe().test {

                val emittedValue = awaitItem()
                assertEquals(PushKeyUpdate.EMPTY, emittedValue)

                eventHandlerChannel.emit(event1)
                dispatcher.scheduler.advanceUntilIdle()

                val update1 = awaitItem()
                assertEquals(expectedUpdate1, update1)

                eventHandlerChannel.emit(event2)
                dispatcher.scheduler.advanceUntilIdle()

                val update2 = awaitItem()
                assertEquals(expectedUpdate2, update2)

                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `should not emit update when receiving invalid event`() = runTest(dispatcher) {
        turbineScope {
            // Given

            val event = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = RandomString.make()
                        )
                    )
                )
            )

            // When
            channel.start()
            dispatcher.scheduler.advanceUntilIdle()

            // Then
            channel.observe().test {

                val emittedValue = awaitItem()
                assertEquals(PushKeyUpdate.EMPTY, emittedValue)

                eventHandlerChannel.emit(event)
                dispatcher.scheduler.advanceUntilIdle()

                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `should stop processing events after stop is called`() = runTest(dispatcher) {

        turbineScope {
            // Given

            val expectedUpdate = PushKeyUpdate(
                encryptionKeyId = RandomString.make(),
                encryptionKey = RandomString.make(),
            )

            val event = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        pushEncryptionKeyUpdate = anytype.Event.PushEncryptionKey.Update(
                            encryptionKeyId = expectedUpdate.encryptionKeyId,
                            encryptionKey = expectedUpdate.encryptionKey
                        )
                    )
                )
            )

            // When
            channel.start()
            channel.stop()
            dispatcher.scheduler.advanceUntilIdle()

            // Then
            channel.observe().test {

                val emittedValue = awaitItem()
                assertEquals(PushKeyUpdate.EMPTY, emittedValue)

                eventHandlerChannel.emit(event)
                dispatcher.scheduler.advanceUntilIdle()

                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `should handle multiple messages in single emission`() = runTest {

        turbineScope {
            turbineScope {
                // Given
                val expectedUpdate1 = PushKeyUpdate(
                    encryptionKeyId = RandomString.make(),
                    encryptionKey = RandomString.make(),
                )

                val event1 = anytype.Event(
                    messages = listOf(
                        anytype.Event.Message(pushEncryptionKeyUpdate = null),
                        anytype.Event.Message(
                            pushEncryptionKeyUpdate = anytype.Event.PushEncryptionKey.Update(
                                encryptionKeyId = expectedUpdate1.encryptionKeyId,
                                encryptionKey = expectedUpdate1.encryptionKey
                            )
                        ),
                        anytype.Event.Message(pushEncryptionKeyUpdate = null),
                    )
                )

                // When
                channel.start()
                dispatcher.scheduler.advanceUntilIdle()

                // Then
                channel.observe().test {

                    val emittedValue = awaitItem()
                    assertEquals(PushKeyUpdate.EMPTY, emittedValue)

                    eventHandlerChannel.emit(event1)
                    dispatcher.scheduler.advanceUntilIdle()

                    val update1 = awaitItem()
                    assertEquals(expectedUpdate1, update1)

                    ensureAllEventsConsumed()
                }
            }
        }
    }
} 