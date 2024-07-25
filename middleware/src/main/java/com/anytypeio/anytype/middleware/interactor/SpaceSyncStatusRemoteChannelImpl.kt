package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.data.auth.status.SpaceStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class SpaceSyncStatusRemoteChannelImpl(private val events: EventProxy) : SpaceStatusRemoteChannel {

    override fun observe(activeSpaceId: Id): Flow<SpaceSyncUpdate> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.lastOrNull()?.spaceSyncStatusUpdate
            }
            .filter { event -> event.id == activeSpaceId }
            .map { event ->
                SpaceSyncUpdate.Update(
                    id = event.id,
                    status = event.status.toCoreModel(),
                    network = event.network.toCoreModel(),
                    error = event.error.toCoreModel(),
                    syncingObjectsCounter = event.syncingObjectsCounter
                )
            }
    }
}