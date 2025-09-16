package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.domain.primitives.FieldParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LinkWidgetContainer(
    widget: Widget,
    fieldParser: FieldParser
) : WidgetContainer {
    override val view: Flow<WidgetView.Link> = flowOf(
        WidgetView.Link(
            id = widget.id,
            source = widget.source,
            icon = widget.icon,
            name = widget.source.getPrettyName(fieldParser)
        )
    )
}
