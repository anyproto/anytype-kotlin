package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.ObjectType

interface ObjectTypeProvider {
    fun provide() : List<ObjectType>
}