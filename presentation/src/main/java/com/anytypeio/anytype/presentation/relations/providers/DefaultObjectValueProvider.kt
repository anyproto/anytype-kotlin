package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.page.editor.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultObjectValueProvider(
    private val details: Store.Details
) : ObjectValueProvider {
    override fun get(target: Id): Map<String, Any?> {
        return details.current().details.getOrDefault(target, Block.Fields.empty()).map
    }

    override fun subscribe(target: Id): Flow<Map<String, Any?>> {
        return details.stream().map { details ->
            details.details.getOrDefault(target, Block.Fields.empty()).map
        }
    }
}