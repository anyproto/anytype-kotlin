package com.anytypeio.anytype.core_ui.features.sets

import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterExpression

class PickFilterColumnAdapter(
    private val filters: List<FilterExpression>,
    columns: List<ColumnView>,
    columnSelectedKey: String?,
    click: (String?, String) -> Unit
) : PickColumnAdapter(columns, columnSelectedKey, click) {

    override fun isColumnAvailable(key: String): Boolean = filters.any { it.key == key }
}