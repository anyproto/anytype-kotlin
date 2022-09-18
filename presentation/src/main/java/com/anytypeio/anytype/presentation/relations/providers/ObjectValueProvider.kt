package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ObjectValueProvider {
    suspend fun get(target: Id): Map<String, Any?>
    suspend fun subscribe(target: Id) : Flow<Map<String, Any?>>
}