package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel

class SubscriptionDataChannel(
    private val remote: SubscriptionEventRemoteChannel
) : SubscriptionEventChannel {
    override fun subscribe(subscriptions: List<Id>) = remote.subscribe(subscriptions)
}