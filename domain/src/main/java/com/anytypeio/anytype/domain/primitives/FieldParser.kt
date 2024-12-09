package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MAX_SNIPPET_SIZE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.primitives.Field
import com.anytypeio.anytype.core_models.primitives.FieldDateValue
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_models.primitives.Value
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import javax.inject.Inject
import kotlin.collections.contains

interface FieldParser {
    fun toDate(any: Any?): Field.Date?
    suspend fun getDateObjectByTimeInSeconds(
        timeInSeconds: TimeInSeconds,
        spaceId: SpaceId,
        actionSuccess: suspend (ObjectWrapper.Basic) -> Unit,
        actionFailure: suspend (Throwable) -> Unit
    )

    fun getObjectName(objectWrapper: ObjectWrapper.Basic): String
    fun getObjectTypeIdAndName(
        objectWrapper: ObjectWrapper.Basic,
        types: List<ObjectWrapper.Type>
    ): Pair<Id?, String?>
}

class FieldParserImpl @Inject constructor(
    private val dateProvider: DateProvider,
    private val logger: Logger,
    private val getDateObjectByTimestamp: GetDateObjectByTimestamp
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

    override suspend fun getDateObjectByTimeInSeconds(
        timeInSeconds: TimeInSeconds,
        spaceId: SpaceId,
        actionSuccess: suspend (ObjectWrapper.Basic) -> Unit,
        actionFailure: suspend (Throwable) -> Unit
    ) {
        val params = GetDateObjectByTimestamp.Params(
            space = spaceId,
            timestampInSeconds = timeInSeconds
        )
        getDateObjectByTimestamp.async(params).fold(
            onSuccess = { dateObject ->
                logger.logInfo("Date object: $dateObject")
                if (dateObject == null) {
                    logger.logWarning("Date object is null")
                    actionFailure(Exception("Date object is null"))
                    return@fold
                }
                val obj = ObjectWrapper.Basic(dateObject)
                if (obj.isValid) {
                    actionSuccess(obj)
                } else {
                    logger.logWarning("Date object is invalid")
                    actionFailure(Exception("Date object is invalid"))
                }
            },
            onFailure = { e ->
                logger.logException(e, "Failed to get date object by timestamp")
                actionFailure(e)
            }
        )
    }

    private fun calculateFieldDate(value: Value.Single<Long>?): Field.Date? {
        val dateInSeconds = value?.single ?: return null
        val relativeDate = dateProvider.calculateRelativeDates(dateInSeconds)
        if (relativeDate is RelativeDate.Empty) {
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

    override fun getObjectTypeIdAndName(
        objectWrapper: ObjectWrapper.Basic,
        types: List<ObjectWrapper.Type>
    ): Pair<Id?, String?> {
        val id = when (objectWrapper.layout) {
            ObjectType.Layout.DATE -> ObjectTypeIds.DATE
            else -> objectWrapper.type.firstOrNull()
        }

        return if (id != null) {
            id to types.find { it.id == id }?.name
        } else {
            null to null
        }
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
}