package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal object FakeObjectValueProvider : ObjectValueProvider {

    override fun get(target: Id): Map<String, Any?> = emptyMap()

    override fun subscribe(target: Id): Flow<Map<String, Any?>> =
        flow { emit(emptyMap()) }
}