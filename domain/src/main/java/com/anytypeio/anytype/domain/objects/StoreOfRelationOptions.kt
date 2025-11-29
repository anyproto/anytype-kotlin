package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
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

interface StoreOfRelationOptions {
    val size: Int
    suspend fun observe(): Flow<Map<Id, ObjectWrapper.Option>>
    suspend fun getById(id: Id): ObjectWrapper.Option?
    suspend fun getById(ids: List<Id>): List<ObjectWrapper.Option>
    suspend fun getByRelationKey(key: Key): List<ObjectWrapper.Option>
    suspend fun getAll(): List<ObjectWrapper.Option>
    suspend fun merge(options: List<ObjectWrapper.Option>)
    suspend fun amend(target: Id, diff: Map<Id, Any?>)
    suspend fun unset(target: Id, keys: List<Key>)
    suspend fun set(target: Id, data: Struct)
    suspend fun remove(target: Id)
    suspend fun clear()

    fun trackChanges(): Flow<TrackedEvent>

    sealed class TrackedEvent {
        object Init : TrackedEvent()
        object Change : TrackedEvent()
    }
}

class DefaultStoreOfRelationOptions : StoreOfRelationOptions {

    private val mutex = Mutex()
    private val store = mutableMapOf<Id, ObjectWrapper.Option>()
    private val relationKeyToIds = mutableMapOf<Key, MutableSet<Id>>()
    private val atomicSize = AtomicInteger(0)

    private val updates = MutableSharedFlow<StoreOfRelationOptions.TrackedEvent>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val size: Int get() = atomicSize.get()

    override suspend fun getById(id: Id): ObjectWrapper.Option? = mutex.withLock {
        store[id]
    }

    override suspend fun getById(ids: List<Id>): List<ObjectWrapper.Option> = mutex.withLock {
        ids.mapNotNull { id -> store[id] }
    }

    override suspend fun getByRelationKey(key: Key): List<ObjectWrapper.Option> = mutex.withLock {
        relationKeyToIds[key]?.mapNotNull { id -> store[id] } ?: emptyList()
    }

    override suspend fun getAll(): List<ObjectWrapper.Option> = mutex.withLock {
        store.values.toList()
    }

    override suspend fun merge(options: List<ObjectWrapper.Option>) {
        var changed = false
        var added = 0
        mutex.withLock {
            options.forEach { option ->
                val current = store[option.id]
                if (current == null) {
                    store[option.id] = option
                    addToRelationKeyIndex(option)
                    added++
                    changed = true
                } else {
                    val amended = current.amend(option.map)
                    if (amended !== current) {
                        // Update relationKey index if relationKey changed
                        val oldRelationKey = current.relationKey
                        val newRelationKey = amended.relationKey
                        if (oldRelationKey != newRelationKey) {
                            removeFromRelationKeyIndex(current)
                            addToRelationKeyIndex(amended)
                        }
                        store[option.id] = amended
                        changed = true
                    }
                }
            }
            if (added > 0) atomicSize.addAndGet(added)
        }
        if (changed) updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override suspend fun amend(target: Id, diff: Map<Id, Any?>) {
        var changed = false
        var inserted = false
        mutex.withLock {
            val current = store[target]
            val amended = current?.amend(diff) ?: ObjectWrapper.Option(diff).also {
                addToRelationKeyIndex(it)
                inserted = true
            }
            if (amended !== current) {
                if (current != null) {
                    val oldRelationKey = current.relationKey
                    val newRelationKey = amended.relationKey
                    if (oldRelationKey != newRelationKey) {
                        removeFromRelationKeyIndex(current)
                        addToRelationKeyIndex(amended)
                    }
                }
                store[target] = amended
                changed = true
                if (inserted) atomicSize.incrementAndGet()
            }
        }
        if (changed) updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override suspend fun set(target: Id, data: Struct) {
        var wasAbsent = false
        mutex.withLock {
            val existed = store[target]
            if (existed != null) {
                removeFromRelationKeyIndex(existed)
            }
            val newOption = ObjectWrapper.Option(data)
            store[target] = newOption
            addToRelationKeyIndex(newOption)
            wasAbsent = existed == null
            if (wasAbsent) atomicSize.incrementAndGet()
        }
        updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override suspend fun unset(target: Id, keys: List<Key>) {
        var changed = false
        mutex.withLock {
            val current = store[target]
            if (current != null) {
                val next = current.unset(keys)
                if (next !== current) {
                    // Update relationKey index if relationKey was unset
                    if (keys.contains(Relations.RELATION_KEY)) {
                        removeFromRelationKeyIndex(current)
                        addToRelationKeyIndex(next)
                    }
                    store[target] = next
                    changed = true
                }
            }
        }
        if (changed) updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override suspend fun remove(target: Id) {
        var removed = false
        mutex.withLock {
            val current = store.remove(target)
            if (current != null) {
                removeFromRelationKeyIndex(current)
                removed = true
                atomicSize.decrementAndGet()
            }
        }
        if (removed) updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override suspend fun clear() {
        var hadItems = false
        mutex.withLock {
            hadItems = store.isNotEmpty()
            if (hadItems) {
                relationKeyToIds.clear()
                store.clear()
                atomicSize.set(0)
            }
        }
        if (hadItems) updates.tryEmit(StoreOfRelationOptions.TrackedEvent.Change)
    }

    override fun trackChanges(): Flow<StoreOfRelationOptions.TrackedEvent> = updates.onStart {
        emit(StoreOfRelationOptions.TrackedEvent.Init)
    }

    override suspend fun observe(): Flow<Map<Id, ObjectWrapper.Option>> {
        return trackChanges().map {
            mutex.withLock { store.toMap() }
        }
    }

    private fun addToRelationKeyIndex(option: ObjectWrapper.Option) {
        val relationKey = option.relationKey
        if (relationKey != null) {
            relationKeyToIds.getOrPut(relationKey) { mutableSetOf() }.add(option.id)
        }
    }

    private fun removeFromRelationKeyIndex(option: ObjectWrapper.Option) {
        val relationKey = option.relationKey
        if (relationKey != null) {
            relationKeyToIds[relationKey]?.remove(option.id)
            if (relationKeyToIds[relationKey]?.isEmpty() == true) {
                relationKeyToIds.remove(relationKey)
            }
        }
    }
}
