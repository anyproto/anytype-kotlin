package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id

interface ObjectDetailProvider {
    fun provide() : Map<Id, Block.Fields>
}