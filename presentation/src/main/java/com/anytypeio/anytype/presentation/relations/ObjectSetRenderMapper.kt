package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_utils.ext.CURRENT_MONTH
import com.anytypeio.anytype.core_utils.ext.CURRENT_WEEK
import com.anytypeio.anytype.core_utils.ext.EXACT_DAY
import com.anytypeio.anytype.core_utils.ext.LAST_WEEK
import com.anytypeio.anytype.core_utils.ext.MONTH_AGO
import com.anytypeio.anytype.core_utils.ext.MONTH_AHEAD
import com.anytypeio.anytype.core_utils.ext.NEXT_WEEK
import com.anytypeio.anytype.core_utils.ext.NUMBER_OF_DAYS_AGO
import com.anytypeio.anytype.core_utils.ext.NUMBER_OF_DAYS_FROM_NOW
import com.anytypeio.anytype.core_utils.ext.TODAY
import com.anytypeio.anytype.core_utils.ext.TOMORROW
import com.anytypeio.anytype.core_utils.ext.YESTERDAY
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.extension.isValueRequired
import com.anytypeio.anytype.presentation.mapper.toCheckboxView
import com.anytypeio.anytype.presentation.mapper.toDateView
import com.anytypeio.anytype.presentation.mapper.toGridRecordRows
import com.anytypeio.anytype.presentation.mapper.toNumberView
import com.anytypeio.anytype.presentation.mapper.toSelectedView
import com.anytypeio.anytype.presentation.mapper.toSimpleRelationView
import com.anytypeio.anytype.presentation.mapper.toTextView
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.mapper.toViewerColumns
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewState
import com.anytypeio.anytype.presentation.sets.buildGalleryViews
import com.anytypeio.anytype.presentation.sets.buildListViews
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import com.anytypeio.anytype.presentation.sets.toObjectView
import timber.log.Timber

fun ObjectSet.tabs(activeViewerId: String? = null): List<ViewerTabView> {
    val block = blocks.first { it.content is DV }
    val dv = block.content as DV
    return dv.tabs(activeViewerId)
}

fun DV.tabs(activeViewerId: String? = null): List<ViewerTabView> {
    return viewers.mapIndexed { index, viewer ->
        ViewerTabView(
            id = viewer.id,
            name = viewer.name,
            isActive = if (activeViewerId != null)
                viewer.id == activeViewerId
            else
                index == 0
        )
    }
}

suspend fun DVViewer.render(
    builder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
    useFallbackView: Boolean = false,
    objects: List<Id>,
    details: Map<Id, Block.Fields>,
    dataViewRelations: List<ObjectWrapper.Relation>,
    store: ObjectStore
): ObjectSetViewState {
    return when (type) {
        DVViewerType.GRID -> {
            buildGridView(
                dataViewRelations = dataViewRelations,
                objects = objects,
                details = details,
                builder = builder,
                store = store
            )
        }
        DVViewerType.GALLERY -> {
            ObjectSetViewState(
                viewer = Viewer.GalleryView(
                    id = id,
                    items = buildGalleryViews(
                        objects = objects,
                        details = details,
                        relations = dataViewRelations,
                        coverImageHashProvider = coverImageHashProvider,
                        urlBuilder = builder,
                        store = store
                    ),
                    title = name,
                    largeCards = cardSize != DVViewerCardSize.SMALL
                )
            )
        }
        DVViewerType.LIST -> {
            val vmap = viewerRelations.associateBy { it.key }
            val visibleRelations = dataViewRelations.filter { relation ->
                val vr = vmap[relation.key]
                vr?.isVisible ?: false
            }
            ObjectSetViewState(
                viewer = Viewer.ListView(
                    id = id,
                    items = buildListViews(
                        objects = objects,
                        details = details,
                        relations = visibleRelations,
                        urlBuilder = builder,
                        store = store
                    ),
                    title = name
                )
            )
        }
        else -> {
            if (useFallbackView) {
                buildGridView(
                    dataViewRelations = dataViewRelations,
                    objects = objects,
                    details = details,
                    builder = builder,
                    store = store
                )
            } else {
                ObjectSetViewState(
                    viewer = Viewer.Unsupported(
                        id = id,
                        title = name,
                        error = "This view type (${type.name.lowercase()}) is not supported on Android yet. See it as grid view?"
                    )
                )
            }
        }
    }
}

