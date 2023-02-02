package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.library.SubscriptionObject
import com.anytypeio.anytype.domain.`object`.amend

class EventAmendProcessor: SubscriptionEventProcessor<SubscriptionEvent.Amend> {

    override fun process(
        event: SubscriptionEvent.Amend,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {
        val item = find { it.id == event.target }
        if (item?.objectWrapper != null) {
            set(
                indexOf(item),
                SubscriptionObject(
                    id = item.id,
                    objectWrapper = item.objectWrapper.amend(event.diff)
                )
            )
        } else {
            set(
                indexOf(item),
                SubscriptionObject(
                    id = item?.id ?: event.target,
                    objectWrapper = ObjectWrapper.Basic(event.diff)
                )
            )
        }
        return this
    }

}