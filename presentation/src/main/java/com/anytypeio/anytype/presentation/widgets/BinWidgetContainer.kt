package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class BinWidgetContainer(
    widget: Widget
) : WidgetContainer {
    override val view: Flow<WidgetView.Bin> = flowOf(
        WidgetView.Bin(
            id = widget.id,
            isLoading = false,
            canCreateObjectOfType = false,
            isEmpty = false,
            sectionType = widget.sectionType,
            source = widget.source
        )
    )
}