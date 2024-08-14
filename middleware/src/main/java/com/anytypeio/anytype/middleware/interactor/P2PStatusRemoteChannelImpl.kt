package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.data.auth.status.P2PStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class P2PStatusRemoteChannelImpl(private val events: EventProxy) : P2PStatusRemoteChannel {

    override fun observe(): Flow<P2PStatusUpdate> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.lastOrNull()?.p2pStatusUpdate
            }
            .map { event ->
                P2PStatusUpdate.Update(
                    spaceId = event.spaceId,
                    status = event.status.toCoreModel(),
                    devicesCounter = event.devicesCounter
                )
            }
    }
}