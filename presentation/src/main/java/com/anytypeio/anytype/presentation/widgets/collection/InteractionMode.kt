package com.anytypeio.anytype.presentation.widgets.collection

sealed class InteractionMode {
    object Edit : InteractionMode()
    object View : InteractionMode()
}