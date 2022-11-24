package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView

/**
 * The method allows you to get object type views for using in the editor and set
 * The main filtering goes by SmartBlockType.PAGE
 * Resulting list of object types, sorted by [ObjectTypeViewComparator]
 * @param isWithSet This parameter determines whether to add ObjectType SET to the resulting list of object types
 * @param isWithBookmark This parameter determines whether to add ObjectType BOOKMARK to the resulting list of object types
 * @param excludeTypes List of object type id's that should not be included in the resulting list
 * @param selectedTypes List of object type id's that have selected status true, see[ObjectTypeView.isSelected]
 *
 */
fun List<ObjectWrapper.Type>.getObjectTypeViewsForSBPage(
    isWithSet: Boolean,
    isWithBookmark: Boolean,
    excludeTypes: List<String>,
    selectedTypes: List<String>

): List<ObjectTypeView> {
    val result = mutableListOf<ObjectTypeView>()
    forEach { obj ->
        if (obj.isArchived == true || obj.isDeleted == true) {
            return@forEach
        }
        if (obj.id == SET) {
            if (isWithSet) {
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
        if (obj.smartBlockTypes.contains(SmartBlockType.PAGE)) {
            val objTypeView = obj.toObjectTypeView(selectedTypes)
            result.add(objTypeView)
            return@forEach
        }
    }
    return result.sortedWith(ObjectTypeViewComparator())
}