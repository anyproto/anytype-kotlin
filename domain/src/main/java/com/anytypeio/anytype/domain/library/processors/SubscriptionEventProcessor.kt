package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject

interface SubscriptionEventProcessor<T: SubscriptionEvent> {
    fun process(
        event: T,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject>
}