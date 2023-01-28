package com.anytypeio.anytype.presentation.sets.filter

sealed class FilterClick {

    data class Value(val index: Int) : FilterClick()
    data class Remove(val index: Int) : FilterClick()
}