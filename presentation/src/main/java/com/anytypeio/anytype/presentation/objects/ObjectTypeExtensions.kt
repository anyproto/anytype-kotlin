package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.COLLECTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView
import com.anytypeio.anytype.presentation.objects.SupportedLayouts.editorLayouts
import com.anytypeio.anytype.presentation.objects.SupportedLayouts.fileLayouts
import com.anytypeio.anytype.presentation.objects.SupportedLayouts.systemLayouts
import com.anytypeio.anytype.presentation.sets.state.ObjectState

/**
 * The method allows you to get object type views for using in the editor and set
 * The main filtering goes by SmartBlockType.PAGE
 * Resulting list of object types, sorted by [ObjectTypeViewComparator]
 * @param isWithCollection This parameter determines whether to add ObjectType COLLECTION to the resulting list of object types
 * @param isWithBookmark This parameter determines whether to add ObjectType BOOKMARK to the resulting list of object types
 * @param excludeTypes List of object type id's that should not be included in the resulting list
 * @param selectedTypes List of object type id's that have selected status true, see[ObjectTypeView.isSelected]
 *
 */
fun List<ObjectWrapper.Type>.getObjectTypeViewsForSBPage(
    isWithCollection: Boolean = false,
    isWithBookmark: Boolean = false,
    excludeTypes: List<String> = emptyList(),
    selectedTypes: List<String> = emptyList()
): List<ObjectTypeView> {
    val result = mutableListOf<ObjectTypeView>()
    forEach { obj ->
        if (obj.isArchived == true || obj.isDeleted == true) {
            return@forEach
        }
        if (obj.id == COLLECTION || obj.id == SET) {
            if (isWithCollection) {
                val objTypeView = obj.toObjectTypeView(selectedTypes)
                result.add(objTypeView)
            }
            return@forEach
        }
        if (obj.id == BOOKMARK) {
            if (isWithBookmark) {
                val objTypeView = obj.toObjectTypeView(selectedTypes)
                result.add(objTypeView)
            }
            return@forEach
        }
        if (excludeTypes.contains(obj.id)) {
            return@forEach
        }
        val objTypeView = obj.toObjectTypeView(selectedTypes)
        result.add(objTypeView)
        return@forEach
    }
    return result.sortedWith(ObjectTypeViewComparator())
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

fun ObjectState.DataView.isCreateObjectAllowed(objectType: ObjectWrapper.Type? = null): Boolean {
    val dataViewRestrictions = dataViewRestrictions.firstOrNull()?.restrictions
    if (dataViewRestrictions?.contains(DataViewRestriction.CREATE_OBJECT) == true) {
        return false
    }

    if (this is ObjectState.DataView.Collection) {
        return true
    }

    val skipLayouts = fileLayouts + systemLayouts
    return !skipLayouts.contains(objectType?.recommendedLayout)
}