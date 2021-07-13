package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.isValueRequired
import com.anytypeio.anytype.presentation.mapper.*
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewState
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.sets.model.*
import java.util.*
import kotlin.collections.ArrayList


fun ObjectSet.tabs(activeViewerId: String? = null): List<ViewerTabView> {
    val block = blocks.first { it.content is DV }
    val dv = block.content as DV
    return dv.viewers.mapIndexed { index, viewer ->
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

// TODO rework the function to exclude index == -1 scenario
fun ObjectSet.render(
    index: Int = 0,
    ctx: Id,
    builder: UrlBuilder,
): ObjectSetViewState {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = if (index >= 0) dv.viewers[index] else dv.viewers.first()

    val vmap = viewer.viewerRelations.associateBy { it.key }

    val relations = dv.relations.filter { relation ->
        val vr = vmap[relation.key]
        vr?.isVisible ?: false
    }

    val columns = viewer.viewerRelations.toViewerColumns(
        relations = relations,
        filterBy = listOf(ObjectSetConfig.NAME_KEY)
    )

    val rows = mutableListOf<Viewer.GridView.Row>()

    viewerDb[viewer.id]?.let { data ->
        rows.addAll(
            data.records.toGridRecordRows(
                columns = columns,
                relations = relations,
                details = details,
                builder = builder
            )
        )
    }

    val dvview = when (viewer.type) {
        Block.Content.DataView.Viewer.Type.GRID -> {
            Viewer.GridView(
                id = viewer.id,
                source = dv.source,
                name = viewer.name,
                columns = columns,
                rows = rows
            )
        }
        else -> {
            Viewer.Unsupported(
                id = viewer.id,
                title = viewer.name,
                error = "This view type (${viewer.type.name.lowercase()}) is not supported on Android. Coming soon..."
            )
        }
    }

    return ObjectSetViewState(
        title = title(ctx),
        viewer = dvview
    )
}

fun ObjectSet.title(ctx: Id): BlockView.Title.Basic {
    val title = blocks.title()
    return BlockView.Title.Basic(
        id = title.id,
        text = title.content<Block.Content.Text>().text,
        emoji = details[ctx]?.iconEmoji,
        image = details[ctx]?.iconImage
    )
}

fun ObjectSet.simpleRelations(viewerId: Id?): ArrayList<SimpleRelationView> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()

    return viewer.viewerRelations.toSimpleRelations(dv.relations)
}

fun DVViewer.toViewRelation(relation: Relation): SimpleRelationView {
    return viewerRelations.first { it.key == relation.key }.let { vRelation ->
        SimpleRelationView(
            key = relation.key,
            isHidden = relation.isHidden,
            isVisible = vRelation.isVisible,
            title = relation.name,
            format = relation.format.toView()
        )
    }
}

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

fun Relation.toCreateFilterTagView(ids: List<*>? = null): List<CreateFilterView.Tag> =
    selections
        .filter { it.scope == Relation.OptionScope.LOCAL }
        .map { option ->
        CreateFilterView.Tag(
            id = option.id,
            name = option.text,
            color = option.color,
            isSelected = ids?.contains(option.id) ?: false
        )
    }

fun Relation.toCreateFilterStatusView(ids: List<*>? = null): List<CreateFilterView.Status> =
    selections
        .filter { it.scope == Relation.OptionScope.LOCAL }
        .map { option ->
        CreateFilterView.Status(
            id = option.id,
            name = option.text,
            color = option.color,
            isSelected = ids?.contains(option.id) ?: false
        )
    }

fun List<Map<String, Any?>>.toCreateFilterObjectView(
    ids: List<*>? = null,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<CreateFilterView.Object> =
    this.map { record ->
        val id = record[ObjectSetConfig.ID_KEY] as String
        val type = record.type
        val name = record[ObjectSetConfig.NAME_KEY] as String?
        val emoji = record[ObjectSetConfig.EMOJI_KEY] as String?
        val image = record[ObjectSetConfig.IMAGE_KEY] as String?
        CreateFilterView.Object(
            id = id,
            type = objectTypes.getTypePrettyName(type),
            name = name.orEmpty(),
            image = if (image.isNullOrBlank()) null else urlBuilder.thumbnail(image),
            emoji = emoji,
            isSelected = ids?.contains(id) ?: false
        )
    }

fun Relation.toCreateFilterDateView(exactDayTimestamp: Long): List<CreateFilterView.Date> {
    val filterTime = Calendar.getInstance()
    if (exactDayTimestamp != EMPTY_TIMESTAMP) {
        filterTime.timeInMillis = exactDayTimestamp * 1000
    }
    val today = getTodayTimeUnit()
    val tomorrow = getTomorrowTimeUnit()
    val yesterday = getYesterdayTimeUnit()
    val weekAgo = getWeekAgoTimeUnit()
    val weekForward = getWeekAheadTimeUnit()
    val monthAgo = getMonthAgoTimeUnit()
    val monthForward = getMonthAheadTimeUnit()

    val isToday = filterTime.isSameDay(today)
    val isTomorrow = filterTime.isSameDay(tomorrow)
    val isYesterday = filterTime.isSameDay(yesterday)
    val isWeekAgo = filterTime.isSameDay(weekAgo)
    val isWeekAhead = filterTime.isSameDay(weekForward)
    val isMonthAgo = filterTime.isSameDay(monthAgo)
    val isMonthAhead = filterTime.isSameDay(monthForward)
    val isExactDay = !isToday && !isTomorrow && !isYesterday && !isWeekAgo && !isWeekAhead
            && !isMonthAgo && !isMonthAhead

    return listOf(
        CreateFilterView.Date(
            id = key,
            description = TODAY,
            type = DateDescription.TODAY,
            timeInMillis = today.timeInMillis,
            isSelected = isToday
        ),
        CreateFilterView.Date(
            id = key,
            description = TOMORROW,
            type = DateDescription.TOMORROW,
            timeInMillis = tomorrow.timeInMillis,
            isSelected = isTomorrow
        ),
        CreateFilterView.Date(
            id = key,
            description = YESTERDAY,
            type = DateDescription.YESTERDAY,
            timeInMillis = yesterday.timeInMillis,
            isSelected = isYesterday
        ),
        CreateFilterView.Date(
            id = key,
            description = WEEK_AGO,
            type = DateDescription.ONE_WEEK_AGO,
            timeInMillis = weekAgo.timeInMillis,
            isSelected = isWeekAgo
        ),
        CreateFilterView.Date(
            id = key,
            description = WEEK_AHEAD,
            type = DateDescription.ONE_WEEK_FORWARD,
            timeInMillis = weekForward.timeInMillis,
            isSelected = isWeekAhead
        ),
        CreateFilterView.Date(
            id = key,
            description = MONTH_AGO,
            type = DateDescription.ONE_MONTH_AGO,
            timeInMillis = monthAgo.timeInMillis,
            isSelected = isMonthAgo
        ),
        CreateFilterView.Date(
            id = key,
            description = MONTH_AHEAD,
            type = DateDescription.ONE_MONTH_FORWARD,
            timeInMillis = monthForward.timeInMillis,
            isSelected = isMonthAhead
        ),
        CreateFilterView.Date(
            id = key,
            description = EXACT_DAY,
            type = DateDescription.EXACT_DAY,
            timeInMillis = filterTime.timeInMillis,
            isSelected = isExactDay
        )
    )
}

enum class DateDescription {
    TODAY, TOMORROW, YESTERDAY, ONE_WEEK_AGO,
    ONE_WEEK_FORWARD, ONE_MONTH_AGO, ONE_MONTH_FORWARD, EXACT_DAY
}

fun ObjectSet.columns(viewerId: Id): ArrayList<ColumnView> {

    val block = blocks.first { it.content is DV }

    val dv = block.content as DV

    val viewer = dv.viewers.first { it.id == viewerId }

    val columns = viewer.viewerRelations.toViewerColumns(
        dv.relations, listOf()
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

fun Relation.toText(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toUrl(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toPhone(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toEmail(value: Any?): String? =
    if (value is String?) {
        value
    } else {
        throw IllegalArgumentException("Text relation format $format value should be String, actual:$value")
    }

fun Relation.toCheckbox(value: Any?): Boolean? =
    if (value is Boolean?) {
        value
    } else {
        throw IllegalArgumentException("Relation format $format value should be Boolean, actual:$value")
    }

fun Relation.toTags(value: Any?): List<TagView> =
    if (value is List<*>?) {
        val list = arrayListOf<TagView>()
        value?.filterIsInstance<String>()?.forEach { id ->
            val option = selections.first { it.id == id }
            list.add(
                TagView(
                    id = option.id,
                    tag = option.text,
                    color = option.color
                )
            )
        }
        list.toList()
    } else {
        throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
    }

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

fun Relation.toObjects(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
) =
    if (value is List<*>?) {
        val ids = value?.filterIsInstance<String>() ?: emptyList()
        val list = arrayListOf<ObjectView>()
        ids.forEach {
            val image = details[it]?.iconImage
            list.add(
                ObjectView(
                    id = it,
                    name = details[it]?.name.orEmpty(),
                    emoji = details[it]?.iconEmoji,
                    image = if (image.isNullOrBlank()) null else urlBuilder.thumbnail(image),
                    types = details[it]?.type
                )
            )
        }
        list
    } else {
        throw IllegalArgumentException("Relation format $format value should be List<String>, actual:$value")
    }

fun DVFilter.toView(
    relation: Relation,
    details: Map<Id, Block.Fields>,
    isInEditMode: Boolean,
    urlBuilder: UrlBuilder
): FilterView.Expression = when (relation.format) {
    Relation.Format.SHORT_TEXT -> {
        FilterView.Expression.TextShort(
            key = relationKey,
            title = relation.name,
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
            title = relation.name,
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
            title = relation.name,
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
            title = relation.name,
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
            title = relation.name,
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
            title = relation.name,
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
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toNumberView(),
            filterValue = FilterValue.Date(DateParser.parse(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.STATUS -> {
        FilterView.Expression.Status(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Status(relation.toStatus(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.TAG -> {
        FilterView.Expression.Tag(
            key = relationKey,
            title = relation.name,
            operator = operator.toView(),
            condition = condition.toSelectedView(),
            filterValue = FilterValue.Tag(relation.toTags(value)),
            format = relation.format.toView(),
            isValueRequired = condition.isValueRequired(),
            isInEditMode = isInEditMode
        )
    }
    Relation.Format.OBJECT -> {
        FilterView.Expression.Object(
            key = relationKey,
            title = relation.name,
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
            title = relation.name,
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

fun Relation.toFilterValue(
    value: Any?,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): FilterValue =
    when (this.format) {
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

fun List<ObjectType>.getTypePrettyName(type: String): String? = firstOrNull { it.url == type }?.name

fun List<ObjectType>.getObjectTypeById(types: List<String>?): ObjectType? {
    types?.forEach { type ->
        val objectType = firstOrNull { it.url == type }
        if (objectType != null) {
            return objectType
        }
    }
    return null
}