package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
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
import com.anytypeio.anytype.core_models.primitives.ParsedFields
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_models.primitives.Value
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import javax.inject.Inject
import kotlin.collections.contains
import kotlin.collections.plus

interface FieldParser {
    fun toDate(any: Any?): Field.Date?
    suspend fun getDateObjectByTimeInSeconds(
        timeInSeconds: TimeInSeconds,
        spaceId: SpaceId,
        actionSuccess: suspend (ObjectWrapper.Basic) -> Unit,
        actionFailure: suspend (Throwable) -> Unit
    )

    fun getObjectName(objectWrapper: ObjectWrapper.Basic): String
    fun getObjectName(objectWrapper: ObjectWrapper.Type): String
    fun getObjectTypeIdAndName(
        objectWrapper: ObjectWrapper.Basic,
        types: List<ObjectWrapper.Type>
    ): Pair<Id?, String?>

    suspend fun getObjectParsedFields(
        objectType: ObjectWrapper.Type,
        objFieldKeys: Set<Key>,
        storeOfRelations: StoreOfRelations
    ): ParsedFields

    suspend fun getObjectTypeParsedFields(
        objectType: ObjectWrapper.Type,
        objectTypeConflictingFieldsIds: List<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedFields

    fun isFieldEditable(relation: ObjectWrapper.Relation): Boolean

    fun isFieldCanBeDeletedFromType(field: ObjectWrapper.Relation): Boolean
}

class FieldParserImpl @Inject constructor(
    private val dateProvider: DateProvider,
    private val logger: Logger,
    private val getDateObjectByTimestamp: GetDateObjectByTimestamp,
    private val stringResourceProvider: StringResourceProvider
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
        if (objectWrapper.isDeleted == true) {
            return stringResourceProvider.getDeletedObjectTitle()
        }
        val result = when (objectWrapper.layout) {
            ObjectType.Layout.DATE -> {
                val relativeDate = dateProvider.calculateRelativeDates(
                    dateInSeconds = objectWrapper.getSingleValue<Double>(Relations.TIMESTAMP)
                        ?.toLong()
                )
                stringResourceProvider.getRelativeDateName(relativeDate)
            }

            ObjectType.Layout.NOTE -> {
                objectWrapper.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
            }

            in SupportedLayouts.fileLayouts -> {
                val fileName = if (objectWrapper.name.isNullOrBlank()) {
                    stringResourceProvider.getUntitledObjectTitle()
                } else {
                    objectWrapper.name
                }
                when {
                    objectWrapper.fileExt.isNullOrBlank() -> fileName
                    fileName?.endsWith(".${objectWrapper.fileExt}") == true -> fileName
                    else -> "$fileName.${objectWrapper.fileExt}"
                }
            }

            else -> {
                objectWrapper.name
            }
        }
        return if (result.isNullOrBlank()) {
            stringResourceProvider.getUntitledObjectTitle()
        } else {
            result
        }
    }

    override fun getObjectName(objectWrapper: ObjectWrapper.Type): String {
        val name = objectWrapper.name
        return if (name.isNullOrBlank()) {
            stringResourceProvider.getUntitledObjectTitle()
        } else {
            name
        }
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
    //endregion

    //region Parsed fields
    // Extension function to get valid relations from a list of IDs.
    private suspend fun List<Id>.getValidRelations(store: StoreOfRelations): List<ObjectWrapper.Relation> =
        mapNotNull { id ->
            store.getById(id)?.takeIf { it.isFieldValid() }
        }

    // Consolidated function to build ParsedFields.
    private suspend fun getParsedFields(
        objType: ObjectWrapper.Type,
        localFieldIds: Collection<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedFields {
        val headerFields = objType.recommendedFeaturedRelations.getValidRelations(storeOfRelations)
        val sidebarFields = objType.recommendedRelations.getValidRelations(storeOfRelations)
        val hiddenFields = objType.recommendedHiddenRelations.getValidRelations(storeOfRelations)
        val fileFields = objType.recommendedFileRelations.getValidRelations(storeOfRelations)

        // Combine IDs from all recommended relations.
        val existingIds = (headerFields + sidebarFields + hiddenFields + fileFields)
            .map { it.id }
            .toSet()

        // Filter out fields already present in the recommended groups.
        val allLocalFields = localFieldIds
            .filter { it !in existingIds }
            .toList()
            .getValidRelations(storeOfRelations)

        // Partition local fields into system and non-system fields.
        val (localSystemFields, localFieldsWithoutSystem) = allLocalFields.partition {
            Relations.systemRelationKeys.contains(it.key)
        }

        return ParsedFields(
            header = headerFields,
            sidebar = sidebarFields,
            hidden = hiddenFields,
            localWithoutSystem = localFieldsWithoutSystem,
            localSystem = localSystemFields,
            file = fileFields
        )
    }

    override suspend fun getObjectParsedFields(
        objectType: ObjectWrapper.Type,
        objFieldKeys: Set<Key>,
        storeOfRelations: StoreOfRelations
    ): ParsedFields {
        return getParsedFields(objectType, objFieldKeys, storeOfRelations)
    }

    override suspend fun getObjectTypeParsedFields(
        objectType: ObjectWrapper.Type,
        objectTypeConflictingFieldsIds: List<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedFields {
        return getParsedFields(objectType, objectTypeConflictingFieldsIds, storeOfRelations)
    }

    private fun ObjectWrapper.Relation.isFieldValid(): Boolean =
        isValid && isDeleted != true && isArchived != true && isHidden != true

    override fun isFieldEditable(relation: ObjectWrapper.Relation): Boolean {
        return !(relation.isReadOnly == true ||
                relation.isHidden == true ||
                relation.isArchived == true ||
                relation.isDeleted == true ||
                Relations.systemRelationKeys.contains(relation.key))
    }

    override fun isFieldCanBeDeletedFromType(field: ObjectWrapper.Relation): Boolean {
        return !(field.isHidden == true || Relations.systemRelationKeys.contains(field.key))
    }
    //endregion
}