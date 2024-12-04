package com.anytypeio.anytype.domain.primitives

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
}

class FieldParserImpl @Inject constructor(
    private val dateProvider: DateProvider,
    private val logger: Logger
) : FieldParser {

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

    override fun getObjectName(objectWrapper: ObjectWrapper.Basic): String {
        return objectWrapper.getProperObjectName().orEmpty()
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
}