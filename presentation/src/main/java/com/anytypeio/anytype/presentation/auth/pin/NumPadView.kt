package com.anytypeio.anytype.presentation.auth.pin

import com.anytypeio.anytype.core_utils.ui.ViewType

sealed class NumPadView : ViewType {
    data class NumberView(val number: Int) : NumPadView() {
        override fun getViewType() = NUMBER
    }

    object RemoveView : NumPadView() {
        override fun getViewType() = REMOVE
    }

    object EmptyView : NumPadView() {
        override fun getViewType() = EMPTY
    }

    companion object {
        const val NUMBER = 0
        const val REMOVE = 1
        const val EMPTY = 2
    }
}