private suspend fun DVViewer.buildGridView(
    dataViewRelations: List<ObjectWrapper.Relation>,
    objects: List<Id>,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder,
    store: ObjectStore
): ObjectSetViewState {
    val vmap = viewerRelations.associateBy { it.key }
    val visibleRelations = dataViewRelations.filter { relation ->
        val vr = vmap[relation.key]
        vr?.isVisible ?: false
    }
    val columns = viewerRelations.toViewerColumns(
        relations = visibleRelations,
        filterBy = listOf(ObjectSetConfig.NAME_KEY)
    )
    val rows = buildList {
        addAll(
            objects.toGridRecordRows(
                showIcon = !hideIcon,
                columns = columns,
                relations = visibleRelations,
                details = details,
                builder = builder,
                store = store
            )
        )
    }
    return ObjectSetViewState(
        viewer = Viewer.GridView(
            id = id,
            name = name,
            columns = columns,
            rows = rows
        )
    )
}

fun title(
    title: Block,
    ctx: Id,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    details: Map<Id, Block.Fields>
): BlockView.Title.Basic {

    val objectDetails = details[ctx]
    val coverContainer = BlockFieldsCoverWrapper(objectDetails)
        .getCover(urlBuilder, coverImageHashProvider)

    return BlockView.Title.Basic(
        id = title.id,
        text = title.content<Block.Content.Text>().text,
        emoji = objectDetails?.iconEmoji?.ifEmpty { null },
        image = objectDetails?.iconImage?.let { hash ->
            if (hash.isNotEmpty())
                urlBuilder.thumbnail(hash = hash)
            else
                null
        },
        coverImage = coverContainer.coverImage,
        coverColor = coverContainer.coverColor,
        coverGradient = coverContainer.coverGradient
    )
}

suspend fun ObjectSet.simpleRelations(
    viewerId: Id?,
    storeOfRelations: StoreOfRelations
): ArrayList<SimpleRelationView> {
    return if (isInitialized) {
        val block = blocks.first { it.content is DV }
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
        viewer.viewerRelations.toSimpleRelationView(
            dv.relationsIndex.mapNotNull {
                storeOfRelations.getByKey(it.key)
            }
        )
    } else {
        arrayListOf()
    }
}

fun DVViewer.toViewRelation(relation: ObjectWrapper.Relation): SimpleRelationView {
    val viewerRelation = viewerRelations.firstOrNull { it.key == relation.key }
    if (viewerRelation == null) {
        Timber.e("ViewerRelations is not containing relation:$relation")
    }
    return SimpleRelationView(
        key = relation.key,
        isHidden = relation.isHidden ?: false,
        isVisible = viewerRelation?.isVisible ?: false,
        title = relation.name.orEmpty(),
        format = relation.format.toView()
    )
}

@Deprecated("To be deleted")
fun Relation.toCreateFilterCheckboxView(isSelected: Boolean? = null): List<CreateFilterView.Checkbox> {
    return listOf(
        CreateFilterView.Checkbox(
            isChecked = true,
            isSelected = isSelected == true
        ),
        CreateFilterView.Checkbox(
            isChecked = false,
            isSelected = isSelected != true
        )
    )
}

fun ObjectWrapper.Relation.toCreateFilterCheckboxView(isSelected: Boolean? = null): List<CreateFilterView.Checkbox> {
    return listOf(
        CreateFilterView.Checkbox(
            isChecked = true,
            isSelected = isSelected == true
        ),
        CreateFilterView.Checkbox(
            isChecked = false,
            isSelected = isSelected != true
        )
    )
}

