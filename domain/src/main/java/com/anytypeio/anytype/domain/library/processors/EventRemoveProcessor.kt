package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject

class EventRemoveProcessor : SubscriptionEventProcessor<SubscriptionEvent.Remove> {

    override fun process(
        event: SubscriptionEvent.Remove,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        retainAll {
            it.id != event.target
        }
        return this
    }

}