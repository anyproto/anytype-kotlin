package com.anytypeio.anytype.data.auth.types

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider

class DefaultObjectTypesProvider : ObjectTypesProvider {

    private val objectTypes: MutableList<ObjectType> = mutableListOf()

    override fun get(): List<ObjectType> = objectTypes

    override fun set(objectTypes: List<ObjectType>) {
        this.objectTypes.apply {
            clear()
            addAll(objectTypes)
        }
    }
}