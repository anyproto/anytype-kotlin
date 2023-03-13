package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
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
): List<ObjectRelationView> = mapNotNull { relation ->
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
): ObjectRelationView? {
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
            ObjectRelationView.Object(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                objects = objects,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
            )
        }
        RelationFormat.FILE -> {
            val files = values.buildFileViews(
                relationKey = relation.key,
                details = details.details
            )
            ObjectRelationView.File(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                files = files,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
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
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = formattedDate,
                featured = isFeatured,
                format = relationFormat,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
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
            ObjectRelationView.Checkbox(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                isChecked = values[relation.key] as? Boolean ?: false,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                system = relation.key.isSystemKey()
            )
        }
        RelationFormat.NUMBER -> {
            val value = values[relation.key]
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = NumberParser.parse(value),
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                format = relationFormat,
                system = relation.key.isSystemKey()
            )
        }
        RelationFormat.LONG_TEXT,
        RelationFormat.SHORT_TEXT,
        RelationFormat.URL,
        RelationFormat.EMAIL,
        RelationFormat.PHONE -> {
            val value = values[relation.key]
            ObjectRelationView.Default(
                id = relation.id,
                key = relation.key,
                name = relation.name.orEmpty(),
                value = value as? String,
                featured = isFeatured,
                readOnly = relation.isReadonlyValue,
                format = relationFormat,
                system = relation.key.isSystemKey()
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
): ObjectRelationView? {
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
    return ObjectRelationView.Status(
        id = relationDetails.id,
        key = relationDetails.key,
        name = relationDetails.name.orEmpty(),
        featured = isFeatured,
        readOnly = relationDetails.isReadonlyValue,
        status = statuses,
        system = relationDetails.key.isSystemKey()
    )
}

fun tagRelation(
    context: Id,
    relationDetails: ObjectWrapper.Relation,
    details: Block.Details,
    isFeatured: Boolean
): ObjectRelationView? {
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
    return ObjectRelationView.Tags(
        id = relationDetails.id,
        key = relationDetails.key,
        name = relationDetails.name.orEmpty(),
        featured = isFeatured,
        readOnly = relationDetails.isReadonlyValue,
        tags = tagViews,
        system = relationDetails.key.isSystemKey()
    )
}

fun Block.Details.objectTypeRelation(
    relationKey: Key,
    isFeatured: Boolean,
    objectTypeId: Id
): ObjectRelationView {
    val typeDetails = details[objectTypeId]?.map
    val objectType = if (typeDetails != null) {
        ObjectWrapper.Type(typeDetails)
    } else {
        null
    }
    return if (objectType == null || objectType.isDeleted == true) {
        ObjectRelationView.ObjectType.Deleted(
            id = objectTypeId,
            key = relationKey,
            featured = isFeatured,
            readOnly = false,
            system = relationKey.isSystemKey()
        )
    } else {
        ObjectRelationView.ObjectType.Base(
            id = objectTypeId,
            key = relationKey,
            name = details[objectTypeId]?.name.orEmpty(),
            featured = isFeatured,
            readOnly = false,
            type = objectTypeId,
            system = relationKey.isSystemKey()
        )
    }
}

object StatusParser {
    fun parse(value: Any?): Id? {
        val result: Id? = when (value) {
            is Id -> value
            is List<*> -> value.typeOf<Id>().firstOrNull()
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