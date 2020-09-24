package com.anytypeio.anytype.presentation.databaseview.models

data class FilterState(
    var filters: MutableSet<String> = mutableSetOf()
)