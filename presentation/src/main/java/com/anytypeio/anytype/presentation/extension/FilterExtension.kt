package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
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
    DVFilterCondition.NONE,
    DVFilterCondition.EXISTS -> false
    else -> true
}

/**
 * Determines whether this filter has a meaningful value and should be included
 * in middleware subscriptions. Filters without valid values (e.g., disabled filters
 * created on Desktop with no value set) are excluded to prevent the middleware
 * from filtering out all objects.
 */
fun DVFilter.isSupportedForSubscription(): Boolean {
    if (!condition.isValueRequired()) return true
    return when (val v = value) {
        null -> false
        is String -> v.isNotEmpty()
        is List<*> -> v.isNotEmpty()
        is Double -> !(relationFormat == RelationFormat.DATE && quickOption == DVFilterQuickOption.EXACT_DATE && v == 0.0)
        is Long -> !(relationFormat == RelationFormat.DATE && quickOption == DVFilterQuickOption.EXACT_DATE && v == 0L)
        else -> true
    }
}

/**
 * Removes filters that have no meaningful value from the list before sending
 * to middleware subscriptions. Advanced (nested) filters are cleaned recursively;
 * if all nested children are removed, the parent is removed too.
 */
fun List<DVFilter>.removeUnsupportedFilters(): List<DVFilter> = mapNotNull { filter ->
    if (filter.isAdvanced()) {
        val cleaned = filter.nestedFilters.removeUnsupportedFilters()
        if (cleaned.isEmpty()) null else filter.copy(nestedFilters = cleaned)
    } else {
        if (filter.isSupportedForSubscription()) filter else null
    }
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
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<FilterView> = mapNotNull { filter ->
    if (filter.isAdvanced()) {
        FilterView.Advanced(
            id = filter.id,
            operator = filter.operator.toView(),
            nestedFilterCount = filter.nestedFilters.size
        )
    } else {
        val relation = storeOfRelations.getByKey(filter.relation)
        if (relation != null) {
            filter.toView(
                relation = relation,
                isInEditMode = screenState == ViewerFilterViewModel.ScreenState.EDIT,
                urlBuilder = urlBuilder,
                store = storeOfObjects,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        } else {
            Timber.w("Could not found relation: ${filter.relation} for filter: $filter")
            null
        }
    }
}

fun DVFilterOperator.toView(): Viewer.FilterOperator = when (this) {
    DVFilterOperator.AND -> Viewer.FilterOperator.And
    DVFilterOperator.OR -> Viewer.FilterOperator.Or
    DVFilterOperator.NO -> Viewer.FilterOperator.No
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