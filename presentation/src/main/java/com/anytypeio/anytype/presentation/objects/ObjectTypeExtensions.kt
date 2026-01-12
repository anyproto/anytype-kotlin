package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.COLLECTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts.createObjectLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.editorLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView
import com.anytypeio.anytype.presentation.sets.state.ObjectState

/**
 * The method allows you to get object type views for using in the editor and set
 * Resulting list of object types
 * @param includeListTypes This parameter determines whether to add list types (SET and COLLECTION) to the resulting list of object types
 * @param includeBookmarkType This parameter determines whether to add ObjectType BOOKMARK to the resulting list of object types
 * @param excludedTypeIds List of object type id's that should not be included in the resulting list
 * @param selectedTypeIds List of object type id's that have selected status true, see[ObjectTypeView.isSelected]
 *
 */
fun List<ObjectWrapper.Type>.toObjectTypeViews(
    includeListTypes: Boolean = false,
    includeBookmarkType: Boolean = false,
    excludedTypeIds: List<String> = emptyList(),
    selectedTypeIds: List<String> = emptyList()
): List<ObjectTypeView> {
    val objectTypeViews = mutableListOf<ObjectTypeView>()
    forEach { objectType ->
        if (objectType.isArchived == true || objectType.isDeleted == true) {
            return@forEach
        }
        if (objectType.uniqueKey == COLLECTION || objectType.uniqueKey == SET) {
            if (includeListTypes) {
                val objTypeView = objectType.toObjectTypeView(selectedTypeIds)
                objectTypeViews.add(objTypeView)
            }
            return@forEach
        }
        if (objectType.uniqueKey == BOOKMARK) {
            if (includeBookmarkType) {
                val objTypeView = objectType.toObjectTypeView(selectedTypeIds)
                objectTypeViews.add(objTypeView)
            }
            return@forEach
        }
        if (excludedTypeIds.contains(objectType.id)) {
            return@forEach
        }
        val objTypeView = objectType.toObjectTypeView(selectedTypeIds)
        objectTypeViews.add(objTypeView)
        return@forEach
    }
    return objectTypeViews
}

/**
 *
 * This method is used to understand if objects of this type can use templates.
 *
 * @return `true` if templates are allowed for this type of object, `false` otherwise.
 */
fun ObjectWrapper.Type.isTemplatesAllowed(): Boolean {
    val showTemplates = !ObjectTypeIds.getTypesWithoutTemplates().contains(this.uniqueKey)
    val allowedObject = editorLayouts.contains(recommendedLayout)
    return showTemplates && allowedObject
}

/**
 * Determines if objects of the given type can be created based on the object type's layout.
 *
 * @param objectType The object type to check for creation eligibility
 * @return true if objects of this type can be created, false otherwise
 */
fun canCreateObjectOfType(objectType: ObjectWrapper.Type?): Boolean {
    if (objectType?.uniqueKey == ObjectTypeIds.TEMPLATE) {
        return false
    }
    return createObjectLayouts.contains(objectType?.recommendedLayout)
}

/**
 * Determines if objects of the given type can be created based on the object type's layout,
 * taking into account the space context.
 *
 * @param objectType The object type to check for creation eligibility
 * @param spaceUxType The UX type of the current space
 * @return true if objects of this type can be created, false otherwise
 */
fun canCreateObjectOfType(objectType: ObjectWrapper.Type?, spaceUxType: SpaceUxType?): Boolean {
    if (objectType?.uniqueKey == ObjectTypeIds.TEMPLATE) {
        return false
    }
    return getCreateObjectLayouts(spaceUxType).contains(objectType?.recommendedLayout)
}

fun ObjectState.DataView.isCreateObjectAllowed(objectType: ObjectWrapper.Type? = null): Boolean {
    val dataViewRestrictions = dataViewRestrictions.firstOrNull()?.restrictions
    if (dataViewRestrictions?.contains(DataViewRestriction.CREATE_OBJECT) == true) {
        return false
    }

    if (this is ObjectState.DataView.Collection) {
        return true
    }

    return canCreateObjectOfType(objectType)
}

/**
 * This method is used to get the parameters for creating an object from + button(single click).
 *
 * @return [CreateObject.Param] with the necessary parameters for creating an object.
 */
fun Key?.getCreateObjectParams(
    space: SpaceId,
    defaultTemplate: Id?
): CreateObject.Param {
    val key = this
    val flags = buildList {
        add(InternalFlags.ShouldEmptyDelete)

        if (key != SET && key != COLLECTION) {
            add(InternalFlags.ShouldSelectTemplate)
        }

        if (key == null) {
            add(InternalFlags.ShouldSelectType)
        }
    }

    return CreateObject.Param(
        space = space,
        type = key?.let { TypeKey(it) },
        internalFlags = flags,
        template = defaultTemplate
    )
}
