package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.permissions.toObjectPermissionsForTypes
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.getTypeForObject
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.isSystemKey
import com.anytypeio.anytype.presentation.relations.linksFeaturedRelation
import com.anytypeio.anytype.presentation.relations.view
import kotlin.collections.mapNotNull

enum class ConflictResolutionStrategy {
    MERGE,
    OBJECT_ONLY
}

/**
 * Converts an object's featured properties into a [BlockView.FeaturedRelation] view.
 *
 * Retrieves the current object and its type from [details] using [objectId]. If the object's type is TEMPLATE,
 * its target type is used as the effective type. The method then obtains the featured properties from the object
 * (via keys) and parses the recommended featured property IDs from the effective type using [fieldParser]. It fetches
 * the corresponding properties from [storeOfRelations] and checks for conflict.
 *
 * In case of a conflict [hasFeaturedConflict], the [conflictResolution] strategy is applied:
 * - [ConflictResolutionStrategy.MERGE]: Merges the type and object properties, giving precedence to type properties.
 * - [ConflictResolutionStrategy.OBJECT_ONLY] (default): Uses only the object properties.
 *
 * Finally, permissions are computed and the featured relation view is returned.
 *
 * @param objectId The object's ID.
 * @param blocks The list of blocks; the featured relations block is used.
 * @param urlBuilder Used for URL generation in views.
 * @param fieldParser Parses fields for view rendering.
 * @param storeOfObjectTypes Store for object type information.
 * @param storeOfRelations Store for relation properties.
 * @param details Provides object view context.
 * @param participantCanEdit Indicates if the participant has edit permissions.
 * @param conflictResolution Determines which strategy to use when a conflict is detected.
 *
 * @return The [BlockView.FeaturedRelation] view, or `null` if no valid featured block or object is found.
 */
suspend fun toFeaturedPropertiesViews(
    objectId: Id,
    blocks: List<Block>,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    storeOfRelations: StoreOfRelations,
    details: ObjectViewDetails,
    participantCanEdit: Boolean,
    conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.OBJECT_ONLY
): BlockView.FeaturedRelation? {

    val block = blocks.find { it.content is Block.Content.FeaturedRelations }

    if (block != null) {
        val views = mutableListOf<ObjectRelationView>()
        val currentObject = details.getObject(objectId)
        if (currentObject?.isValid != true) {
            //object not found or not valid, do not render featured properties
            return null
        }
        val objectFeaturedProperties = storeOfRelations.getByKeys(
            keys = currentObject.featuredRelations
        )

        val currType = details.getTypeForObject(objectId)
        // Determine the effective object type. If the type is TEMPLATE, use the target object type.
        val effectiveType = if (currType?.uniqueKey == ObjectTypeIds.TEMPLATE) {
            currentObject.targetObjectType?.let { storeOfObjectTypes.get(it) }
        } else {
            currType
        }

        val typeRecommendedFeaturedPropertiesIds = if (effectiveType != null) {
            // Parse the object's properties using the effective type.
            val parsedProperties = fieldParser.getObjectParsedProperties(
                objectType = effectiveType,
                objPropertiesKeys = currentObject.map.keys.toList(),
                storeOfRelations = storeOfRelations
            )
            parsedProperties.header.map { it.id }
        } else {
            emptyList()
        }

        val typeRecommendedFeaturedProperties = storeOfRelations.getById(
            ids = typeRecommendedFeaturedPropertiesIds
        )

        val hasConflict = hasFeaturedConflict(
            objectFeatured = objectFeaturedProperties,
            typeRecommended = typeRecommendedFeaturedProperties
        )

        if (!hasConflict) {
            val featuredViews = typeRecommendedFeaturedProperties.mapNotNull { property ->
                property.toView(
                    currentObject = currentObject,
                    typeOfCurrentObject = currType,
                    details = details,
                    urlBuilder = urlBuilder,
                    storeOfObjectTypes = storeOfObjectTypes,
                    fieldParser = fieldParser,
                    storeOfRelations = storeOfRelations
                )
            }
            views.addAll(featuredViews)
        } else {
            when (conflictResolutionStrategy) {
                ConflictResolutionStrategy.MERGE -> {
                    val displayPropertiesMap = LinkedHashMap<String, ObjectWrapper.Relation>()
                    for (prop in typeRecommendedFeaturedProperties) {
                        displayPropertiesMap[prop.id] = prop
                    }
                    for (prop in objectFeaturedProperties) {
                        displayPropertiesMap.putIfAbsent(prop.id, prop)
                    }
                    val featuredViews = displayPropertiesMap.values.mapNotNull { property ->
                        property.toView(
                            currentObject = currentObject,
                            typeOfCurrentObject = currType,
                            details = details,
                            urlBuilder = urlBuilder,
                            storeOfObjectTypes = storeOfObjectTypes,
                            fieldParser = fieldParser,
                            storeOfRelations = storeOfRelations
                        )
                    }
                    views.addAll(featuredViews)
                }
                ConflictResolutionStrategy.OBJECT_ONLY -> {
                    val featuredViews = objectFeaturedProperties.mapNotNull { property ->
                        property.toView(
                            currentObject = currentObject,
                            typeOfCurrentObject = currType,
                            details = details,
                            urlBuilder = urlBuilder,
                            storeOfObjectTypes = storeOfObjectTypes,
                            fieldParser = fieldParser,
                            storeOfRelations = storeOfRelations
                        )
                    }
                    views.addAll(featuredViews)
                }
            }
        }

        val canChangeType = currType?.toObjectPermissionsForTypes(participantCanEdit)?.canChangeType == true
        return BlockView.FeaturedRelation(
            id = block.id,
            relations = views,
            allowChangingObjectType = canChangeType,
            isTodoLayout = currType?.recommendedLayout == ObjectType.Layout.TODO,
            hasFeaturePropertiesConflict = hasConflict
        )
    } else {
        //featured block not found
        return null
    }
}

