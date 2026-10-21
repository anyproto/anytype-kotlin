package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject

class EventAddProcessor : SubscriptionEventProcessor<SubscriptionEvent.Add> {

    override fun process(
        event: SubscriptionEvent.Add,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        // Idempotent: the target can already be present when the add event was emitted
        // while the initial subscribe request was in flight and replayed on top of its results.
        // If the event's afterId implies a different position than the snapshot, the snapshot
        // order is kept; a subsequent Position event corrects it.
        if (any { it.id == event.target }) {
            return this
        }
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