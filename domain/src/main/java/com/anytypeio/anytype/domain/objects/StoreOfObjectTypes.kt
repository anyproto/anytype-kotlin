package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes.TrackedEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface StoreOfObjectTypes {

    fun observe(id: Id) : Flow<ObjectWrapper.Type>
    fun observe() : Flow<List<ObjectWrapper.Type>>
    
    /**
     * Observes all object types and maps to a property using the provided mapper.
     * Applies distinctUntilChanged to only emit when the mapped value changes.
     * 
     * @param keys List of relation keys being observed (informational, for documentation)
     * @param mapper Function to extract the desired property from the list of types
     * @return Flow that emits only when the mapped value changes
     */
    fun <T> observe(
        keys: List<String>,
        mapper: (List<ObjectWrapper.Type>) -> T
    ): Flow<T>
    
    /**
     * Observes a specific object type and maps to a property using the provided mapper.
     * Applies distinctUntilChanged to only emit when the mapped value changes.
     * 
     * @param id The type ID to observe
     * @param keys List of relation keys being observed (informational, for documentation)
     * @param mapper Function to extract the desired property from the type
     * @return Flow that emits only when the mapped value changes
     */
    fun <T> observe(
        id: Id,
        keys: List<String>,
        mapper: (ObjectWrapper.Type) -> T
    ): Flow<T>
    
    suspend fun get(id: Id): ObjectWrapper.Type?
    suspend fun getByKey(key: Key): ObjectWrapper.Type?
    suspend fun getAll(): List<ObjectWrapper.Type>
    suspend fun merge(types: List<ObjectWrapper.Type>)
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

class DefaultStoreOfObjectTypes : StoreOfObjectTypes {

    private val mutex = Mutex()
    private val store = mutableMapOf<Id, ObjectWrapper.Type>()

    private val updates = MutableSharedFlow<TrackedEvent>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun observe(id: Id): Flow<ObjectWrapper.Type> = flow {
        val init = get(id)
        if (init != null && init.isValid) {
            emit(init)
        }
        emitAll(
            trackChanges()
                .mapNotNull { get(id) }
                .filter { it.isValid }
        )
    }

    override fun observe(): Flow<List<ObjectWrapper.Type>> = flow {
        emitAll(trackChanges().map { getAll() })
    }
    
    override fun <T> observe(
        keys: List<String>,
        mapper: (List<ObjectWrapper.Type>) -> T
    ): Flow<T> {
        return observe()
            .map(mapper)
            .distinctUntilChanged()
    }
    
    override fun <T> observe(
        id: Id,
        keys: List<String>,
        mapper: (ObjectWrapper.Type) -> T
    ): Flow<T> {
        return observe(id)
            .map(mapper)
            .distinctUntilChanged()
    }

    override suspend fun get(id: Id): ObjectWrapper.Type? = mutex.withLock {
        store[id]
    }

    override suspend fun getAll(): List<ObjectWrapper.Type> = mutex.withLock {
        store.values.toList()
    }

    override suspend fun getByKey(key: Key): ObjectWrapper.Type? =
        mutex.withLock { store.values.firstOrNull { it.uniqueKey == key } }

    override suspend fun merge(types: List<ObjectWrapper.Type>) {
        var changed = false
        mutex.withLock {
            types.forEach { o ->
                val current = store[o.id]
                store[o.id] = if (current == null) {
                    changed = true
                    o
                } else {
                    val amended = current.amend(o.map)
                    if (amended !== current) changed = true
                    amended
                }
            }
        }
        if (changed) {
            updates.tryEmit(TrackedEvent.Change)
        }
    }

    override suspend fun amend(target: Id, diff: Map<Id, Any?>) {
        var changed = false
        mutex.withLock {
            val current = store[target]
            val amended = current?.amend(diff) ?: ObjectWrapper.Type(diff)
            if (amended !== current) {
                store[target] = amended
                changed = true
            }
        }
        if (changed) updates.tryEmit(TrackedEvent.Change)
    }

    override suspend fun set(
        target: Id,
        data: Struct
    ) {
        mutex.withLock {
            store[target] = ObjectWrapper.Type(data)
        }
        updates.tryEmit(TrackedEvent.Change)
    }

    override suspend fun unset(
        target: Id,
        keys: List<Key>
    ): Unit {
        var changed = false
        mutex.withLock {
            val current = store[target]
            if (current != null) {
                store[target] = current.unset(keys)
                changed = true
            }
        }
        if (changed) updates.tryEmit(TrackedEvent.Change)
    }

    override suspend fun remove(target: Id) {
        var removed = false
        mutex.withLock {
            removed = store.remove(target) != null
        }
        if (removed) updates.tryEmit(TrackedEvent.Change)
    }

    override suspend fun clear() {
        val hadItems: Boolean
        mutex.withLock {
            hadItems = store.isNotEmpty()
            store.clear()
        }
        if (hadItems) updates.tryEmit(TrackedEvent.Change)
    }

    override fun trackChanges(): Flow<TrackedEvent> = updates.onStart {
        emit(TrackedEvent.Init)
    }
}

suspend fun StoreOfObjectTypes.getTypeOfObject(obj: ObjectWrapper.Basic): ObjectWrapper.Type? {
    val typeId = obj.type.firstOrNull()
    return if (typeId != null) {
        return get(typeId)
    } else {
        null
    }
}

/**
 * Shared helper to build the limit object types for a property.
 */
suspend fun StoreOfObjectTypes.mapLimitObjectTypes(
    property: ObjectWrapper.Relation,
): List<Id> {
    return if (property.format == RelationFormat.OBJECT && property.relationFormatObjectTypes.isNotEmpty()) {
        property.relationFormatObjectTypes.mapNotNull { id ->
            get(id)?.let { objType ->
                if (objType.isValid) {
                    objType.id
                } else {
                    null
                }
            }
        }
    } else {
        emptyList()
    }
}