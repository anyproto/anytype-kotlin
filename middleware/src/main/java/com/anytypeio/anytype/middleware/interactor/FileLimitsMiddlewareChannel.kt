package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.data.auth.event.FileLimitsRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class FileLimitsMiddlewareChannel(
    private val events: EventProxy
) : FileLimitsRemoteChannel {

    override fun observe(): Flow<List<FileLimitsEvent>> =
        events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    when {
                        message.fileSpaceUsage != null -> {
                            val event = message.fileSpaceUsage
                            checkNotNull(event)
                            FileLimitsEvent.SpaceUsage(
                                bytesUsage = event.bytesUsage
                            )
                        }
                        message.fileLocalUsage != null -> {
                            val event = message.fileLocalUsage
                            checkNotNull(event)
                            FileLimitsEvent.LocalUsage(
                                bytesUsage = event.localBytesUsage
                            )
                        }
                        message.fileLimitReached!= null -> {
                            val event = message.fileLimitReached
                            checkNotNull(event)
                            FileLimitsEvent.FileLimitReached(
                                fileId = event.fileId,
                                spaceId = event.spaceId
                            )
                        }
                        else -> null
                    }
                }
            }
}