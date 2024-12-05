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
            name = when(val source = widget.source) {
                is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                is Widget.Source.Default -> WidgetView.Name.Default(
                    prettyPrintName = fieldParser.getObjectName(source.obj)
                )
            }
        )
    )
}
