package com.anytypeio.anytype.domain.library.processors

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.SubscriptionObject
import com.anytypeio.anytype.domain.`object`.amend

class EventAmendProcessor(private val logger: Logger) : SubscriptionEventProcessor<SubscriptionEvent.Amend> {

    override fun process(
        event: SubscriptionEvent.Amend,
        dataItems: MutableList<SubscriptionObject>
    ): MutableList<SubscriptionObject> = with(dataItems) {

        val index = indexOfFirst { it.id == event.target }

        if (index != -1) {
            val item = get(index)
            val objectWrapper = item.objectWrapper
            if (objectWrapper != null) {
                set(
                    index,
                    SubscriptionObject(
                        id = item.id,
                        objectWrapper = objectWrapper.amend(event.diff)
                    )
                )
            } else {
                set(
                    index,
                    SubscriptionObject(
                        id = item.id,
                        objectWrapper = ObjectWrapper.Basic(event.diff)
                    )
                )
            }
        } else {
            logger.logWarning("EventAmendProcessor warning. Item with id:${event.target} is not found")
        }
        return this
    }

}