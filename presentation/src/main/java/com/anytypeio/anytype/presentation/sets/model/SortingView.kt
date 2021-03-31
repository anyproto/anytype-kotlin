package com.anytypeio.anytype.presentation.sets.model

import com.anytypeio.anytype.core_utils.ui.ViewType

sealed class SortingView : ViewType {

    data class Set(
        val key: String,
        val type: Viewer.SortType,
        val title: String,
        val isWithPrefix: Boolean = false,
        val format: ColumnView.Format
    ) : SortingView(), ViewType {
        override fun getViewType(): Int = HOLDER_SET
    }

    object Add : SortingView(), ViewType {
        override fun getViewType(): Int = HOLDER_ADD
    }

    object Apply : SortingView(), ViewType {
        override fun getViewType(): Int = HOLDER_APPLY
    }

    companion object {
        const val HOLDER_SET = 1
        const val HOLDER_ADD = 2
        const val HOLDER_APPLY = 3
    }
}