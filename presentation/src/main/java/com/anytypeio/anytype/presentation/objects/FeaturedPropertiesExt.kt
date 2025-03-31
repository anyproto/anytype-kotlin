package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.permissions.toObjectPermissionsForTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.getTypeForObject
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.isSystemKey
import com.anytypeio.anytype.presentation.relations.linksFeaturedRelation
import com.anytypeio.anytype.presentation.relations.view
import kotlin.collections.mapNotNull

/**
 * Converts an object's featured properties into a [BlockView.FeaturedRelation] view.
 *
 * Retrieves the current object and its type from [details] using [objectId]. If the object's type is
 * TEMPLATE, its target object type is used as the effective type. The method then obtains the featured
 * properties from the object (via keys) and parses the recommended featured property IDs from the effective
 * type using [fieldParser]. It fetches the corresponding properties from [storeOfRelations] and checks for
 * conflicts (any property key not equal to [Relations.DESCRIPTION]). In case of a conflict, the two sets are
 * merged with type properties taking precedence. Finally, permissions are computed and the featured relation
 * view is returned.
 *
 * @param objectId The object's ID.
 * @param blocks The list of blocks; the featured relations block is used.
 * @param urlBuilder Used for URL generation in views.
 * @param fieldParser Parses fields for view rendering.
 * @param storeOfObjectTypes Store for object type information.
 * @param storeOfRelations Store for relation properties.
 * @param details Provides object view context.
 * @param participantCanEdit Indicates if the participant has edit permissions.
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
): BlockView.FeaturedRelation? {

    val block = blocks.find { it.content is Block.Content.FeaturedRelations }

    if (block != null) {
        val views = mutableListOf<ObjectRelationView>()
        val currentObject = details.getObject(objectId)
        if (currentObject?.isValid != true) {
            //object not found or not valid, do not render featured properties
            return null
        }
        val currType = details.getTypeForObject(objectId)

        val objectFeaturedProperties = storeOfRelations.getByKeys(
            keys = currentObject.featuredRelations
        )

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

        val hasConflict = objectFeaturedProperties.any { property -> property.key != Relations.DESCRIPTION } == true

        if (!hasConflict) {

            val featuredViews =  typeRecommendedFeaturedProperties.mapNotNull { property ->
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