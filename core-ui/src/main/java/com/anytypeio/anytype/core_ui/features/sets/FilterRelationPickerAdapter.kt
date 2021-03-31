package com.anytypeio.anytype.core_ui.features.sets

import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

class FilterRelationPickerAdapter(
    relations: List<SimpleRelationView>,
    relationSelectedKey: String?,
    click: (String?, String) -> Unit
) : RelationPickerAdapter(relations, relationSelectedKey, click) {

    override fun isRelationAvailable(key: String): Boolean = true
}