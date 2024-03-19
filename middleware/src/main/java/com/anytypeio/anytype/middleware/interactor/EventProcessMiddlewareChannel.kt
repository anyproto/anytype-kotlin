package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.ProcessEvent
import com.anytypeio.anytype.data.auth.event.EventProcessRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class EventProcessMiddlewareChannel(
    private val events: EventProxy
) : EventProcessRemoteChannel {

    override fun observe(): Flow<List<ProcessEvent>> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    when {
                        message.processNew != null -> {
                            val event = message.processNew
                            checkNotNull(event)
                            ProcessEvent.New(
                                process = event.process?.toCoreModel()
                            )
                        }
                        message.processUpdate != null -> {
                            val event = message.processUpdate
                            checkNotNull(event)
                            ProcessEvent.Update(
                                process = event.process?.toCoreModel()
                            )
                        }
                        message.processDone != null -> {
                            val event = message.processDone
                            checkNotNull(event)
                            ProcessEvent.Done(
                                process = event.process?.toCoreModel()
                            )
                        }
                        else -> null
                    }
                }
            }
    }
}