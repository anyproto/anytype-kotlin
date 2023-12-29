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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ext.DateParser
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
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.core_utils.ext.typeOf
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
import com.anytypeio.anytype.presentation.sets.buildGalleryViews
import com.anytypeio.anytype.presentation.sets.buildListViews
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.toObjectView
import timber.log.Timber

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
    store: ObjectStore,
    objectOrderIds: List<Id> = emptyList()
): Viewer {
    return when (type) {
        DVViewerType.GRID -> {
            buildGridView(
                dataViewRelations = dataViewRelations,
                objects = objects,
                details = details,
                builder = builder,
                store = store,
                objectOrderIds = objectOrderIds
            )
        }
        DVViewerType.GALLERY -> {
            Viewer.GalleryView(
                id = id,
                items = buildGalleryViews(
                    objectIds = objects,
                    details = details,
                    relations = dataViewRelations,
                    coverImageHashProvider = coverImageHashProvider,
                    urlBuilder = builder,
                    objectStore = store,
                    objectOrderIds = objectOrderIds
                ),
                title = name,
                largeCards = cardSize == DVViewerCardSize.LARGE
            )
        }
        DVViewerType.LIST -> {
            val vmap = viewerRelations.associateBy { it.key }
            val visibleRelations = dataViewRelations.filter { relation ->
                val vr = vmap[relation.key]
                vr?.isVisible ?: false
            }
            Viewer.ListView(
                id = id,
                items = buildListViews(
                    objects = objects,
                    relations = visibleRelations,
                    urlBuilder = builder,
                    store = store,
                    objectOrderIds = objectOrderIds
                ),
                title = name
            )
        }
        else -> {
            if (useFallbackView) {
                buildGridView(
                    dataViewRelations = dataViewRelations,
                    objects = objects,
                    details = details,
                    builder = builder,
                    store = store,
                    objectOrderIds = objectOrderIds
                )
            } else {
                Viewer.Unsupported(
                    id = id,
                    title = name,
                    error = "This view type (${type.name.lowercase()}) is not supported on Android yet. See it as grid view?"
                )
            }
        }
    }
}

private fun List<Viewer.GridView.Row>.sortObjects(objectOrderIds: List<Id>): List<Viewer.GridView.Row> {
    val orderMap = objectOrderIds.mapIndexed { index, id -> id to index }.toMap()
    return sortedBy { orderMap[it.id] }
}

private suspend fun DVViewer.buildGridView(
    dataViewRelations: List<ObjectWrapper.Relation>,
    objects: List<Id>,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder,
    store: ObjectStore,
    objectOrderIds: List<Id>
): Viewer {
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
    return Viewer.GridView(
        id = id,
        name = name,
        columns = columns,
        rows = if (objectOrderIds.isNotEmpty()) rows.sortObjects(objectOrderIds) else rows
    )
}

fun title(
    title: Block,
    ctx: Id,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    details: Map<Id, Block.Fields>
): BlockView.Title.Basic {
    val wrapper = ObjectWrapper.Basic(details[ctx]?.map ?: emptyMap())
    val objectDetails = details[ctx]
    val coverContainer = BlockFieldsCoverWrapper(objectDetails).getCover(
        urlBuilder = urlBuilder,
        coverImageHashProvider = coverImageHashProvider
    )
    return BlockView.Title.Basic(
        id = title.id,
        text = wrapper.name.orEmpty(),
        emoji = wrapper.iconEmoji.orNull(),
        image = wrapper.iconImage.orNull()?.let { hash ->
            urlBuilder.thumbnail(hash = hash)
        },
        coverImage = coverContainer.coverImage,
        coverColor = coverContainer.coverColor,
        coverGradient = coverContainer.coverGradient
    )
}

suspend fun ObjectState.simpleRelations(
    viewerId: Id?,
    storeOfRelations: StoreOfRelations
): ArrayList<SimpleRelationView> {
    val state = dataViewState() ?: return arrayListOf()
    val dv = state.dataViewContent
    val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
    return viewer.viewerRelations.toSimpleRelationView(
        dv.relationLinks.mapNotNull {
            storeOfRelations.getByKey(it.key)
        }
    )
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

fun List<ObjectWrapper.Option>.toCreateFilterTagView(
    selected: List<Id>,
): List<CreateFilterView.Tag> = map { option ->
    CreateFilterView.Tag(
        id = option.id,
        name = option.name.orEmpty(),
        color = option.color,
        isSelected = selected.contains(option.id)
    )
}

fun List<ObjectWrapper.Option>.toCreateFilterStatusView(
    selected: List<Id>,
): List<CreateFilterView.Status> = map { option ->
    CreateFilterView.Status(
        id = option.id,
        name = option.name.orEmpty(),
        color = option.color,
        isSelected = selected.contains(option.id)
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

fun ObjectState.DataView.filterExpression(viewerId: Id?): List<DVFilter> {
    val viewer =
        dataViewContent.viewers.find { it.id == viewerId } ?: dataViewContent.viewers.first()
    return viewer.filters
}

fun ObjectWrapper.Relation.toText(value: Any?): String? =
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

fun ObjectWrapper.Relation.toPhone(value: Any?): String? =
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

fun ObjectWrapper.Relation.toCheckbox(value: Any?): Boolean? =
    if (value is Boolean?) {
        value
    } else {
        throw IllegalArgumentException("Relation format $format value should be Boolean, actual:$value")
    }

suspend fun ObjectWrapper.Relation.toTags(
    value: Any?,
    store: ObjectStore
): List<TagView> {
    val ids = value.values<Id>()
    return buildList {
        ids.forEach { id ->
            val option = store.get(id)?.let { ObjectWrapper.Option(it.map) }
            if (option != null) {
                add(
                    TagView(
                        id = option.id,
                        tag = option.name.orEmpty(),
                        color = option.color
                    )
                )
            } else {
                Timber.e("Failed to find corresponding tag option")
            }
        }
    }
}

suspend fun ObjectWrapper.Relation.toStatus(
    value: Any?,
    store: ObjectStore
): StatusView? {
    val filter : Id? = value.values<Id>().firstOrNull()
    return if (filter != null) {
        val option = store.get(filter)?.let { ObjectWrapper.Option(it.map) }
        if (option != null) {
            StatusView(
                id = option.id,
                status = option.name.orEmpty(),
                color = option.color
            )
        } else {
            null
        }
    } else {
        null
    }
}

fun ObjectWrapper.Relation.toObjects(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) : List<ObjectView> {
    val ids = value.values<Id>()
    return buildList {
        ids.forEach { id ->
            val raw = details[id]?.map
            if (!raw.isNullOrEmpty()) {
                val wrapper = ObjectWrapper.Basic(raw)
                add(wrapper.toObjectView(urlBuilder))
            }
        }
    }
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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
            id = id,
            relation = this.relation,
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

inline fun <reified T> Any?.values(): List<T> = when (this) {
    is List<*> -> this.typeOf()
    is T -> listOf(this)
    else -> emptyList()
}