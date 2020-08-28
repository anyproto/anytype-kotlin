package com.agileburo.anytype.core_ui.model

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.styling.StylingType

data class StyleConfig(
    val visibleTypes: List<StylingType>,
    val enabledMarkup: List<Markup.Type>,
    val enabledAlignment: List<Alignment>
) {

    companion object {
        fun emptyState() = StyleConfig(
            visibleTypes = emptyList(),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
}