package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.data.auth.event.EventProcessRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class EventProcessMiddlewareChannel(
    private val events: EventProxy
) : EventProcessRemoteChannel {

    override fun observe(): Flow<List<Process.Event>> {
        return events.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    when {
                        message.processNew != null -> {
                            val event = message.processNew
                            checkNotNull(event)
                            Process.Event.New(
                                process = event.process?.toCoreModel()
                            )
                        }
                        message.processUpdate != null -> {
                            val event = message.processUpdate
                            checkNotNull(event)
                            Process.Event.Update(
                                process = event.process?.toCoreModel()
                            )
                        }
                        message.processDone != null -> {
                            val event = message.processDone
                            checkNotNull(event)
                            Process.Event.Done(
                                process = event.process?.toCoreModel()
                            )
                        }
                        else -> null
                    }
                }
            }
    }
}