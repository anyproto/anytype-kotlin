package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject

class EventPositionProcessor: SubscriptionEventProcessor<SubscriptionEvent.Position> {

    override fun process(
        event: SubscriptionEvent.Position,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        val itemToMove = find { it.id == event.target }
        if (itemToMove != null) {
            remove(itemToMove)
            val afterIdx = indexOfFirst { event.afterId == it.id }
            if (afterIdx != -1) {
                add(afterIdx.inc(), itemToMove)
            } else {
                add(0, itemToMove)
            }
        }
        return this
    }

}