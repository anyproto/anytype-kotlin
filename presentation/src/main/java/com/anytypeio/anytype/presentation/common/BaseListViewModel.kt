package com.anytypeio.anytype.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseListViewModel<VIEW> : BaseViewModel() {
    internal val _views = MutableStateFlow<List<VIEW>>(emptyList())
    val views: StateFlow<List<VIEW>> = _views
}