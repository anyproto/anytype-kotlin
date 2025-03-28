package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
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
    showHiddenProperty: Boolean
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
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val sidebarItems = parsedProperties.sidebar.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val hiddenItems = parsedProperties.hidden.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val conflictedItems = parsedProperties.localWithoutSystem.mapNotNull {
        mapToUiPropertiesLocalListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    //this items goes to the Hidden section as draggable items
    val conflictedSystemItems = parsedProperties.localSystem.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    val fileRecommendedFields = parsedProperties.file.mapNotNull {
        mapToUiPropertiesDraggableListItem(
            property = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    return buildList {
        add(Section.Header(canAdd = false))
        addAll(headerItems)

        add(Section.SideBar(canAdd = true))
        addAll(sidebarItems)

        //todo file fields are off for now
//        if (fileRecommendedFields.isNotEmpty()) {
//            add(Section.File(canAdd = false))
//            addAll(fileRecommendedFields)
//        }

        if (showHiddenProperty) {
            add(Section.Hidden(canAdd = false))
            addAll(hiddenItems)
            addAll(conflictedSystemItems)
        }

        if (conflictedItems.isNotEmpty()) {
            add(Section.Local(canAdd = false))
            addAll(conflictedItems)
        }
    }
}

/**
 * Shared helper to build the limit object types for a property.
 */
private suspend fun mapLimitObjectTypes(
    property: ObjectWrapper.Relation,
    storeOfObjectTypes: StoreOfObjectTypes
): List<Id> {
    return if (property.format == RelationFormat.OBJECT && property.relationFormatObjectTypes.isNotEmpty()) {
        property.relationFormatObjectTypes.mapNotNull { id ->
            storeOfObjectTypes.get(id)?.let { objType ->
                if (objType.isValid) {
                    objType.id
                } else {
                    null
                }
            }
        }
    } else {
        emptyList()
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
    fieldParser: FieldParser
): UiFieldsListItem? {
    if (property.key == Relations.DESCRIPTION) return null

    return Item.Draggable(
        id = property.id,
        fieldKey = property.key,
        fieldTitle = property.getName(stringResourceProvider),
        format = property.format,
        limitObjectTypes = mapLimitObjectTypes(
            property = property,
            storeOfObjectTypes = storeOfObjectTypes
        ),
        isEditableField = fieldParser.isPropertyEditable(property),
        isPossibleToUnlinkFromType = fieldParser.isPropertyCanBeDeletedFromType(property)
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
        limitObjectTypes = mapLimitObjectTypes(
            property = property,
            storeOfObjectTypes = storeOfObjectTypes
        ),
        isEditableField = fieldParser.isPropertyEditable(property)
    )
}

