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
import com.anytypeio.anytype.core_models.primitives.ParsedProperties
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_models.primitives.Value
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getValidRelations
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

    fun getObjectName(objectWrapper: ObjectWrapper.Basic, useUntitled: Boolean = true): String
    fun getObjectName(objectWrapper: ObjectWrapper.Type): String
    fun getObjectPluralName(objectWrapper: ObjectWrapper.Type): String
    fun getObjectPluralName(objectWrapper: ObjectWrapper.Basic, useUntitled: Boolean = true): String
    fun getObjectNameOrPluralsForTypes(objectWrapper: ObjectWrapper.Basic, ): String
    fun getObjectTypeIdAndName(
        objectWrapper: ObjectWrapper.Basic,
        types: List<ObjectWrapper.Type>
    ): Pair<Id?, String?>

    suspend fun getObjectParsedProperties(
        objectType: ObjectWrapper.Type?,
        objPropertiesKeys: List<Key>,
        storeOfRelations: StoreOfRelations
    ): ParsedProperties

    suspend fun getObjectTypeParsedProperties(
        objectType: ObjectWrapper.Type,
        objectTypeConflictingPropertiesIds: List<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedProperties

    fun isPropertyEditable(property: ObjectWrapper.Relation): Boolean

    fun isPropertyCanBeUnlinkedFromType(property: ObjectWrapper.Relation): Boolean

    fun isPossibleToMovePropertyToBin(property: ObjectWrapper.Relation): Boolean
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
    override fun getObjectName(objectWrapper: ObjectWrapper.Basic, useUntitled: Boolean): String {
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
            if (useUntitled) stringResourceProvider.getUntitledObjectTitle() else ""
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

    override fun getObjectPluralName(objectWrapper: ObjectWrapper.Type): String {
        val name = objectWrapper.pluralName?.takeIf { it.isNotEmpty() } ?: objectWrapper.name
        return if (name.isNullOrBlank()) {
            stringResourceProvider.getUntitledObjectTitle()
        } else {
            name
        }
    }

    override fun getObjectPluralName(objectWrapper: ObjectWrapper.Basic, useUntitled: Boolean): String {
        val name = objectWrapper.pluralName?.takeIf { it.isNotEmpty() } ?: getObjectName(objectWrapper, useUntitled)
        return if (name.isEmpty()) {
            if (useUntitled) stringResourceProvider.getUntitledObjectTitle() else ""
        } else {
            name
        }
    }

    override fun getObjectNameOrPluralsForTypes(objectWrapper: ObjectWrapper.Basic): String {
        return if (objectWrapper.layout == ObjectType.Layout.OBJECT_TYPE) {
            objectWrapper.pluralName?.takeIf { it.isNotEmpty() } ?: getObjectName(objectWrapper)
        } else {
            getObjectName(objectWrapper)
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

    //region Parsed properties

    // Consolidated function to build Parsed Properties.
    private suspend fun getParsedProperties(
        objType: ObjectWrapper.Type?,
        localPropertiesIds: Collection<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedProperties {

        if (objType == null || !objType.isValid || objType.isDeleted == true) {
            val allLocalProperties = storeOfRelations.getValidRelations(
                ids = localPropertiesIds.toList()
            )
            return ParsedProperties(
                header = emptyList(),
                sidebar = emptyList(),
                hidden = emptyList(),
                local = allLocalProperties,
                file = emptyList()
            )
        }

        // Clean recommended IDs based on priority.
        // recommendedFeaturedRelations always remain.
        val featuredIds = objType.recommendedFeaturedRelations.distinct()

        // recommendedRelations: remove any ids that appear in featuredIds.
        val relationsIds = objType.recommendedRelations
            .filter { it !in featuredIds }
            .distinct()

        // recommendedFileRelations: remove ids that are in featuredIds or relationsIds.
        val fileIds = objType.recommendedFileRelations
            .filter { it !in featuredIds && it !in relationsIds }
            .distinct()

        // recommendedHiddenRelations: remove ids that are in featuredIds, relationsIds, or fileIds.
        val hiddenIds = objType.recommendedHiddenRelations
            .filter { it !in featuredIds && it !in relationsIds && it !in fileIds }
            .distinct()

        // Fetch valid properties for each recommended group.
        val headerProperties = storeOfRelations.getValidRelations(ids = featuredIds)
        val sidebarProperties = storeOfRelations.getValidRelations(ids = relationsIds)
        val fileProperties = storeOfRelations.getValidRelations(ids = fileIds)
        val hiddenProperties = storeOfRelations.getValidRelations(ids = hiddenIds)

        // Combine IDs from all recommended properties.
        val existingIds = (headerProperties + sidebarProperties + hiddenProperties + fileProperties)
            .map { it.id }
            .toSet()

        // Filter out properties already present in the recommended groups.
        val allLocalProperties = storeOfRelations.getValidRelations(
            ids = localPropertiesIds.filter { it !in existingIds }
        )

        return ParsedProperties(
            header = headerProperties,
            sidebar = sidebarProperties,
            hidden = hiddenProperties,
            local = allLocalProperties,
            file = fileProperties
        )
    }

    override suspend fun getObjectParsedProperties(
        objectType: ObjectWrapper.Type?,
        objPropertiesKeys: List<Key>,
        storeOfRelations: StoreOfRelations
    ): ParsedProperties {
        val localFieldIds = storeOfRelations.getByKeys(
            keys = objPropertiesKeys
        ).mapNotNull {
            if (it.isValidToUse) {
                it.id
            } else {
                null
            }
        }
        return getParsedProperties(
            objType = objectType,
            localPropertiesIds = localFieldIds,
            storeOfRelations = storeOfRelations
        )
    }

    override suspend fun getObjectTypeParsedProperties(
        objectType: ObjectWrapper.Type,
        objectTypeConflictingPropertiesIds: List<Id>,
        storeOfRelations: StoreOfRelations
    ): ParsedProperties {
        return getParsedProperties(
            objType = objectType,
            localPropertiesIds = objectTypeConflictingPropertiesIds,
            storeOfRelations = storeOfRelations
        )
    }

    override fun isPropertyEditable(relation: ObjectWrapper.Relation): Boolean {
        return !(relation.isReadOnly == true ||
                relation.isHidden == true ||
                relation.isArchived == true ||
                relation.isDeleted == true ||
                Relations.systemRelationKeys.contains(relation.key))
    }

    override fun isPropertyCanBeUnlinkedFromType(property: ObjectWrapper.Relation): Boolean {
        return property.isHidden != true
    }

    override fun isPossibleToMovePropertyToBin(property: ObjectWrapper.Relation): Boolean {
        return property.isHidden != true && !property.restrictions.contains(ObjectRestriction.DELETE)
    }

    //endregion
}