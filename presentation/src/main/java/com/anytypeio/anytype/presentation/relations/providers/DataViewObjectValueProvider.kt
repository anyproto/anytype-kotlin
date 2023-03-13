package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import kotlinx.coroutines.flow.map

class DataViewObjectValueProvider(
    private val db: ObjectSetDatabase,
) : ObjectValueProvider {
    override suspend fun get(target: Id): Map<String, Any?> {
        return db.store.get(target)?.map ?: emptyMap()
    }

    override suspend fun subscribe(target: Id) = db.observe(target).map { it.map }
}