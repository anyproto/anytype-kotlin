package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin

import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.ViewType
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin.NumPadAdapter

sealed class NumPadView : ViewType {
    data class NumberView(val number: Int) : NumPadView() {
        override fun getViewType() = NumPadAdapter.NUMBER
    }

    object RemoveView : NumPadView() {
        override fun getViewType() = NumPadAdapter.REMOVE
    }

    object EmptyView : NumPadView() {
        override fun getViewType() = NumPadAdapter.EMPTY
    }
}