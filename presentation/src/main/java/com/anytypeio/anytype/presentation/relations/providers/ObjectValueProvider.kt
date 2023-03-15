package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import kotlinx.coroutines.flow.Flow

interface ObjectValueProvider {
    suspend fun get(ctx: Id, target: Id): Struct
    suspend fun subscribe(ctx: Id, target: Id) : Flow<Struct>
}