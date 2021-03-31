package com.anytypeio.anytype.core_ui.features.sets

import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.SortingExpression

class SortingRelationPickerAdapter(
    private val sorts: List<SortingExpression>,
    relations: List<SimpleRelationView>,
    relationSelectedKey: String?,
    click: (String?, String) -> Unit
) : RelationPickerAdapter(relations, relationSelectedKey, click) {

    override fun isRelationAvailable(key: String): Boolean = sorts.none { it.key == key }
}