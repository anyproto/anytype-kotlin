package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider

internal object FakeObjectTypesProvider : ObjectTypesProvider {
    internal var list = emptyList<ObjectType>()

    override fun get(): List<ObjectType> = list

    override fun set(objectTypes: List<ObjectType>) {
        this.list = objectTypes
    }
}