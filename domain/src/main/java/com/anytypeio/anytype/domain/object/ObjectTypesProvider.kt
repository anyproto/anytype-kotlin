package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectType

interface ObjectTypesProvider {

    fun get(): List<ObjectType>
    fun set(objectTypes: List<ObjectType>)
}