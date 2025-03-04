package com.anytypeio.anytype.feature_object_type.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ObjectTypeStore {

    private val _propertiesFlow = MutableStateFlow<List<String>>(emptyList())
    val propertiesFlow: StateFlow<List<String>> = _propertiesFlow.asStateFlow()

    fun setProperties(newProperties: List<String>) {
        _propertiesFlow.value = newProperties
    }
}