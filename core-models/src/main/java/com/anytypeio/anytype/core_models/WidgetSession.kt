package com.anytypeio.anytype.core_models

/**
 * @property [collapsed] list of collapsed widgets
 * @property [widgetsToActiveViews] maps specific widget to the selected view inside this widget
 */
// TODO scope to space
data class WidgetSession(
    val collapsed: List<Id> = emptyList(),
    val widgetsToActiveViews: Map<Id, Id> = emptyMap()
)