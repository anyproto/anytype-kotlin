package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.relations.toView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel
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

fun List<CreateFilterView>.checkboxFilterValue(): Boolean? {
    val checkboxes = filterIsInstance<CreateFilterView.Checkbox>()
    val selected = checkboxes.firstOrNull { it.isSelected }
    return selected?.isChecked
}

suspend fun List<DVFilter>.toView(
    storeOfRelations: StoreOfRelations,
    storeOfObjects: ObjectStore,
    screenState: ViewerFilterViewModel.ScreenState,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser
): List<FilterView.Expression> = mapNotNull { filter ->
    val relation = storeOfRelations.getByKey(filter.relation)
    if (relation != null) {
        filter.toView(
            relation = relation,
            isInEditMode = screenState == ViewerFilterViewModel.ScreenState.EDIT,
            urlBuilder = urlBuilder,
            store = storeOfObjects,
            fieldParser = fieldParser
        )
    } else {
        Timber.w("Could not found relation: ${filter.relation} for filter: $filter")
        null
    }
}

suspend fun List<DVSort>.toView(
    storeOfRelations: StoreOfRelations,
    screenState: ViewerSortViewModel.ScreenState
): List<ViewerSortViewModel.ViewerSortView> = mapNotNull { sort ->
    val relation = storeOfRelations.getByKey(sort.relationKey)
    if (relation != null) {
        ViewerSortViewModel.ViewerSortView(
            sortId = sort.id,
            relation = sort.relationKey,
            name = relation.name.orEmpty(),
            type = sort.type,
            format = relation.format,
            mode = screenState
        )
    } else {
        Timber.w("Could not found relation: ${sort.relationKey} in StoreOfRelations for sort: $sort")
        null
    }
}