@Deprecated("To be deleted")
fun Relation.toCreateFilterTagView(ids: List<*>? = null): List<CreateFilterView.Tag> =
    selections
        .map { option ->
            CreateFilterView.Tag(
                id = option.id,
                name = option.text,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

suspend fun ObjectWrapper.Relation.toCreateFilterTagView(
    ids: List<*>? = null,
    store: ObjectStore
): List<CreateFilterView.Tag> =
    relationOptionsDict
        .mapNotNull { id ->
            store.get(id)?.let {
                ObjectWrapper.Option(it.map)
            }
        }
        .map { option ->
            CreateFilterView.Tag(
                id = option.id,
                name = option.title,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

@Deprecated("To be deleted")
fun Relation.toCreateFilterStatusView(ids: List<*>? = null): List<CreateFilterView.Status> =
    selections
        .map { option ->
            CreateFilterView.Status(
                id = option.id,
                name = option.text,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

suspend fun ObjectWrapper.Relation.toCreateFilterStatusView(
    ids: List<*>? = null,
    store: ObjectStore
): List<CreateFilterView.Status> =
    relationOptionsDict
        .mapNotNull { id ->
            store.get(id)?.let {
                ObjectWrapper.Option(it.map)
            }
        }
        .map { option ->
            CreateFilterView.Status(
                id = option.id,
                name = option.title,
                color = option.color,
                isSelected = ids?.contains(option.id) ?: false
            )
        }

@Deprecated("To be deleted")
fun Relation.toCreateFilterDateView(
    quickOption: DVFilterQuickOption?,
    condition: DVFilterCondition,
    value: Long
) = quickOptionOrderMap.getOrDefault(condition, quickOptionDefaultOrder)
    .map {
        val isSelected = quickOption.isSelected(it, value)
        CreateFilterView.Date(
            id = key,
            description = it.toName(),
            type = it,
            condition = condition,
            value = if (isSelected) value else CreateFilterView.Date.NO_VALUE,
            isSelected = isSelected
        )
    }

fun ObjectWrapper.Relation.toCreateFilterDateView(
    quickOption: DVFilterQuickOption?,
    condition: DVFilterCondition,
    value: Long
) = quickOptionOrderMap.getOrDefault(condition, quickOptionDefaultOrder)
    .map {
        val isSelected = quickOption.isSelected(it, value)
        CreateFilterView.Date(
            id = key,
            description = it.toName(),
            type = it,
            condition = condition,
            value = if (isSelected) value else CreateFilterView.Date.NO_VALUE,
            isSelected = isSelected
        )
    }

private fun DVFilterQuickOption?.isSelected(
    quickOption: DVFilterQuickOption,
    value: Long
) = if (this == quickOption) {
    if (quickOption == DVFilterQuickOption.EXACT_DATE) {
        value > 0
    } else {
        true
    }
} else {
    false
}


private val quickOptionDefaultOrder by lazy {
    listOf(
        DVFilterQuickOption.TODAY,
        DVFilterQuickOption.TOMORROW,
        DVFilterQuickOption.YESTERDAY,
        DVFilterQuickOption.CURRENT_WEEK,
        DVFilterQuickOption.LAST_WEEK,
        DVFilterQuickOption.NEXT_WEEK,
        DVFilterQuickOption.CURRENT_MONTH,
        DVFilterQuickOption.LAST_MONTH,
        DVFilterQuickOption.NEXT_MONTH,
        DVFilterQuickOption.DAYS_AGO,
        DVFilterQuickOption.DAYS_AHEAD,
        DVFilterQuickOption.EXACT_DATE,
    )
}

private val quickOptionOrderMap: Map<DVFilterCondition, List<DVFilterQuickOption>> by lazy {
    buildMap {
        put(
            DVFilterCondition.EQUAL, listOf(
                DVFilterQuickOption.TODAY,
                DVFilterQuickOption.TOMORROW,
                DVFilterQuickOption.YESTERDAY,
                DVFilterQuickOption.DAYS_AGO,
                DVFilterQuickOption.DAYS_AHEAD,
                DVFilterQuickOption.EXACT_DATE,
            )
        )

        put(
            DVFilterCondition.IN, listOf(
                DVFilterQuickOption.TODAY,
                DVFilterQuickOption.TOMORROW,
                DVFilterQuickOption.YESTERDAY,
                DVFilterQuickOption.LAST_WEEK,
                DVFilterQuickOption.CURRENT_WEEK,
                DVFilterQuickOption.NEXT_WEEK,
                DVFilterQuickOption.LAST_MONTH,
                DVFilterQuickOption.CURRENT_MONTH,
                DVFilterQuickOption.NEXT_MONTH,
            )
        )

        put(DVFilterCondition.EMPTY, emptyList())
        put(DVFilterCondition.NOT_EMPTY, emptyList())
    }
}

private val quickOptionToNameMapping: Map<DVFilterQuickOption, String> by lazy {
    buildMap {
        put(DVFilterQuickOption.EXACT_DATE, EXACT_DAY)
        put(DVFilterQuickOption.YESTERDAY, YESTERDAY)
        put(DVFilterQuickOption.TODAY, TODAY)
        put(DVFilterQuickOption.TOMORROW, TOMORROW)
        put(DVFilterQuickOption.LAST_WEEK, LAST_WEEK)
        put(DVFilterQuickOption.CURRENT_WEEK, CURRENT_WEEK)
        put(DVFilterQuickOption.NEXT_WEEK, NEXT_WEEK)
        put(DVFilterQuickOption.LAST_MONTH, MONTH_AGO)
        put(DVFilterQuickOption.CURRENT_MONTH, CURRENT_MONTH)
        put(DVFilterQuickOption.NEXT_MONTH, MONTH_AHEAD)
        put(DVFilterQuickOption.DAYS_AGO, NUMBER_OF_DAYS_AGO)
        put(DVFilterQuickOption.DAYS_AHEAD, NUMBER_OF_DAYS_FROM_NOW)
    }
}

fun DVFilterQuickOption.toName() = quickOptionToNameMapping.getOrDefault(this, "Error")

fun ObjectSet.columns(
    viewerId: Id
): ArrayList<ColumnView> {
    // TODO use relation stores here
    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.first { it.id == viewerId }

    val columns = viewer.viewerRelations.toViewerColumns(
//        relations = dv.relations,
        relations = emptyList(),
        filterBy = listOf()
    )
    return ArrayList(columns)
}

fun ObjectSet.sortingExpression(viewerId: Id): ArrayList<SortingExpression> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.first { it.id == viewerId }

    val list = arrayListOf<SortingExpression>()
    viewer.sorts.forEach { sort ->
        list.add(
            SortingExpression(key = sort.relationKey, type = sort.type.toView())
        )
    }
    return list
}

fun ObjectSet.filterExpression(viewerId: Id?): List<DVFilter> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
    return viewer.filters
}

@Deprecated("To be deleted")
fun Relation.toText(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun ObjectWrapper.Relation.toText(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toUrl(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun ObjectWrapper.Relation.toUrl(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toPhone(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun ObjectWrapper.Relation.toPhone(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toEmail(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun ObjectWrapper.Relation.toEmail(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toCheckbox(value: Any?): Boolean? =
    if (value is Boolean?) {
        value
    } else {
        throw IllegalArgumentException("Relation format $format value should be Boolean, actual:$value")
    }

fun ObjectWrapper.Relation.toCheckbox(value: Any?): Boolean? =
    if (value is Boolean?) {
        value
    } else {
        throw IllegalArgumentException("Relation format $format value should be Boolean, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toTags(value: Any?): List<TagView> = if (value is List<*>?) {
    val views = arrayListOf<TagView>()
    value?.filterIsInstance<Id>()?.forEach { id ->
        val option = selections.find { it.id == id }
        if (option != null) {
            views.add(
                TagView(
                    id = option.id,
                    tag = option.text,
                    color = option.color
                )
            )
        } else {
            Timber.e("Failed to find corresponding tag option")
        }
    }
    views.toList()
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

suspend fun ObjectWrapper.Relation.toTags(
    value: Any?,
    store: ObjectStore
): List<TagView> = if (value is List<*>?) {
    val views = arrayListOf<TagView>()
    value?.filterIsInstance<Id>()?.forEach { id ->
        val option = store.get(id)?.let { ObjectWrapper.Option(it.map) }
        if (option != null) {
            views.add(
                TagView(
                    id = option.id,
                    tag = option.title,
                    color = option.color
                )
            )
        } else {
            Timber.e("Failed to find corresponding tag option")
        }
    }
    views.toList()
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

@Deprecated("To be deleted")
fun Relation.toStatus(value: Any?): StatusView? =
    if (value is List<*>?) {
        var status: StatusView? = null
        val filter = value?.filterIsInstance<String>()?.firstOrNull()
        val option = selections.firstOrNull { it.id == filter }
        if (option != null) {
            status = StatusView(
                id = option.id,
                status = option.text,
                color = option.color
            )
        }
        status
    } else {
        throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
    }

suspend fun ObjectWrapper.Relation.toStatus(
    value: Any?,
    store: ObjectStore
): StatusView? =
    if (value is List<*>?) {
        var status: StatusView? = null
        val filter = value?.filterIsInstance<String>()?.firstOrNull()
        if (filter != null) {
            val option = store.get(filter)?.let { ObjectWrapper.Option(it.map) }
            if (option != null) {
                status = StatusView(
                    id = option.id,
                    status = option.title,
                    color = option.color
                )
            }
        }
        status
    } else {
        throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
    }

@Deprecated("To be deleted")
fun Relation.toObjects(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) = if (value is List<*>?) {
    val ids = value?.filterIsInstance<String>() ?: emptyList()
    val list = arrayListOf<ObjectView>()
    ids.forEach { id ->
        val wrapper = ObjectWrapper.Basic(details[id]?.map ?: emptyMap())
        if (!wrapper.isEmpty()) {
            list.add(wrapper.toObjectView(urlBuilder))
        }
    }
    list
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

fun ObjectWrapper.Relation.toObjects(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) = if (value is List<*>?) {
    val ids = value?.filterIsInstance<String>() ?: emptyList()
    val list = arrayListOf<ObjectView>()
    ids.forEach { id ->
        val wrapper = ObjectWrapper.Basic(details[id]?.map ?: emptyMap())
        if (!wrapper.isEmpty()) {
            list.add(wrapper.toObjectView(urlBuilder))
        }
    }
    list
} else {
    throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
}

suspend fun DVFilter.toView(
    store: ObjectStore,
    relation: ObjectWrapper.Relation,
    details: Map<Id, Block.Fields>,
    isInEditMode: Boolean,
    urlBuilder: UrlBuilder
): FilterView.Expression = when (relation.format) {
    Relation.Format.SHORT_TEXT -> {
        FilterView.Expression.TextShort(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.TextShort(relation.toText(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.LONG_TEXT -> {
        FilterView.Expression.Text(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Text(relation.toText(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.URL -> {
        FilterView.Expression.Url(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Url(relation.toUrl(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.EMAIL -> {
        FilterView.Expression.Email(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Email(relation.toEmail(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.PHONE -> {
        FilterView.Expression.Phone(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toTextView(),
            filterValue = FilterValue.Phone(relation.toPhone(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.NUMBER -> {
        FilterView.Expression.Number(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toNumberView(),
            filterValue = FilterValue.Number(NumberParser.parse(value)),
            format = relation.format.toView(),
            isValueRequired = true,
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.DATE -> {
        FilterView.Expression.Date(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toDateView(),
            quickOption = quickOption,
            filterValue = FilterValue.Date(DateParser.parse(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.STATUS -> {
        FilterView.Expression.Status(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Status(
                relation.toStatus(
                    value = value,
                    store = store
                )
            ),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.TAG -> {
        FilterView.Expression.Tag(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Tag(
                relation.toTags(
                    value = value,
                    store = store
                )
            ),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.OBJECT -> {
        FilterView.Expression.Object(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Object(relation.toObjects(value, details, urlBuilder)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.CHECKBOX -> {
        FilterView.Expression.Checkbox(
            key = relationKey,
            title = relation.name.orEmpty(),
            operator = operator.toView(),
            condition = condition.toCheckboxView(),
            filterValue = FilterValue.Check(relation.toCheckbox(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    else -> throw UnsupportedOperationException("Unsupported relation format:${relation.format}")
}

@Deprecated("To be deleted")
fun Relation.toFilterValue(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): FilterValue = when (this.format) {
    Relation.Format.SHORT_TEXT -> FilterValue.TextShort(toText(value))
    Relation.Format.LONG_TEXT -> FilterValue.Text(toText(value))
    Relation.Format.NUMBER -> FilterValue.Number(NumberParser.parse(value))
    Relation.Format.STATUS -> FilterValue.Status(toStatus(value))
    Relation.Format.TAG -> FilterValue.Tag(toTags(value))
    Relation.Format.DATE -> FilterValue.Date(DateParser.parse(value))
    Relation.Format.URL -> FilterValue.Url(toText(value))
    Relation.Format.EMAIL -> FilterValue.Email(toText(value))
    Relation.Format.PHONE -> FilterValue.Phone(toText(value))
    Relation.Format.OBJECT -> FilterValue.Object(toObjects(value, details, urlBuilder))
    Relation.Format.CHECKBOX -> FilterValue.Check(toCheckbox(value))
    else -> throw UnsupportedOperationException("Unsupported relation format:${format}")
}

suspend fun ObjectWrapper.Relation.toFilterValue(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    store: ObjectStore
): FilterValue = when (this.format) {
    Relation.Format.SHORT_TEXT -> FilterValue.TextShort(toText(value))
    Relation.Format.LONG_TEXT -> FilterValue.Text(toText(value))
    Relation.Format.NUMBER -> FilterValue.Number(NumberParser.parse(value))
    Relation.Format.STATUS -> FilterValue.Status(
        toStatus(
            value = value,
            store = store
        )
    )
    Relation.Format.TAG -> FilterValue.Tag(
        toTags(
            value = value,
            store = store
        )
    )
    Relation.Format.DATE -> FilterValue.Date(DateParser.parse(value))
    Relation.Format.URL -> FilterValue.Url(toText(value))
    Relation.Format.EMAIL -> FilterValue.Email(toText(value))
    Relation.Format.PHONE -> FilterValue.Phone(toText(value))
    Relation.Format.OBJECT -> FilterValue.Object(toObjects(value, details, urlBuilder))
    Relation.Format.CHECKBOX -> FilterValue.Check(toCheckbox(value))
    else -> throw UnsupportedOperationException("Unsupported relation format:${format}")
}

fun List<ObjectType>.getTypePrettyName(type: String?): String? =
    firstOrNull { it.url == type }?.name

fun List<ObjectType>.getObjectTypeById(types: List<String>?): ObjectType? {
    types?.forEach { type ->
        val objectType = firstOrNull { it.url == type }
        if (objectType != null) {
            return objectType
        }
    }
    return null
}