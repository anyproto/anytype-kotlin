package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.sets.*
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

fun List<Relation>.views(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder
): List<DocumentRelationView> = mapNotNull { relation -> relation.view(details, values, urlBuilder) }

fun Relation.view(
    details: Block.Details,
    values: Map<String, Any?>,
    urlBuilder: UrlBuilder
) : DocumentRelationView? {
    val relation = this
    return when {
        relation.isHidden -> null
        relation.format == Relation.Format.OBJECT -> {
            val objects = values.buildObjectViews(
                columnKey = relation.key,
                details = details.details,
                builder = urlBuilder
            )
            DocumentRelationView.Object(
                relationId = relation.key,
                name = relation.name,
                objects = objects
            )
        }
        relation.format == Relation.Format.FILE -> {
            val files = values.buildFileViews(
                columnKey = relation.key,
                details = details.details
            )
            DocumentRelationView.File(
                relationId = relation.key,
                name = relation.name,
                files = files
            )
        }
        relation.format == Relation.Format.DATE -> {
            val format = SimpleDateFormat(MONTH_DAY_AND_YEAR, Locale.US)
            val time: Long? = (values[relation.key] as Double?)?.toLong()
            val timestamp = if (time != null) time * 1000L else null
            DocumentRelationView.Default(
                relationId = relation.key,
                name = relation.name,
                value = if (timestamp != null) {
                    val date = Date(timestamp)
                    format.format(date)
                } else {
                    null
                }
            )
        }
        relation.format == Relation.Format.STATUS -> {
            val status = values.buildStatusViews(
                selOptions = relation.selections,
                columnKey = relation.key
            )
            DocumentRelationView.Status(
                relationId = relation.key,
                name = relation.name,
                status = status
            )
        }
        relation.format == Relation.Format.TAG -> {
            val tags = values.buildTagViews(
                selOptions = relation.selections,
                columnKey = relation.key
            )
            DocumentRelationView.Tags(
                relationId = relation.key,
                name = relation.name,
                tags = tags
            )
        }
        relation.format == Relation.Format.CHECKBOX -> {
            DocumentRelationView.Checkbox(
                relationId = relation.key,
                name = relation.name,
                isChecked = values[relation.key] as? Boolean ?: false
            )
        }
        else -> {
            DocumentRelationView.Default(
                relationId = relation.key,
                name = relation.name,
                value = values[relation.key] as? String
            )
        }
    }
}