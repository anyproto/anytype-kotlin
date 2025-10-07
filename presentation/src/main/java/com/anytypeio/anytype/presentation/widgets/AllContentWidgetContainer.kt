package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AllContentWidgetContainer(
    widget: Widget.AllObjects
) : WidgetContainer {
    override val view: Flow<WidgetView> = flowOf(
        WidgetView.AllContent(
            id = widget.id,
            sectionType = widget.sectionType
        )
    )
}