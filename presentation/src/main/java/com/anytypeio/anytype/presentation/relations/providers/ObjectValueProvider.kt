package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ObjectValueProvider {
    fun get(target: Id): Map<String, Any?>
    fun subscribe(target: Id) : Flow<Map<String, Any?>>
}