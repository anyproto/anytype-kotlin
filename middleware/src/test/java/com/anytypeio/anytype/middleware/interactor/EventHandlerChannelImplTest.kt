package com.anytypeio.anytype.middleware.interactor

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EventHandlerChannelImplTest {

    @Test
    fun `channel should receive events`() = runTest {

        turbineScope {
            val event1 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = "spaceId1",
                            status = anytype.Event.P2PStatus.Status.Connected,
                            devicesCounter = 145
                        )
                    )
                )
            )

            val event2 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        spaceSyncStatusUpdate = anytype.Event.Space.SyncStatus.Update(
                            id = "id1",
                            status = anytype.Event.Space.Status.Syncing,
                            network = anytype.Event.Space.Network.Anytype,
                            syncingObjectsCounter = 999
                        )
                    )
                )
            )

            val eventHandlerChannelImpl = EventHandlerChannelImpl()

            eventHandlerChannelImpl.flow().test {
                eventHandlerChannelImpl.emit(event1)
                assertEquals(event1, awaitItem())
                eventHandlerChannelImpl.emit(event2)
                assertEquals(event2, awaitItem())
            }
        }
    }
}