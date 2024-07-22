package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.data.auth.status.SpaceStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SpaceSyncStatusRemoteChannelImpl(private val events: EventProxy) : SpaceStatusRemoteChannel {

    override fun observe(activeSpaceId: Id): Flow<List<SpaceSyncUpdate>> {
        return events.flow().mapNotNull { emission ->
            emission.messages.mapNotNull { message ->
                when {
                    message.spaceSyncStatusUpdate != null -> {
                        val event = message.spaceSyncStatusUpdate
                        checkNotNull(event)
                        if (event.id == activeSpaceId) {
                            SpaceSyncUpdate(
                                id = event.id,
                                status = event.status.toCoreModel(),
                                network = event.network.toCoreModel(),
                                error = event.error.toCoreModel(),
                                syncingObjectsCounter = event.syncingObjectsCounter
                            )
                        } else {
                            null
                        }
                    }

                    else -> null
                }
            }
        }
    }
}