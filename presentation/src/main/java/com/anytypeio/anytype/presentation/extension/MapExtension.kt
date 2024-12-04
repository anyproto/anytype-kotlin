package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.presentation.objects.SupportedLayouts

const val MAX_SNIPPET_SIZE = 30

fun Map<Id, Block.Fields>.updateFields(update: Map<Id, Block.Fields>): Map<Id, Block.Fields> {
    val result = this.toMutableMap()
    for ((key, value) in update) {
        result[key] = value
    }
    return result
}

fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
    if (id == null) return null
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        this[id]?.name
    }
}

private fun ObjectWrapper.Basic.getProperDateName(dateProvider: DateProvider): String {
    val timestampInSeconds = getSingleValue<Double>(Relations.TIMESTAMP)?.toLong()
    if (timestampInSeconds != null) {
        val (formattedDate, _) = dateProvider.formatTimestampToDateAndTime(
            timestamp = timestampInSeconds * 1000,
        )
        return formattedDate
    } else {
        return ""
    }
}

fun ObjectWrapper.Basic.getProperObjectName(dateProvider: DateProvider): String? {
    return when (layout) {
        ObjectType.Layout.DATE -> {
            getProperDateName(dateProvider)
        }
        ObjectType.Layout.NOTE -> {
            snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
        }
        in SupportedLayouts.fileLayouts -> {
            val fileName = if (name.isNullOrBlank()) "Untitled" else name.orEmpty()
            if (fileExt.isNullOrBlank()) {
                fileName
            } else {
                if (fileName.endsWith(".$fileExt")) {
                    fileName
                } else {
                    "$fileName.$fileExt"
                }
            }
        }
        else -> {
            name
        }
    }
}
