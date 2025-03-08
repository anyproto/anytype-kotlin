package com.anytypeio.anytype.feature_object_type.properties.add

import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeStore
import kotlinx.coroutines.flow.Flow

interface TypePropertiesProvider {
    fun observeKeys(): Flow<List<Key>>
}

class TypePropertiesProviderImpl(
    private val objectTypeStore: ObjectTypeStore
) : TypePropertiesProvider {
    override fun observeKeys(): Flow<List<Key>> = objectTypeStore.propertiesFlow
}