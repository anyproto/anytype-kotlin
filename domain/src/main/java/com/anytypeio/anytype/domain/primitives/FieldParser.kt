package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MAX_SNIPPET_SIZE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.primitives.Field
import com.anytypeio.anytype.core_models.primitives.FieldDateValue
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_models.primitives.Value
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import javax.inject.Inject
import kotlin.collections.contains

interface FieldParser {
    fun toDate(any: Any?): Field.Date?
    fun getObjectName(objectWrapper: ObjectWrapper.Basic): String
    fun getObjectName(map: Map<Id, Block.Fields>, objectId: Id?): String?
}

class FieldParserImpl @Inject constructor(
    private val dateProvider: DateProvider,
    private val logger: Logger
) : FieldParser {

    //region Date field
    override fun toDate(
        any: Any?
    ): Field.Date? {
        return when (val value = FieldDateParser.parse(any)) {
            is Value.Single -> {
                calculateFieldDate(value = value)
            }

            else -> {
                return null
            }
        }
    }

    private fun calculateFieldDate(value: Value.Single<Long>?): Field.Date? {
        val dateInSeconds = value?.single ?: return null
        val relativeDate = dateProvider.calculateRelativeDates(dateInSeconds)
        if (relativeDate == null) {
            return null
        }
        return Field.Date(
            value = Value.Single(
                FieldDateValue(
                    timestamp = TimestampInSeconds(time = dateInSeconds),
                    relativeDate = relativeDate
                )
            )
        )
    }
    //endregion

    //region ObjectWrapper.Basic fields
    override fun getObjectName(objectWrapper: ObjectWrapper.Basic): String {
        return objectWrapper.getProperObjectName().orEmpty()
    }

    private fun ObjectWrapper.Basic.getProperObjectName(): String? {
        return when (layout) {
            ObjectType.Layout.DATE -> {
                getProperDateName()
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

    private fun ObjectWrapper.Basic.getProperDateName(): String {
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
    //endregion

    //region Block.Fields
    override fun getObjectName(map: Map<Id, Block.Fields>, objectId: Id?): String? {
        return map.getProperObjectName(id = objectId)
    }

    private fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
        if (id == null) return null
        val layoutCode = this[id]?.layout?.toInt()
        return if (layoutCode == ObjectType.Layout.NOTE.code) {
            this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
        } else {
            this[id]?.name
        }
    }
    //endregion
}