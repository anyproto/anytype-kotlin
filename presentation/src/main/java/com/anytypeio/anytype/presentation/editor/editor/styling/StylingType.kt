package com.anytypeio.anytype.presentation.editor.editor.styling

import com.anytypeio.anytype.core_utils.ui.ViewType

enum class StylingType : ViewType {
    STYLE {
        override fun getViewType(): Int = ordinal
    },
    TEXT_COLOR {
        override fun getViewType(): Int = ordinal
    },
    BACKGROUND {
        override fun getViewType(): Int = ordinal
    }
}