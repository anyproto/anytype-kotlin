package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.Viewer

fun Viewer.Filter.Condition.hasValue(): Boolean = when (this) {
    is Viewer.Filter.Condition.Selected.Empty,
    is Viewer.Filter.Condition.Selected.NotEmpty,
    is Viewer.Filter.Condition.Text.Empty,
    is Viewer.Filter.Condition.Text.NotEmpty -> false
    else -> true
}

fun FilterValue?.getTextValue(): String? = when (this) {
    is FilterValue.Email -> value
    is FilterValue.Number -> value
    is FilterValue.Phone -> value
    is FilterValue.Text -> value
    is FilterValue.TextShort -> value
    is FilterValue.Url -> value
    else -> null
}

fun DVFilterCondition.isValueRequired(): Boolean = when (this) {
    DVFilterCondition.EMPTY, DVFilterCondition.NOT_EMPTY -> false
    else -> true
}