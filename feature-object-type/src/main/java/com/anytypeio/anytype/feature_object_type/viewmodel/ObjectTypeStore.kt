package com.anytypeio.anytype.feature_object_type.viewmodel

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ObjectTypeStore {

    private val _propertiesFlow = MutableStateFlow<List<String>>(emptyList())
    val propertiesFlow: StateFlow<List<String>> = _propertiesFlow.asStateFlow()

    private val _recommendedPropertiesFlow = MutableStateFlow<List<Id>>(emptyList())
    val recommendedPropertiesFlow: StateFlow<List<Id>> = _recommendedPropertiesFlow.asStateFlow()

    fun setProperties(newProperties: List<String>) {
        _propertiesFlow.value = newProperties
    }

    fun setRecommendedProperties(recommendedProperties: List<Id>) {
        _recommendedPropertiesFlow.value = recommendedProperties
    }
}