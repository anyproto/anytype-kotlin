package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.sets.model.Viewer

sealed class DataViewViewState {

    sealed class Collection : DataViewViewState() {
        object NoView : Collection()
        data class NoItems(val title: String) : Collection()
        data class Default(val viewer: Viewer?) : Collection()
    }

    sealed class Set : DataViewViewState() {
        object NoQuery : Set()
        object NoView : Set()
        data class NoItems(val title: String) : Set()
        data class Default(val viewer: Viewer?) : Set()
    }

    object Init: DataViewViewState()
    data class Error(val msg: String) : DataViewViewState()
}