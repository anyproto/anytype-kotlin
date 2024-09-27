package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AllContentWidgetContainer : WidgetContainer {
    override val view: Flow<WidgetView> = flowOf(
        WidgetView.AllContent
    )
}