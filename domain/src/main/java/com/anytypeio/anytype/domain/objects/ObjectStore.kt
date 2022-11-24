package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ObjectStore {

    val size : Int

    suspend fun get(target: Id) : ObjectWrapper.Basic?

    suspend fun getAll() : List<ObjectWrapper.Basic>
    suspend fun getAllAsRelations() : List<ObjectWrapper.Relation>

    // Temporary API until we introduce proper interface for relations store
    suspend fun getRelationById(id: Id) : ObjectWrapper.Relation?

    suspend fun amend(
        target: Id,
        diff: Map<Id, Any?>,
        subscriptions: List<Id>
    )

    suspend fun unset(
        target: Id,
        keys: List<Id>,
        subscriptions: List<Id>
    )

    suspend fun set(
        target: Id,
        data: Map<String, Any?>,
        subscriptions: List<Id>
    )

    suspend fun merge(
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic>,
        subscriptions: List<Id>
    )

    /**
     * Registers [subscription] for [target]
     */
    suspend fun subscribe(subscription: Id, target: Id)

    /**
     * Unregister [subscriptions] and clear store if needed.
     */
    suspend fun unsubscribe(subscriptions: List<Id>)

    /**
     * Remove object if this object only has this [subscription]
     */
    suspend fun unsubscribe(subscription: Id, target: Id)
}

class DefaultObjectStore : ObjectStore {

    private val mutex = Mutex()

    override val size: Int get() = map.size

    val map = mutableMapOf<Id, Holder>()

    override suspend fun get(target: Id): ObjectWrapper.Basic? = mutex.withLock {
        return map[target]?.obj
    }

    override suspend fun getAll(): List<ObjectWrapper.Basic> {
        return map.values.map { it.obj }
    }

    override suspend fun getAllAsRelations(): List<ObjectWrapper.Relation> {
        return map.values.map { ObjectWrapper.Relation(it.obj.map) }
    }

    override suspend fun getRelationById(id: Id): ObjectWrapper.Relation? {
        return map[id]?.obj?.map?.let { ObjectWrapper.Relation(it) }
    }

    override suspend fun merge(
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic>,
        subscriptions: List<Id>
    ) = mutex.withLock {
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
                        it + DEPENDENT_SUBSCRIPTION_POSTFIX
                    }
                )
            } else {
                map[d.id] = current.copy(
                    obj = current.obj.amend(d.map),
                    subscriptions = current.subscriptions + subscriptions.map {
                        it + DEPENDENT_SUBSCRIPTION_POSTFIX
                    }
                )
            }
        }
    }

    override suspend fun amend(
        target: Id,
        diff: Map<Id, Any?>,
        subscriptions: List<Id>
    ) = mutex.withLock {
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

    override suspend fun unset(
        target: Id,
        keys: List<Id>,
        subscriptions: List<Id>
    ) = mutex.withLock {
        val current = map[target]
        if (current != null) {
            map[target] = current.copy(
                obj = current.obj.unset(keys),
                subscriptions = subscriptions
            )
        }
    }

    override suspend fun set(
        target: Id,
        data: Map<String, Any?>,
        subscriptions: List<Id>
    ) = mutex.withLock {
        map[target] = Holder(
            obj = ObjectWrapper.Basic(data),
            subscriptions = subscriptions
        )
    }

    override suspend fun subscribe(subscription: Id, target: Id) = mutex.withLock {
        val current = map[target]
        if (current != null) {
            if (!current.subscriptions.contains(subscription)) {
                map[target] = current.copy(
                    subscriptions = current.subscriptions + listOf(subscription)
                )
            }
        }
    }

    override suspend fun unsubscribe(subscriptions: List<Id>) = mutex.withLock {
        val all = subscriptions + subscriptions.map { "$it$DEPENDENT_SUBSCRIPTION_POSTFIX" }
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

    override suspend fun unsubscribe(subscription: Id, target: Id) = mutex.withLock {
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
        const val DEPENDENT_SUBSCRIPTION_POSTFIX = "/dep"
    }
}