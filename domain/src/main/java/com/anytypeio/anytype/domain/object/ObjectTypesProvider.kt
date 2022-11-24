package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectType

@Deprecated("To be deleted")
interface ObjectTypesProvider {
    fun get(): List<ObjectType>
    fun set(objectTypes: List<ObjectType>)
}