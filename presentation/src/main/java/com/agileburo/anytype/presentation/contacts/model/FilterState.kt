package com.agileburo.anytype.presentation.contacts.model

import com.agileburo.anytype.presentation.filters.model.FilterView

data class FilterState(
    var filters: MutableSet<FilterView> = mutableSetOf()
)