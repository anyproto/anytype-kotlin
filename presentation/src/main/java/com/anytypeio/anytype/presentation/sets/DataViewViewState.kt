package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.sets.model.Viewer

sealed class DataViewViewState {

    sealed class Collection : DataViewViewState() {
        object NoView : Collection()
        data class NoItems(val title: String, val isCreateObjectAllowed: Boolean) : Collection()
        data class Default(val viewer: Viewer?, val isCreateObjectAllowed: Boolean) : Collection()
    }

    sealed class Set : DataViewViewState() {
        object NoQuery : Set()
        object NoView : Set()
        data class NoItems(val title: String, val isCreateObjectAllowed: Boolean) : Set()
        data class Default(val viewer: Viewer?, val isCreateObjectAllowed: Boolean) : Set()
    }

    object Init: DataViewViewState()
    data class Error(val msg: String) : DataViewViewState()
}

sealed class SetOrCollectionHeaderState {
    object None : SetOrCollectionHeaderState()
    data class Default(
        val title: BlockView.Title.Basic,
        val description: Description
    ) : SetOrCollectionHeaderState()

    sealed class Description {
        object None: Description()
        data class Default(val description: String) : Description()
    }
}