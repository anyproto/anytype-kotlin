package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.sets.model.Viewer

sealed class DataViewViewState {

    sealed class Collection : DataViewViewState() {

        abstract val isCreateObjectAllowed: Boolean
        abstract val isEditingViewAllowed: Boolean

        data class NoView(
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Collection()
        data class NoItems(
            val title: String,
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Collection()

        data class Default(
            val viewer: Viewer?,
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Collection()
    }

    sealed class Set : DataViewViewState() {

        abstract val isCreateObjectAllowed: Boolean
        abstract val isEditingViewAllowed: Boolean

        data class NoQuery(
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Set()

        data class NoView(
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Set()

        data class NoItems(
            val title: String,
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Set()

        data class Default(
            val viewer: Viewer?,
            override val isCreateObjectAllowed: Boolean = true,
            override val isEditingViewAllowed: Boolean = true
        ) : Set()
    }

    object Init: DataViewViewState()
    data class Error(val msg: String) : DataViewViewState()
}

sealed class SetOrCollectionHeaderState {
    object None : SetOrCollectionHeaderState()
    data class Default(
        val title: BlockView.Title.Basic,
        val description: Description,
        val isReadOnlyMode: Boolean = false
    ) : SetOrCollectionHeaderState()

    sealed class Description {
        object None: Description()
        data class Default(val description: String) : Description()
    }
}