/**
 * Determines if there is a conflict between the object's featured properties and the type's recommended featured properties.
 *
 * A conflict exists if any property in [objectFeatured] (ignoring those with key [Relations.DESCRIPTION])
 * is not present in [typeRecommended] in the same order.
 *
 * @param objectFeatured List of featured properties from the object.
 * @param typeRecommended List of recommended featured properties from the type.
 * @return `true` if a conflict exists, `false` otherwise.
 */
private fun hasFeaturedConflict(
    objectFeatured: List<ObjectWrapper.Relation>,
    typeRecommended: List<ObjectWrapper.Relation>
): Boolean {
    // Filter out properties with key == Relations.DESCRIPTION
    val filtered = objectFeatured.filter { it.key != Relations.DESCRIPTION }
    if (filtered.isEmpty()) return false

    // Check if filtered list appears in typeRecommended in the same order
    var index = 0
    for (property in typeRecommended) {
        if (index < filtered.size && property.id == filtered[index].id) {
            index++
        }
    }
    // If not all properties were found in order, we have a conflict.
    return index != filtered.size
}

private suspend fun ObjectWrapper.Relation.toView(
    currentObject: ObjectWrapper.Basic,
    typeOfCurrentObject: ObjectWrapper.Type?,
    details: ObjectViewDetails,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes,
    storeOfRelations: StoreOfRelations,
    fieldParser: FieldParser
) : ObjectRelationView? {
    val property = this
    val propertyKey = property.key
    return when (propertyKey) {
        Relations.DESCRIPTION -> null
        Relations.TYPE -> {
            if (typeOfCurrentObject == null || typeOfCurrentObject.isDeleted == true) {
                val id = currentObject.getProperType()
                if (id == null) {
                    null
                } else {
                    ObjectRelationView.ObjectType.Deleted(
                        id = id,
                        key = propertyKey,
                        featured = true,
                        readOnly = false,
                        system = false
                    )
                }
            } else {
                ObjectRelationView.ObjectType.Base(
                    id = id,
                    key = propertyKey,
                    name = fieldParser.getObjectName(typeOfCurrentObject),
                    featured = true,
                    readOnly = false,
                    type = typeOfCurrentObject.id,
                    system = uniqueKey?.isSystemKey() == true
                )
            }
        }
        Relations.SET_OF -> {

            val source = currentObject.setOf.firstOrNull()

            val wrapper = if (source != null) {
                details.getObject(source)
            } else {
                null
            }

            val isValid = wrapper?.isValid == true
            val isDeleted = wrapper?.isDeleted == true
            val isReadOnly = wrapper?.relationReadonlyValue == true

            val sources = if (isValid && !isDeleted) {
                listOf(
                    wrapper.toObjectViewDefault(
                        urlBuilder = urlBuilder,
                        fieldParser = fieldParser,
                        storeOfObjectTypes = storeOfObjectTypes
                    )
                )
            } else {
                emptyList()
            }

            ObjectRelationView.Source(
                id = currentObject.id,
                key = propertyKey,
                name = Relations.RELATION_NAME_EMPTY,
                featured = true,
                readOnly = isReadOnly,
                sources = sources,
                system = propertyKey.isSystemKey()
            )
        }
        Relations.BACKLINKS, Relations.LINKS -> {
            details.linksFeaturedRelation(
                relations = storeOfRelations.getAll(),
                ctx = currentObject.id,
                relationKey = propertyKey,
                isFeatured = true
            )
        }
        else -> {
            property.view(
                details = details,
                values = currentObject.map,
                urlBuilder = urlBuilder,
                isFeatured = true,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }
}

suspend fun hasLayoutConflict(
    blocks: List<BlockView>,
    currentObject: ObjectWrapper.Basic?,
    storeOfObjectTypes: StoreOfObjectTypes,
): Boolean {

    val featuredBlock = blocks.firstOrNull { it is BlockView.FeaturedRelation }

    if (currentObject == null) {
        return false
    }

    val hasFeaturedPropertiesConflict =
        (featuredBlock as? BlockView.FeaturedRelation)?.hasFeaturePropertiesConflict == true
    val currentObjectType = storeOfObjectTypes.getTypeOfObject(currentObject)

    val objectLayout = currentObject.layout

    val hasObjectLayoutConflict =
        objectLayout != null && currentObjectType != null && currentObjectType.isValid
                && objectLayout != currentObjectType.recommendedLayout

    val hasAlignConflict =
        objectLayout != null && currentObjectType != null && currentObjectType.isValid
                && !hasEqualLayoutAlign(currentObject, currentObjectType)

    return hasObjectLayoutConflict || hasFeaturedPropertiesConflict || hasAlignConflict
}

private fun hasEqualLayoutAlign(
    currentObject: ObjectWrapper.Basic,
    objectType: ObjectWrapper.Type
): Boolean {
    if (!currentObject.isValid || !objectType.isValid) {
        return true
    }
    val currentLayoutAlign: Double? = currentObject.getSingleValue<Double>(Relations.LAYOUT_ALIGN)
    val typeLayoutAlign: Double? = objectType.getSingleValue<Double>(Relations.LAYOUT_ALIGN)
    return currentLayoutAlign == typeLayoutAlign
}