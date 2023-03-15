package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.presentation.editor.editor.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultObjectValueProvider(
    private val details: Store.Details
) : ObjectValueProvider {
    override suspend fun get(ctx: Id, target: Id): Struct {
        return details.current().details.getOrDefault(target, Block.Fields.empty()).map
    }

    override suspend fun subscribe(ctx: Id, target: Id): Flow<Struct> {
        return details.stream().map { details ->
            details.details.getOrDefault(target, Block.Fields.empty()).map
        }
    }
}