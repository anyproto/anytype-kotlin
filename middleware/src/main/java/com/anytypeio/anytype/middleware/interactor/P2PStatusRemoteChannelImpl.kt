package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.data.auth.status.P2PStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class P2PStatusRemoteChannelImpl(private val events: EventProxy) : P2PStatusRemoteChannel {

    override fun observe(activeSpaceId: Id): Flow<List<P2PStatusUpdate>> {
        return events.flow().mapNotNull { emission ->
            emission.messages.mapNotNull { message ->
                when {
                    message.p2pStatusUpdate != null -> {
                        val event = message.p2pStatusUpdate
                        checkNotNull(event)
                        if (event.spaceId == activeSpaceId) {
                            P2PStatusUpdate(
                                spaceId = event.spaceId,
                                status = event.status.toCoreModel(),
                                devicesCounter = event.devicesCounter
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