package com.agileburo.anytype.core_ui.features.page.styling

import com.agileburo.anytype.core_ui.common.ViewType

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