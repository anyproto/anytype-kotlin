package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.relations.toView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.Viewer
import timber.log.Timber

fun Viewer.Filter.Condition.hasValue(): Boolean = when (this) {
    is Viewer.Filter.Condition.Selected.Empty,
    is Viewer.Filter.Condition.Selected.NotEmpty,
    is Viewer.Filter.Condition.Text.Empty,
    is Viewer.Filter.Condition.Text.NotEmpty,
    is Viewer.Filter.Condition.Checkbox.None,
    is Viewer.Filter.Condition.Number.None,
    is Viewer.Filter.Condition.Number.Empty,
    is Viewer.Filter.Condition.Number.NotEmpty,
    is Viewer.Filter.Condition.Date.None,
    is Viewer.Filter.Condition.Date.Empty,
    is Viewer.Filter.Condition.Date.NotEmpty,
    is Viewer.Filter.Condition.Selected.None,
    is Viewer.Filter.Condition.Text.None -> false
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
    DVFilterCondition.EMPTY,
    DVFilterCondition.NOT_EMPTY,
    DVFilterCondition.NONE -> false
    else -> true
}

fun List<CreateFilterView>.checkboxFilter(
    relationKey: Id,
    condition: Viewer.Filter.Condition
): DVFilter {
    val checkboxes = filterIsInstance<CreateFilterView.Checkbox>()
    val selected = checkboxes.firstOrNull { it.isSelected }
    val value = selected?.isChecked
    return DVFilter(
        relationKey = relationKey,
        condition = condition.toDomain(),
        value = value
    )
}

suspend fun List<DVFilter>.toView(
    storeOfRelations: StoreOfRelations,
    storeOfObjects: ObjectStore,
    details: Map<Id, Block.Fields>,
    screenState: ViewerFilterViewModel.ScreenState,
    urlBuilder: UrlBuilder
) = mapNotNull { filter ->
    val relation = storeOfRelations.getByKey(filter.relationKey)
    if (relation != null) {
        filter.toView(
            relation = relation,
            details = details,
            isInEditMode = screenState == ViewerFilterViewModel.ScreenState.EDIT,
            urlBuilder = urlBuilder,
            store = storeOfObjects
        )
    } else {
        Timber.w("Could not found relation: ${filter.relationKey} for filter: $filter")
        null
    }
}