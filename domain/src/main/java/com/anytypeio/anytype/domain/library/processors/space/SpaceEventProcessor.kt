package com.anytypeio.anytype.domain.library.processors.space

import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.space.SpaceSubscriptionObject
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset

interface SpaceSubscriptionEventProcessor<T: SubscriptionEvent> {
    fun process(
        event: T,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject>
}

class SpaceEventAddProcessor : SpaceSubscriptionEventProcessor<SubscriptionEvent.Add> {

    override fun process(
        event: SubscriptionEvent.Add,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = with(dataItems) {
        val afterId = event.afterId
        if (afterId != null) {
            val afterIdx = indexOfFirst { afterId == it.id }
            if (afterIdx != -1) {
                add(afterIdx.inc(), SpaceSubscriptionObject(event.target))
            } else {
                add(0, SpaceSubscriptionObject(event.target))
            }
        } else {
            add(0, SpaceSubscriptionObject(event.target))
        }
        return this
    }
}

class SpaceEventAmendProcessor(private val logger: Logger) :
    SpaceSubscriptionEventProcessor<SubscriptionEvent.Amend> {

    override fun process(
        event: SubscriptionEvent.Amend,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = with(dataItems) {

        val item = find { it.id == event.target }
        val index = indexOf(item)

        if (index != -1) {
            if (item?.spaceView != null) {
                set(
                    index,
                    SpaceSubscriptionObject(
                        id = item.id,
                        spaceView = item.spaceView.amend(event.diff)
                    )
                )
            } else {
                set(
                    index,
                    SpaceSubscriptionObject(
                        id = item?.id ?: event.target,
                        spaceView = SpaceView(event.diff)
                    )
                )
            }
        } else {
            logger.logWarning("SpaceEventAmendProcessor warning. Item with id:${item?.id} is not found in ArrayList:{${this.map { it.id }}}")
        }
        return this
    }
}

class SpaceEventPositionProcessor: SpaceSubscriptionEventProcessor<SubscriptionEvent.Position> {

    override fun process(
        event: SubscriptionEvent.Position,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = with(dataItems) {
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

class SpaceEventRemoveProcessor : SpaceSubscriptionEventProcessor<SubscriptionEvent.Remove> {

    override fun process(
        event: SubscriptionEvent.Remove,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = with(dataItems) {
        retainAll {
            it.id != event.target
        }
        return this
    }
}

class SpaceEventSetProcessor : SpaceSubscriptionEventProcessor<SubscriptionEvent.Set> {

    override fun process(
        event: SubscriptionEvent.Set,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = with(dataItems) {
        val indexOfItem = indexOfFirst { it.id == event.target }
        if (indexOfItem != -1) {
            set(
                indexOfItem,
                SpaceSubscriptionObject(
                    event.target,
                    SpaceView(event.data)
                )
            )
        } else {
            add(
                0,
                SpaceSubscriptionObject(
                    event.target,
                    SpaceView(event.data)
                )
            )
        }
        return this
    }

}

class SpaceEventUnsetProcessor : SpaceSubscriptionEventProcessor<SubscriptionEvent.Unset> {

    override fun process(
        event: SubscriptionEvent.Unset,
        dataItems: MutableList<SpaceSubscriptionObject>
    ): MutableList<SpaceSubscriptionObject> = dataItems.map {
        if (it.id == event.target) {
            SpaceSubscriptionObject(it.id, it.spaceView?.unset(event.keys))
        } else {
            it
        }
    }.toMutableList()
}