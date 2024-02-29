package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.COLLECTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.page.CreateObject
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
    selectedTypes: List<String> = emptyList(),
    useCustomComparator: Boolean = true
): List<ObjectTypeView> {
    val result = mutableListOf<ObjectTypeView>()
    forEach { obj ->
        if (obj.isArchived == true || obj.isDeleted == true) {
            return@forEach
        }
        if (obj.uniqueKey == COLLECTION || obj.uniqueKey == SET) {
            if (isWithCollection) {
                val objTypeView = obj.toObjectTypeView(selectedTypes)
                result.add(objTypeView)
            }
            return@forEach
        }
        if (obj.uniqueKey == BOOKMARK) {
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
    return if (useCustomComparator)
        result.sortedWith(ObjectTypeViewComparator())
    else
        result
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

/**
 * This method is used to get the parameters for creating an object from + button(single click).
 *
 * @return [CreateObject.Param] with the necessary parameters for creating an object.
 */
fun Key?.getCreateObjectParams(defaultTemplate: Id?): CreateObject.Param {
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
        type = key?.let { TypeKey(it) },
        internalFlags = flags,
        template = defaultTemplate
    )
}

/**
 * Determines whether an object of this type is allowed to be created.
 *
 * This function checks if the current [ObjectWrapper.Type] is eligible for object creation
 * based on certain restrictions and layout considerations.
 * An object type is considered not allowed to be created if either of the following conditions is met:
 * - The type has a restriction that explicitly prohibits the creation of an object of this type ([ObjectRestriction.CREATE_OBJECT_OF_THIS_TYPE]).
 * - The type is designated as a PARTICIPANT through recommended layout ([ObjectType.Layout.PARTICIPANT])
 *
 * @receiver The [ObjectWrapper.Type] for which the check is being performed. If the receiver is `null`, the function immediately returns `false`.
 * @return `true` if an object of this type is allowed to be created
 */
fun ObjectWrapper.Type?.isAllowedToCreateObject(): Boolean {
    return this != null &&
            !restrictions.contains(ObjectRestriction.CREATE_OBJECT_OF_THIS_TYPE) &&
            recommendedLayout != ObjectType.Layout.PARTICIPANT
}
