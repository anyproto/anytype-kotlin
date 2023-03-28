package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import kotlinx.coroutines.flow.Flow

interface ObjectRelationProvider {
    suspend fun get(relation: Key): ObjectWrapper.Relation
    suspend fun getById(relation: Id) : ObjectWrapper.Relation
    fun observe(relation: Key): Flow<ObjectWrapper.Relation>
    fun observeAll(): Flow<List<ObjectWrapper.Relation>>

    companion object {
        /**
         * Provider which should provide intrinsic relations of given object, be it a page, a collection or a set
         */
        const val INTRINSIC_PROVIDER_TYPE = "object-intrinsic-relations-provider"
        /**
         * Provider which should provide relations for a data view from an object, a collection or a set.
         */
        const val DATA_VIEW_PROVIDER_TYPE = "data-view-relations-provider"
    }
}