package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject
import com.anytypeio.anytype.domain.`object`.unset

class EventUnsetProcessor : SubscriptionEventProcessor<SubscriptionEvent.Unset> {

    override fun process(
        event: SubscriptionEvent.Unset,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = dataItems.map {
        if (it.id == event.target) {
            SubscriptionObject(it.id, it.objectWrapper?.unset(event.keys))
        } else {
            it
        }
    }.toMutableList()

}