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

        val item = find { it.id == event.target }
        val index = indexOf(item)

        if (index != -1) {
            if (item?.objectWrapper != null) {
                set(
                    index,
                    SubscriptionObject(
                        id = item.id,
                        objectWrapper = item.objectWrapper.amend(event.diff)
                    )
                )
            } else {
                set(
                    index,
                    SubscriptionObject(
                        id = item?.id ?: event.target,
                        objectWrapper = ObjectWrapper.Basic(event.diff)
                    )
                )
            }
        } else {
            logger.logWarning("EventAmendProcessor warning. Item with id:${item?.id} is not found in ArrayList:{${this.map { it.id }}}")
        }
        return this
    }

}