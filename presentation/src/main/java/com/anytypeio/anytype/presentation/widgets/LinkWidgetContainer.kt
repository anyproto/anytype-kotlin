package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LinkWidgetContainer(
    widget: Widget.Link
) : WidgetContainer {
    override val view: Flow<WidgetView.Link> = flowOf(
        WidgetView.Link(
            id = widget.id,
            source = widget.source
        )
    )
}
