package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import kotlinx.coroutines.flow.Flow

interface SubscriptionEventRemoteChannel {
    fun subscribe(subscriptions: List<Id>): Flow<List<SubscriptionEvent>>
}