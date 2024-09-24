package com.anytypeio.anytype.middleware.interactor

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.multiplayer.P2PStatus
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import kotlin.test.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class SyncAndP2PStatusEventsStoreImplTest {

    private val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")

    private val spaceId1 = "spaceId1"
    private val spaceId2 = "spaceId2"
    private val spaceId3 = "spaceId3"

    lateinit var channel: EventHandlerChannelImpl

    @Before
    fun setUp() {
        channel = EventHandlerChannelImpl()
    }

    @Test
    fun `should update spaces p2p statuses`() = runTest(dispatcher) {

        turbineScope {

            val store = SyncAndP2PStatusEventsStoreImpl(
                channel = channel,
                dispatcher = dispatcher,
                scope = backgroundScope
            )

            val initialEvent = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = spaceId1,
                        )
                    ),
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = spaceId2,
                            status = anytype.Event.P2PStatus.Status.Connected,
                            devicesCounter = 145
                        )
                    )
                )
            )

            val event1 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = spaceId2,
                            status = anytype.Event.P2PStatus.Status.Connected,
                            devicesCounter = 233
                        )
                    )
                )
            )

            val event2 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        p2pStatusUpdate = anytype.Event.P2PStatus.Update(
                            spaceId = spaceId1,
                            status = anytype.Event.P2PStatus.Status.NotPossible
                        )
                    )
                )
            )

            store.start()

            store.p2pStatus.test {
                val firstItem = awaitItem()
                assertEquals(mapOf(), firstItem)
                channel.emit(initialEvent)
                assertEquals(
                    expected = mapOf(
                        spaceId1 to P2PStatusUpdate.Update(
                            spaceId = spaceId1,
                            status = P2PStatus.NOT_CONNECTED,
                            devicesCounter = 0
                        )
                    ),
                    actual = awaitItem()
                )
                assertEquals(
                    expected = mapOf(
                        spaceId1 to P2PStatusUpdate.Update(
                            spaceId = spaceId1,
                            status = P2PStatus.NOT_CONNECTED,
                            devicesCounter = 0
                        ),
                        spaceId2 to P2PStatusUpdate.Update(
                            spaceId = spaceId2,
                            status = P2PStatus.CONNECTED,
                            devicesCounter = 145
                        )
                    ),
                    actual = awaitItem()
                )
                channel.emit(event1)
                assertEquals(
                    expected = mapOf(
                        spaceId1 to P2PStatusUpdate.Update(
                            spaceId = spaceId1,
                            status = P2PStatus.NOT_CONNECTED,
                            devicesCounter = 0
                        ),
                        spaceId2 to P2PStatusUpdate.Update(
                            spaceId = spaceId2,
                            status = P2PStatus.CONNECTED,
                            devicesCounter = 233
                        )
                    ),
                    actual = awaitItem()
                )
                channel.emit(event2)
                assertEquals(
                    expected = mapOf(
                        spaceId1 to P2PStatusUpdate.Update(
                            spaceId = spaceId1,
                            status = P2PStatus.NOT_POSSIBLE,
                            devicesCounter = 0
                        ),
                        spaceId2 to P2PStatusUpdate.Update(
                            spaceId = spaceId2,
                            status = P2PStatus.CONNECTED,
                            devicesCounter = 233
                        )
                    ),
                    actual = awaitItem()
                )
            }
        }
    }

    @Test
    fun `should update spaces sync statuses`() = runTest(dispatcher) {
        turbineScope {

            val store = SyncAndP2PStatusEventsStoreImpl(
                channel = channel,
                dispatcher = dispatcher,
                scope = backgroundScope
            )

            val initialEvent = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        spaceSyncStatusUpdate = anytype.Event.Space.SyncStatus.Update(
                            id = spaceId1,
                            status = anytype.Event.Space.Status.Offline,
                            network = anytype.Event.Space.Network.Anytype
                        )
                    ),
                    anytype.Event.Message(
                        spaceSyncStatusUpdate = anytype.Event.Space.SyncStatus.Update(
                            id = spaceId2,
                            status = anytype.Event.Space.Status.Syncing,
                            network = anytype.Event.Space.Network.SelfHost
                        )
                    )
                )
            )

            val event1 = anytype.Event(
                messages = listOf(
                    anytype.Event.Message(
                        spaceSyncStatusUpdate = anytype.Event.Space.SyncStatus.Update(
                            id = spaceId2,
                            status = anytype.Event.Space.Status.Error,
                            network = anytype.Event.Space.Network.SelfHost,
                            error = anytype.Event.Space.SyncError.StorageLimitExceed
                        )
                    ),
                    anytype.Event.Message(
                        spaceSyncStatusUpdate = anytype.Event.Space.SyncStatus.Update(
                            id = spaceId3,
                            status = anytype.Event.Space.Status.Synced,
                            network = anytype.Event.Space.Network.Anytype,
                            syncingObjectsCounter = 2345
                        )
                    )
                )
            )

            store.start()

            store.syncStatus.test {
                val firstItem = awaitItem()
                assertEquals(mapOf(), firstItem)

                channel.emit(initialEvent)

                assertEquals(
                    expected = mapOf(
                        spaceId1 to SpaceSyncUpdate.Update(
                            id = spaceId1,
                            status = SpaceSyncStatus.OFFLINE,
                            network = SpaceSyncNetwork.ANYTYPE,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.NULL
                        )
                    ),
                    actual = awaitItem()
                )

                assertEquals(
                    expected = mapOf(
                        spaceId1 to SpaceSyncUpdate.Update(
                            id = spaceId1,
                            status = SpaceSyncStatus.OFFLINE,
                            network = SpaceSyncNetwork.ANYTYPE,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.NULL
                        ),
                        spaceId2 to SpaceSyncUpdate.Update(
                            id = spaceId2,
                            status = SpaceSyncStatus.SYNCING,
                            network = SpaceSyncNetwork.SELF_HOST,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.NULL
                        )
                    ),
                    actual = awaitItem()
                )

                channel.emit(event1)

                assertEquals(
                    expected = mapOf(
                        spaceId1 to SpaceSyncUpdate.Update(
                            id = spaceId1,
                            status = SpaceSyncStatus.OFFLINE,
                            network = SpaceSyncNetwork.ANYTYPE,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.NULL
                        ),
                        spaceId2 to SpaceSyncUpdate.Update(
                            id = spaceId2,
                            status = SpaceSyncStatus.ERROR,
                            network = SpaceSyncNetwork.SELF_HOST,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.STORAGE_LIMIT_EXCEED
                        )
                    ),
                    actual = awaitItem()
                )

                assertEquals(
                    expected = mapOf(
                        spaceId1 to SpaceSyncUpdate.Update(
                            id = spaceId1,
                            status = SpaceSyncStatus.OFFLINE,
                            network = SpaceSyncNetwork.ANYTYPE,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.NULL
                        ),
                        spaceId2 to SpaceSyncUpdate.Update(
                            id = spaceId2,
                            status = SpaceSyncStatus.ERROR,
                            network = SpaceSyncNetwork.SELF_HOST,
                            syncingObjectsCounter = 0,
                            error = SpaceSyncError.STORAGE_LIMIT_EXCEED
                        ),
                        spaceId3 to SpaceSyncUpdate.Update(
                            id = spaceId3,
                            status = SpaceSyncStatus.SYNCED,
                            network = SpaceSyncNetwork.ANYTYPE,
                            syncingObjectsCounter = 2345,
                            error = SpaceSyncError.NULL
                        )
                    ),
                    actual = awaitItem()
                )
            }
        }
    }
}