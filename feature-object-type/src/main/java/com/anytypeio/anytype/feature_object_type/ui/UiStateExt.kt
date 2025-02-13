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
    objectId: Id,
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
        layout = layout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = false,
        targetTypeKey = TypeKey(objectId)
    )
}
//endregion

/**
 * Extension function to safely get a name for the relation.
 * If the name is blank, returns a default untitled title.
 */
private fun ObjectWrapper.Relation.getName(stringResourceProvider: StringResourceProvider): String =
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
    objTypeConflictingFields: List<Id>
): List<UiFieldsListItem> {

    val parsedFields = fieldParser.getObjectTypeParsedFields(
        objectType = objType,
        storeOfRelations = storeOfRelations,
        objectTypeConflictingFieldsIds = objTypeConflictingFields
    )

    val headerItems = parsedFields.featured.mapNotNull { field ->
        if (field.key == Relations.DESCRIPTION) {
            null
        } else {
            mapToUiFieldsDraggableListItem(
                relation = field,
                stringResourceProvider = stringResourceProvider,
                fieldParser = fieldParser,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }

    val sidebarItems = parsedFields.sidebar.mapNotNull { field ->
        if (field.key == Relations.DESCRIPTION) {
            null
        } else {
            mapToUiFieldsDraggableListItem(
                relation = field,
                stringResourceProvider = stringResourceProvider,
                fieldParser = fieldParser,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }

    val hiddenItems = parsedFields.hidden.mapNotNull { field ->
        if (field.key == Relations.DESCRIPTION) {
            null
        } else {
            mapToUiFieldsDraggableListItem(
                relation = field,
                stringResourceProvider = stringResourceProvider,
                fieldParser = fieldParser,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }

    val conflictedItems = parsedFields.conflicted.mapNotNull { field ->
        if (field.key == Relations.DESCRIPTION) {
            null
        } else {
            mapToUiFieldsLocalListItem(
                relation = field,
                stringResourceProvider = stringResourceProvider,
                fieldParser = fieldParser,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }

    return buildList {

        if (headerItems.isNotEmpty()) {
            add(Section.Header(canAdd = false))
            addAll(headerItems)
        }

        add(Section.SideBar(canAdd = true))
        addAll(sidebarItems)

        if (hiddenItems.isNotEmpty()) {
            add(Section.Hidden(canAdd = false))
            addAll(hiddenItems)
        }

        if (conflictedItems.isNotEmpty()) {
            add(Section.Local(canAdd = false))
            addAll(conflictedItems)
        }
    }
}

private suspend fun mapToUiFieldsDraggableListItem(
    relation: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): UiFieldsListItem? {
    val limitObjectTypes = if (relation.format == RelationFormat.OBJECT &&
        relation.relationFormatObjectTypes.isNotEmpty()
    ) {
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
    return Item.Draggable(
        id = relation.id,
        fieldKey = relation.key,
        fieldTitle = relation.getName(stringResourceProvider),
        format = relation.format,
        limitObjectTypes = limitObjectTypes,
        isEditableField = fieldParser.isFieldEditable(relation),
    )
}

private suspend fun mapToUiFieldsLocalListItem(
    relation: ObjectWrapper.Relation,
    stringResourceProvider: StringResourceProvider,
    storeOfObjectTypes: StoreOfObjectTypes,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): UiFieldsListItem? {
    val limitObjectTypes = if (relation.format == RelationFormat.OBJECT &&
        relation.relationFormatObjectTypes.isNotEmpty()
    ) {
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
    return Item.Local(
        id = relation.id,
        fieldKey = relation.key,
        fieldTitle = relation.getName(stringResourceProvider),
        format = relation.format,
        limitObjectTypes = limitObjectTypes,
        isEditableField = fieldParser.isFieldEditable(relation),
    )
}

