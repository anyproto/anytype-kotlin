package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.sets.buildFileViews
import com.anytypeio.anytype.presentation.sets.buildRelationValueObjectViews
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun List<ObjectWrapper.Relation>.views(
    context: Id,
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    featured: List<Id> = emptyList()
): List<DocumentRelationView> = mapNotNull { relation ->
    relation.view(
        context = context,
        details = details,
        values = values,
        urlBuilder = urlBuilder,
        isFeatured = featured.contains(relation.key)
    )
}

fun ObjectWrapper.Relation.view(
    context: Id,
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder,
    isFeatured: Boolean = false
): DocumentRelationView? {
    val relation = this
    val relationFormat = relation.relationFormat
    if (relation.isHidden == true || relation.isArchived == true || relation.isDeleted == true) {
        return null
    }
    return when (relationFormat) {
        RelationFormat.OBJECT -> {
            val objects = values.buildRelationValueObjectViews(
                relationKey = relation.key,
                details = details.details,
                builder = urlBuilder
            )
            DocumentRelationView.Object(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                objects = objects,
                isFeatured = isFeatured
            )
        }
        RelationFormat.FILE -> {
            val files = values.buildFileViews(
                relationKey = relation.key,
                details = details.details
            )
            DocumentRelationView.File(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                files = files,
                isFeatured = isFeatured
            )
        }
        RelationFormat.DATE -> {
            //TODO In DataView Relation Date uses DateFormat and TimeFormat
            // so SimpleDateFormat can be different from what we have here
            // see {SetsExtension:buildGridRow()}
            val format = SimpleDateFormat(DateConst.DEFAULT_DATE_FORMAT, Locale.getDefault())
            val value = values[relation.key]

            val timeInSec = DateParser.parse(value)
            val formattedDate = if (timeInSec != null) {
                format.format(Date(TimeUnit.SECONDS.toMillis(timeInSec)))
            } else {
                null
            }
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = formattedDate,
                isFeatured = isFeatured,
                format = relationFormat
            )
        }
        RelationFormat.STATUS -> {
            statusRelation(
                details = details,
                context = context,
                relationDetails = relation,
                isFeatured = isFeatured
            )
        }
        RelationFormat.TAG -> {
            tagRelation(
                context = context,
                relationDetails = relation,
                details = details,
                isFeatured = isFeatured
            )
        }
        RelationFormat.CHECKBOX -> {
            DocumentRelationView.Checkbox(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                isChecked = values[relation.key] as? Boolean ?: false,
                isFeatured = isFeatured
            )
        }
        RelationFormat.NUMBER -> {
            val value = values[relation.key]
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = NumberParser.parse(value),
                isFeatured = isFeatured,
                format = relationFormat
            )
        }
        RelationFormat.LONG_TEXT,
        RelationFormat.SHORT_TEXT,
        RelationFormat.URL,
        RelationFormat.EMAIL,
        RelationFormat.PHONE -> {
            val value = values[relation.key]
            DocumentRelationView.Default(
                relationId = relation.id,
                relationKey = relation.key,
                name = relation.name.orEmpty(),
                value = value as? String,
                isFeatured = isFeatured,
                format = relationFormat
            )
        }
        else -> null
    }
}

fun statusRelation(
    context: Id,
    relationDetails: ObjectWrapper.Relation,
    details: Block.Details,
    isFeatured: Boolean
): DocumentRelationView? {
    val objectDetails = details.details[context]?.map ?: return null
    val optionId = StatusParser.parse(objectDetails[relationDetails.key])
    val statuses = buildList {
        if (optionId != null) {
            val map = details.details[optionId]?.map ?: return null
            val optionDetails = ObjectWrapper.Basic(map)
            add(
                StatusView(
                    id = optionId,
                    status = optionDetails.name.orEmpty(),
                    color = optionDetails.relationOptionColor.orEmpty()
                )
            )
        }
    }
    return DocumentRelationView.Status(
        relationId = relationDetails.id,
        relationKey = relationDetails.key,
        name = relationDetails.name.orEmpty(),
        isFeatured = isFeatured,
        status = statuses
    )
}

fun tagRelation(
    context: Id,
    relationDetails: ObjectWrapper.Relation,
    details: Block.Details,
    isFeatured: Boolean
): DocumentRelationView? {
    val objectDetails = details.details[context]?.map ?: return null
    val tagViews = mutableListOf<TagView>()
    val tagIds = TagParser.parse(objectDetails[relationDetails.key])
    tagIds.forEach { id ->
        val map = details.details[id]?.map ?: emptyMap()
        val optionDetails = ObjectWrapper.Basic(map)
        val tagView = TagView(
            id = id,
            tag = optionDetails.name.orEmpty(),
            color = optionDetails.relationOptionColor.orEmpty()
        )
        tagViews.add(tagView)
    }
    return DocumentRelationView.Tags(
        relationId = relationDetails.id,
        relationKey = relationDetails.key,
        name = relationDetails.name.orEmpty(),
        isFeatured = isFeatured,
        tags = tagViews
    )
}

object StatusParser {
    fun parse(value: Any?): Id? {
        val result: Id? = when (value) {
            is Id -> value
            is List<*> -> value.firstOrNull().toString()
            else -> null
        }
        return result
    }
}

object TagParser {
    fun parse(value: Any?): List<Id> {
        val result: List<Id> = when (value) {
            is Id -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
        return result
    }
}

object MultiValueParser {
    inline fun <reified T> parse(value: Any?): List<T> = when (value) {
        is T -> listOf(value)
        is List<*> -> value.typeOf()
        else -> emptyList()
    }
}