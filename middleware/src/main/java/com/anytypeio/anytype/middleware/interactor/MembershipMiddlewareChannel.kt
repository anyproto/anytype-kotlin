package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.data.auth.event.MembershipRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

class MembershipMiddlewareChannel(
    private val eventsProxy: EventProxy
): MembershipRemoteChannel {

    override fun observe(): Flow<List<Membership.Event>> {
        return eventsProxy.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    when {
                        message.membershipUpdate != null -> {
                            val event = message.membershipUpdate
                            checkNotNull(event)
                            val membership = event.data_
                            if (membership != null) {
                                Membership.Event.Update(
                                    membership = membership.toCoreModel()
                                )
                            } else {
                                null
                            }
                        }
                        else -> null
                    }
                }
            }.filter { events -> events.isNotEmpty() }
    }
}