package com.anytypeio.anytype.core_ui.model

import com.anytypeio.anytype.core_ui.common.Alignment
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.features.page.styling.StylingType

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