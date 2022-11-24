package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface StoreOfObjectTypes {
    val size: Int
    suspend fun get(id: Id): ObjectWrapper.Type?
    suspend fun getAll(): List<ObjectWrapper.Type>
    suspend fun merge(types: List<ObjectWrapper.Type>)
    suspend fun amend(target: Id, diff: Map<Id, Any?>)
    suspend fun unset(target: Id, keys: List<Key>)
    suspend fun set(target: Id, data: Struct)
    suspend fun remove(target: Id)
    suspend fun clear()
}

class DefaultStoreOfObjectTypes : StoreOfObjectTypes {

    private val mutex = Mutex()
    private val store = mutableMapOf<Id, ObjectWrapper.Type>()

    override val size: Int get() = store.size

    override suspend fun get(id: Id): ObjectWrapper.Type? = mutex.withLock {
        store[id]
    }

    override suspend fun getAll(): List<ObjectWrapper.Type> = mutex.withLock {
        store.values.toList()
    }

    override suspend fun merge(types: List<ObjectWrapper.Type>): Unit = mutex.withLock {
        types.forEach { o ->
            val current = store[o.id]
            if (current == null) {
                store[o.id] = o
            } else {
                store[o.id] = current.amend(o.map)
            }
        }
    }

    override suspend fun amend(target: Id, diff: Map<Id, Any?>): Unit = mutex.withLock {
        val current = store[target]
        if (current != null) {
            store[target] = current.amend(diff)
        } else {
            store[target] = ObjectWrapper.Type(diff)
        }
    }

    override suspend fun set(
        target: Id,
        data: Map<String, Any?>
    ): Unit = mutex.withLock {
        store[target] = ObjectWrapper.Type(data)
    }

    override suspend fun unset(
        target: Id,
        keys: List<Id>
    ): Unit = mutex.withLock {
        val current = store[target]
        if (current != null) {
            store[target] = current.unset(keys)
        }
    }

    override suspend fun remove(target: Id) : Unit = mutex.withLock {
        val current = store[target]
        if (current != null) {
            store.remove(target)
        }
    }

    override suspend fun clear(): Unit = mutex.withLock {
        store.clear()
    }
}