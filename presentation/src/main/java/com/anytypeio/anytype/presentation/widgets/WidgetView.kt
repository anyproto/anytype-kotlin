package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id

sealed class WidgetView {
    data class Default(
        val id: Id,
        val elements: List<String>
    ) : WidgetView()
}