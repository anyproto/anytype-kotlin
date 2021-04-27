package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.sets.model.Viewer

fun List<CreateFilterView>.checkboxFilter(
    relationKey: Id,
    condition: Viewer.Filter.Condition
): DVFilter {
    val checkboxes = filterIsInstance<CreateFilterView.Checkbox>()
    val selected = checkboxes.first { it.isSelected }
    val value = selected.isChecked
    return DVFilter(
        relationKey = relationKey,
        condition = condition.toDomain(),
        value = value
    )
}