package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id

// TODO will be used later.
sealed class WidgetViewEvent {
    abstract val widget: Id

    sealed class Tree : WidgetViewEvent() {
        data class OnExpandToggled(
            override val widget: Id
        ) : Tree()
    }
}