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
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Item
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Section
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.templates.TemplateView

//region Mapping
fun ObjectWrapper.Basic.toTemplateView(
    objType: ObjectWrapper.Type,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
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
 * Extension function to safely get a name for the relation.
 * If the name is blank, returns a default untitled title.
 */
fun ObjectWrapper.Relation.getName(stringResourceProvider: StringResourceProvider): String =
    if (name.isNullOrBlank()) {
        stringResourceProvider.getUntitledObjectTitle()
    } else {
        name!!
    }

suspend fun buildUiFieldsList(
    objType: ObjectWrapper.Type,
    stringResourceProvider: StringResourceProvider,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes,
    storeOfRelations: StoreOfRelations,
    objTypeConflictingFields: List<Id>,
    showHiddenFields: Boolean
): List<UiFieldsListItem> {

    val parsedFields = fieldParser.getObjectTypeParsedFields(
        objectType = objType,
        storeOfRelations = storeOfRelations,
        objectTypeConflictingFieldsIds = objTypeConflictingFields
    )

    // The mapping functions already skip the Relations.DESCRIPTION key.
    val headerItems = parsedFields.header.mapNotNull {
        mapToUiFieldsDraggableListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val sidebarItems = parsedFields.sidebar.mapNotNull {
        mapToUiFieldsDraggableListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val hiddenItems = parsedFields.hidden.mapNotNull {
        mapToUiFieldsDraggableListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }
    val conflictedItems = parsedFields.localWithoutSystem.mapNotNull {
        mapToUiFieldsLocalListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    //this items goes to the Hidden section as draggable items
    val conflictedSystemItems = parsedFields.localSystem.mapNotNull {
        mapToUiFieldsDraggableListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    val fileRecommendedFields = parsedFields.file.mapNotNull {
        mapToUiFieldsDraggableListItem(
            field = it,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
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

        if (showHiddenFields) {
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
 * Shared helper to build the limit object types for a field.
 */
private suspend fun mapLimitObjectTypes(
    relation: ObjectWrapper.Relation,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): List<UiFieldObjectItem> {
    return if (relation.format == RelationFormat.OBJECT && relation.relationFormatObjectTypes.isNotEmpty()) {
        relation.relationFormatObjectTypes.mapNotNull { key ->
            storeOfObjectTypes.getByKey(key)?.let { objType ->
                UiFieldObjectItem(
                    id = objType.id,
                    key = objType.uniqueKey,
                    title = fieldParser.getObjectName(objType),
                    icon = objType.objectIcon(urlBuilder)
                )
            }
        }
    } else {
        emptyList()
    }
}

/**
 * Maps a field to a draggable UI list item.
 * Returns null if the field key equals DESCRIPTION.
 */
private suspend fun mapToUiFieldsDraggableListItem(
    field: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): UiFieldsListItem? {
    if (field.key == Relations.DESCRIPTION) return null

    val limitObjectTypes = mapLimitObjectTypes(field, storeOfObjectTypes, fieldParser, urlBuilder)
    return Item.Draggable(
        id = field.id,
        fieldKey = field.key,
        fieldTitle = field.getName(stringResourceProvider),
        format = field.format,
        limitObjectTypes = limitObjectTypes,
        isEditableField = fieldParser.isFieldEditable(field),
        canDelete = fieldParser.isFieldCanBeDeletedFromType(field)
    )
}

/**
 * Maps a field to a local UI list item.
 * Returns null if the field key equals DESCRIPTION.
 */
private suspend fun mapToUiFieldsLocalListItem(
    field: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): UiFieldsListItem? {
    if (field.key == Relations.DESCRIPTION) return null

    val limitObjectTypes = mapLimitObjectTypes(field, storeOfObjectTypes, fieldParser, urlBuilder)
    return Item.Local(
        id = field.id,
        fieldKey = field.key,
        fieldTitle = field.getName(stringResourceProvider),
        format = field.format,
        limitObjectTypes = limitObjectTypes,
        isEditableField = fieldParser.isFieldEditable(field)
    )
}

