package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class DataViewObjectValueProvider(
    private val db: ObjectSetDatabase,
    private val objectState: StateFlow<ObjectState>
) : ObjectValueProvider {
    override suspend fun get(ctx: Id, target: Id): Struct {
        return if (ctx == target)
            parseIntrinsicObjectValues(
                state = objectState.value,
                target = target
            )
        else
            db.store.get(target)?.map ?: emptyMap()
    }

    override suspend fun subscribe(ctx: Id, target: Id): Flow<Struct> {
        return if (ctx == target)
            objectState.map { state ->
                parseIntrinsicObjectValues(
                    state = state,
                    target = target
                )
            }
        else
            db.observe(target).map { it.map }
    }

    /**
     * Providing values for relations of a Set of objects or a Collection of objects.
     * Set or Collection are both considered here as Objects, contrary to objects from database query.
     * Objects corresponding to data view query should be taken from [db].
     */
    private fun parseIntrinsicObjectValues(
        state: ObjectState,
        target: Id
    ) : Struct = when (state) {
        is ObjectState.DataView.Collection -> state.details[target]?.map ?: emptyMap()
        is ObjectState.DataView.Set -> state.details[target]?.map ?: emptyMap()
        is ObjectState.ErrorLayout -> emptyMap()
        is ObjectState.Init -> emptyMap()
    }
}