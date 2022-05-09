package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider

internal object FakeObjectDetailsProvider : ObjectDetailProvider {
    override fun provide(): Map<Id, Block.Fields> {
        return emptyMap()
    }
}