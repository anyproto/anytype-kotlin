package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

interface ObjectStore {

    val size: Int

    suspend fun get(target: Id): ObjectWrapper.Basic?

    suspend fun getAll(): List<ObjectWrapper.Basic>
    suspend fun getAllAsRelations(): List<ObjectWrapper.Relation>

    // Temporary API until we introduce proper interface for relations store
    suspend fun getRelationById(id: Id): ObjectWrapper.Relation?

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

    override val size: Int get() = atomicSize.get()

    val map = mutableMapOf<Id, Holder>()

    private val atomicSize = AtomicInteger(0)

    override suspend fun get(target: Id): ObjectWrapper.Basic? = mutex.withLock {
        return map[target]?.obj
    }

    override suspend fun getAll(): List<ObjectWrapper.Basic> {
        return mutex.withLock { map.values.map { it.obj }.toList() }
    }

    override suspend fun getAllAsRelations(): List<ObjectWrapper.Relation> {
        return mutex.withLock { map.values.map { ObjectWrapper.Relation(it.obj.map) }.toList() }
    }

    override suspend fun getRelationById(id: Id): ObjectWrapper.Relation? {
        return mutex.withLock { map[id]?.obj?.map?.let { ObjectWrapper.Relation(it) } }
    }

    override suspend fun merge(
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic>,
        subscriptions: List<Id>
    ) {
        mutex.withLock {
            var added = 0
            objects.forEach { o ->
                val current = map[o.id]
                if (current == null) {
                    map[o.id] = Holder(
                        obj = o,
                        subscriptions = subscriptions.distinct()
                    )
                    added++
                } else {
                    map[o.id] = current.copy(
                        obj = current.obj.amend(o.map),
                        subscriptions = (current.subscriptions + subscriptions).distinct()
                    )
                }
            }
            dependencies.forEach { d ->
                val depSubs = subscriptions.map { it + DEPENDENT_SUBSCRIPTION_POSTFIX }
                val current = map[d.id]
                if (current == null) {
                    map[d.id] = Holder(
                        obj = d,
                        subscriptions = depSubs.distinct()
                    )
                    added++
                } else {
                    map[d.id] = current.copy(
                        obj = current.obj.amend(d.map),
                        subscriptions = (current.subscriptions + depSubs).distinct()
                    )
                }
            }
            if (added > 0) atomicSize.addAndGet(added)
        }
    }

    override suspend fun amend(
        target: Id,
        diff: Map<Id, Any?>,
        subscriptions: List<Id>
    ) {
        mutex.withLock {
            val current = map[target]
            if (current != null) {
                map[target] = current.copy(
                    obj = current.obj.amend(diff),
                    subscriptions = (current.subscriptions + subscriptions).distinct()
                )
            } else {
                map[target] = Holder(
                    obj = ObjectWrapper.Basic(diff),
                    subscriptions = subscriptions.distinct()
                )
                atomicSize.incrementAndGet()
            }
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
                subscriptions = (current.subscriptions + subscriptions).distinct()
            )
        }
    }

    override suspend fun set(
        target: Id,
        data: Map<String, Any?>,
        subscriptions: List<Id>
    ) {
        mutex.withLock {
            val current = map[target]
            if (current == null) {
                map[target] = Holder(
                    obj = ObjectWrapper.Basic(data),
                    subscriptions = subscriptions.distinct()
                )
                atomicSize.incrementAndGet()
            } else {
                map[target] = current.copy(
                    obj = ObjectWrapper.Basic(data),
                    subscriptions = (current.subscriptions + subscriptions).distinct()
                )
            }
        }
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
            if (map.remove(id) != null) {
                atomicSize.decrementAndGet()
            }
        }
    }

    override suspend fun unsubscribe(subscription: Id, target: Id) = mutex.withLock {
        val current = map[target]
        if (current != null) {
            if (current.subscriptions.firstOrNull() == subscription) {
                if (map.remove(target) != null) atomicSize.decrementAndGet()
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