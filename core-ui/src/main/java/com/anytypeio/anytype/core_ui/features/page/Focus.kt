package com.anytypeio.anytype.core_ui.features.page

/**
 * Focus/carriage model.
 * @property id of the focused block
 * @property selection current selection
 */
data class Focus(
    val id: String,
    val selection: IntRange
)