package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow

sealed interface WidgetContainer {
    val view: Flow<WidgetView>
}