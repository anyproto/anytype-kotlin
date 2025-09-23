package com.anytypeio.anytype.presentation.widgets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed class SectionWidgetContainer : WidgetContainer {

    data object Pinned : SectionWidgetContainer() {
        override val view: Flow<WidgetView.Section> = flowOf(WidgetView.Section.Pinned)
    }
    data object ObjectTypes : SectionWidgetContainer() {
        override val view: Flow<WidgetView.Section> = flowOf(WidgetView.Section.ObjectTypes)
    }
}