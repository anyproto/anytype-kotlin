package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeObjectValueProvider(
    var values: Map<Id, Map<String, Any?>> = emptyMap()
) : ObjectValueProvider {

    override fun get(target: Id): Map<String, Any?> = values[target] ?: emptyMap()

    override fun subscribe(target: Id): Flow<Map<String, Any?>> =
        flow { emit(get(target)) }
}