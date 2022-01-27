package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset

interface ObjectStore {

    val size : Int

    fun get(target: Id) : ObjectWrapper.Basic?

    fun amend(
        target: Id,
        diff: Map<Id, Any?>,
        subscriptions: List<Id>
    )

    fun unset(
        target: Id,
        keys: List<Id>,
        subscriptions: List<Id>
    )

    fun set(
        target: Id,
        data: Map<String, Any?>,
        subscriptions: List<Id>
    )

    fun merge(
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic>,
        subscriptions: List<Id>
    )

    /**
     * Registers [subscription] for [target]
     */
    fun subscribe(subscription: Id, target: Id)

    /**
     * Unregister [subscriptions] and clear store if needed.
     */
    fun unsubscribe(subscriptions: List<Id>)

    /**
     * Remove object if this object only has this [subscription]
     */
    fun unsubscribe(subscription: Id, target: Id)
}

/**
 * TODO make store thread-safe.
 */
class DefaultObjectStore : ObjectStore {

    override val size: Int get() = map.size

    val map = mutableMapOf<Id, Holder>()

    override fun get(target: Id): ObjectWrapper.Basic? {
        return map[target]?.obj
    }

    override fun merge(
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic>,
        subscriptions: List<Id>
    ) {
        objects.forEach { o ->
            val current = map[o.id]
            if (current == null) {
                map[o.id] = Holder(
                    obj = o,
                    subscriptions = subscriptions
                )
            } else {
                map[o.id] = current.copy(
                    obj = current.obj.amend(o.map),
                    subscriptions = current.subscriptions + subscriptions
                )
            }
        }
        dependencies.forEach { d ->
            val current = map[d.id]
            if (current == null) {
                map[d.id] = Holder(
                    obj = d,
                    subscriptions = subscriptions.map {
                        it + DEPENDENT_SUBSRIPTION_POSTFIX
                    }
                )
            } else {
                map[d.id] = current.copy(
                    obj = current.obj.amend(d.map),
                    subscriptions = current.subscriptions + subscriptions.map {
                        it + DEPENDENT_SUBSRIPTION_POSTFIX
                    }
                )
            }
        }
    }

    override fun amend(
        target: Id,
        diff: Map<Id, Any?>,
        subscriptions: List<Id>
    ) {
        val current = map[target]
        if (current != null) {
            map[target] = current.copy(
                obj = current.obj.amend(diff),
                subscriptions = subscriptions
            )
        } else {
            map[target] = Holder(
                obj = ObjectWrapper.Basic(diff),
                subscriptions = subscriptions
            )
        }
    }

    override fun unset(
        target: Id,
        keys: List<Id>,
        subscriptions: List<Id>
    ) {
        val current = map[target]
        if (current != null) {
            map[target] = current.copy(
                obj = current.obj.unset(keys),
                subscriptions = subscriptions
            )
        }
    }

    override fun set(
        target: Id,
        data: Map<String, Any?>,
        subscriptions: List<Id>
    ) {
        map[target] = Holder(
            obj = ObjectWrapper.Basic(data),
            subscriptions = subscriptions
        )
    }

    override fun subscribe(subscription: Id, target: Id) {
        val current = map[target]
        if (current != null) {
            if (!current.subscriptions.contains(subscription)) {
                map[target] = current.copy(
                    subscriptions = current.subscriptions + listOf(subscription)
                )
            }
        }
    }

    override fun unsubscribe(subscriptions: List<Id>) {
        val all = subscriptions + subscriptions.map { "$it$DEPENDENT_SUBSRIPTION_POSTFIX" }
        val unsubscribed = mutableListOf<Id>()
        map.forEach { (id, holder) ->
            val remaining = (holder.subscriptions - all)
            if (remaining.isEmpty())
                unsubscribed.add(id)
            else {
                map[id] = holder.copy(
                    subscriptions = remaining
                )
            }
        }
        unsubscribed.forEach { id ->
            map.remove(id)
        }
    }

    override fun unsubscribe(subscription: Id, target: Id) {
        val current = map[target]
        if (current != null) {
            if (current.subscriptions.firstOrNull() == subscription) {
                map.remove(target)
            } else {
                map[target] = current.copy(
                    subscriptions = current.subscriptions.filter { id ->
                        id != subscription
                    }
                )
            }
        }
    }

    /**
     * @property [subscriptions] ids of subscriptions registered for this [obj]
     */
    data class Holder(
        val obj: ObjectWrapper.Basic,
        val subscriptions: List<Id>
    )

    companion object {
        const val DEPENDENT_SUBSRIPTION_POSTFIX = "/dep"
    }
}