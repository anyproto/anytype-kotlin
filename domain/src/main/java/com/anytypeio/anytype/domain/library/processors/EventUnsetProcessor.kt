package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject
import com.anytypeio.anytype.domain.`object`.unset

class EventUnsetProcessor : SubscriptionEventProcessor<SubscriptionEvent.Unset> {

    override fun process(
        event: SubscriptionEvent.Unset,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        for (index in indices) {
            val item = get(index)
            if (item.id == event.target) {
                set(index, SubscriptionObject(item.id, item.objectWrapper?.unset(event.keys)))
            }
        }
        return this
    }

}