package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import kotlinx.coroutines.flow.Flow

/**
 * Channel for events related to changes and transformations of objects.
 * @see SubscriptionEvent
 */
interface SubscriptionEventChannel {
    fun subscribe(subscriptions: List<Id>): Flow<List<SubscriptionEvent>>
}