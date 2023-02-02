package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject
import com.anytypeio.anytype.domain.`object`.unset

class EventUnsetProcessor : SubscriptionEventProcessor<SubscriptionEvent.Unset> {

    override fun process(
        event: SubscriptionEvent.Unset,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        val item = find { it.id == event.target }?.let {
            SubscriptionObject(it.id, it.objectWrapper.apply {
                this?.unset(event.keys)
            })
        }
        if (item != null) {
            set(indexOf(item), item)
        }
        return this
    }

}