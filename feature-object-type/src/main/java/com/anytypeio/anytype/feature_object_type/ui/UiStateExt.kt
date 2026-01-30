package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.permissions.ObjectPermissions
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.mapLimitObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Item
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Section
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.templates.TemplateView

//region Mapping
fun ObjectWrapper.Basic.toTemplateView(
    objType: ObjectWrapper.Type,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider
): TemplateView.Template {
    val coverContainer = if (coverType != CoverType.NONE) {
        BasicObjectCoverWrapper(this)
            .getCover(urlBuilder, coverImageHashProvider)
    } else {
        null
    }
    return TemplateView.Template(
        id = id,
        name = name.orEmpty(),
        targetTypeId = TypeId(targetObjectType.orEmpty()),
        emoji = if (!iconEmoji.isNullOrBlank()) iconEmoji else null,
        image = iconImage?.takeIf { it.isNotBlank() }?.let { urlBuilder.thumbnail(it) },
        layout = objType.recommendedLayout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = false,
        targetTypeKey = TypeKey(objType.uniqueKey)
    )
}
//endregion

/**
 * Extension function to safely get a name for the property.
 * If the name is blank, returns a default untitled title.
 */
fun ObjectWrapper.Relation.getName(stringResourceProvider: StringResourceProvider): String =
    if (name.isNullOrBlank()) {
        stringResourceProvider.getUntitledObjectTitle()
    } else {
        name!!
    }

suspend fun buildUiPropertiesList(
    objType: ObjectWrapper.Type,
    stringResourceProvider: StringResourceProvider,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    storeOfRelations: StoreOfRelations,
    objectTypeConflictingPropertiesIds: List<Id>,
    objectPermissions: ObjectPermissions
): List<UiFieldsListItem> {

    val parsedProperties = fieldParser.getObjectTypeParsedProperties(
        objectType = objType,
        storeOfRelations = storeOfRelations,
        objectTypeConflictingPropertiesIds = objectTypeConflictingPropertiesIds
    )

    // The mapping functions already skip the Relations.DESCRIPTION key.
    val headerItems = parsedProperties.header.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            objectPermissions = objectPermissions
        )
    }
    val sidebarItems = parsedProperties.sidebar.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            objectPermissions = objectPermissions
        )
    }
    val hiddenItems = parsedProperties.hidden.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            objectPermissions = objectPermissions
        )
    }
    val conflictedItems = parsedProperties.local.mapNotNull {
        mapToUiPropertiesLocalListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    return buildList {
        if (headerItems.isNotEmpty()) {
            add(Section.Header(canAdd = false))
            addAll(headerItems)
        } else {
            add(Section.Header(canAdd = false, isEmptyState = true))
        }

        if (sidebarItems.isNotEmpty()) {
            add(Section.SideBar(canAdd = objectPermissions.canEditRelationsList))
            addAll(sidebarItems)
        } else {
            add(Section.SideBar(canAdd = objectPermissions.canEditRelationsList, isEmptyState = true))
        }

        //todo file fields are off for now
//        if (fileRecommendedFields.isNotEmpty()) {
//            add(Section.File(canAdd = false))
//            addAll(fileRecommendedFields)
//        }

        if (hiddenItems.isNotEmpty()) {
            add(Section.Hidden(canAdd = false))
            addAll(hiddenItems)
        } else {
            add(Section.Hidden(canAdd = false, isEmptyState = true))
        }

        if (conflictedItems.isNotEmpty()) {
            add(Section.Local(canAdd = false))
            addAll(conflictedItems)
        }
    }
}

/**
 * Maps a property to a draggable UI list item.
 * Returns null if the property key equals DESCRIPTION.
 */
private suspend fun mapToUiPropertiesDraggableListItem(
    property: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    objectPermissions: ObjectPermissions
): UiFieldsListItem? {
    if (property.key == Relations.DESCRIPTION) return null

    return Item.Draggable(
        id = property.id,
        fieldKey = property.key,
        fieldTitle = property.getName(stringResourceProvider),
        format = property.format,
        limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(
            property = property
        ),
        isEditableField = fieldParser.isPropertyEditable(property),
        isPossibleToUnlinkFromType =
        objectPermissions.canUnlinkPropertyFromType &&
                fieldParser.isPropertyCanBeUnlinkedFromType(property),
        isPossibleToMoveToBin = objectPermissions.canUnlinkPropertyFromType &&
                fieldParser.isPossibleToMovePropertyToBin(property),
        isPossibleToDrag = objectPermissions.canEditRelationsList
    )
}

/**
 * Maps a property to a local UI list item.
 * Returns null if the property key equals DESCRIPTION.
 */
private suspend fun mapToUiPropertiesLocalListItem(
    property: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
): UiFieldsListItem? {
    if (property.key == Relations.DESCRIPTION) return null

    return Item.Local(
        id = property.id,
        fieldKey = property.key,
        fieldTitle = property.getName(stringResourceProvider),
        format = property.format,
        limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(
            property = property
        ),
        isEditableField = fieldParser.isPropertyEditable(property)
    )
}

