package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.BufferOverflow

interface StoreOfRelations {
    val size: Int
    suspend fun observe(): Flow<Map<Id, ObjectWrapper.Relation>>
    suspend fun getByKey(key: Key): ObjectWrapper.Relation?
    suspend fun getByKeys(keys: List<Key>): List<ObjectWrapper.Relation>
    suspend fun getById(id: Id): ObjectWrapper.Relation?
    suspend fun getById(ids: List<Id>): List<ObjectWrapper.Relation>
    suspend fun getAll(): List<ObjectWrapper.Relation>
    suspend fun merge(relations: List<ObjectWrapper.Relation>)
    suspend fun amend(target: Id, diff: Map<Id, Any?>)
    suspend fun unset(target: Id, keys: List<Key>)
    suspend fun set(target: Id, data: Struct)
    suspend fun remove(target: Id)
    suspend fun clear()

    fun trackChanges() : Flow<TrackedEvent>

    sealed class TrackedEvent {
        object Init : TrackedEvent()
        object Change: TrackedEvent()
    }
}

class DefaultStoreOfRelations : StoreOfRelations {

    private val mutex = Mutex()
    private val store = mutableMapOf<Id, ObjectWrapper.Relation>()
    private val keysToIds = mutableMapOf<Key, Id>()
    private val atomicSize = AtomicInteger(0)

    private val updates = MutableSharedFlow<StoreOfRelations.TrackedEvent>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val size: Int get() = atomicSize.get()

    override suspend fun getByKey(key: Key): ObjectWrapper.Relation? = mutex.withLock {
        val id = keysToIds[key]
        if (id != null)
            store[id]
        else
            null
    }

    override suspend fun getByKeys(keys: List<Key>): List<ObjectWrapper.Relation> = mutex.withLock {
        keys.mapNotNull { key -> keysToIds[key]?.let { store[it] } }
    }

    override suspend fun getById(id: Id): ObjectWrapper.Relation? = mutex.withLock {
        store[id]
    }

    override suspend fun getById(ids: List<Id>): List<ObjectWrapper.Relation> = mutex.withLock {
        ids.mapNotNull { id -> store[id] }
    }

    override suspend fun getAll(): List<ObjectWrapper.Relation> = mutex.withLock {
        store.values.toList()
    }

    override suspend fun merge(relations: List<ObjectWrapper.Relation>) {
        var changed = false
        var added = 0
        mutex.withLock {
            relations.forEach { o ->
                val current = store[o.id]
                if (current == null) {
                    store[o.id] = o.also { keysToIds[it.key] = o.id }
                    added++
                    changed = true
                } else {
                    val amended = current.amend(o.map)
                    if (amended !== current) {
                        store[o.id] = amended
                        changed = true
                    }
                }
            }
            if (added > 0) atomicSize.addAndGet(added)
        }
        if (changed) updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override suspend fun amend(target: Id, diff: Map<Id, Any?>) {
        var changed = false
        var inserted = false
        mutex.withLock {
            val current = store[target]
            val amended = current?.amend(diff) ?: ObjectWrapper.Relation(diff).also {
                keysToIds[it.key] = target
                inserted = true
            }
            if (amended !== current) {
                store[target] = amended
                changed = true
                if (inserted) atomicSize.incrementAndGet()
            }
        }
        if (changed) updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override suspend fun set(
        target: Id,
        data: Struct
    ) {
        var wasAbsent = false
        mutex.withLock {
            val existed = store.containsKey(target)
            store[target] = ObjectWrapper.Relation(data).also { keysToIds[it.key] = target }
            wasAbsent = !existed
            if (wasAbsent) atomicSize.incrementAndGet()
        }
        updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override suspend fun unset(
        target: Id,
        keys: List<Key>
    ) {
        var changed = false
        mutex.withLock {
            val current = store[target]
            if (current != null) {
                val next = current.unset(keys)
                if (next !== current) {
                    store[target] = next
                    changed = true
                }
            }
        }
        if (changed) updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override suspend fun remove(target: Id) {
        var removed = false
        mutex.withLock {
            val current = store.remove(target)
            if (current != null) {
                keysToIds.remove(current.key)
                removed = true
                atomicSize.decrementAndGet()
            }
        }
        if (removed) updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override suspend fun clear() {
        var hadItems = false
        mutex.withLock {
            hadItems = store.isNotEmpty()
            if (hadItems) {
                keysToIds.clear()
                store.clear()
                atomicSize.set(0)
            }
        }
        if (hadItems) updates.tryEmit(StoreOfRelations.TrackedEvent.Change)
    }

    override fun trackChanges(): Flow<StoreOfRelations.TrackedEvent> = updates.onStart {
        emit(StoreOfRelations.TrackedEvent.Init)
    }

    override suspend fun observe(): Flow<Map<Id, ObjectWrapper.Relation>> {
        return trackChanges().map {
            mutex.withLock { store.toMap() }
        }
    }
}

suspend fun StoreOfRelations.getValidRelations(ids: List<Id>): List<ObjectWrapper.Relation> {
    return ids.mapNotNull { id ->
        getById(id)?.takeIf { it.isValidToUse }
    }
}