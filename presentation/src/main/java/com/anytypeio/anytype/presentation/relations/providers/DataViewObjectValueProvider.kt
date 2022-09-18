package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewerById
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class DataViewObjectValueProvider(
    private val db: ObjectSetDatabase,
) : ObjectValueProvider {
    override suspend fun get(target: Id): Map<String, Any?> {
        return db.store.get(target)?.map ?: emptyMap()
    }

    override suspend fun subscribe(target: Id) = db.observe(target).map { it.map }
}