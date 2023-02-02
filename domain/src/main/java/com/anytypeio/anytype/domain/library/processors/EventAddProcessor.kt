package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject

class EventAddProcessor : SubscriptionEventProcessor<SubscriptionEvent.Add> {

    override fun process(
        event: SubscriptionEvent.Add,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        val afterId = event.afterId
        if (afterId != null) {
            val afterIdx = indexOfFirst { afterId == it.id }
            if (afterIdx != -1) {
                add(afterIdx.inc(), SubscriptionObject(event.target))
            } else {
                add(0, SubscriptionObject(event.target))
            }
        } else {
            add(0, SubscriptionObject(event.target))
        }
        return this
    }
    
}