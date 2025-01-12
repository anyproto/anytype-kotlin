package com.anytypeio.anytype.core_models.permissions

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

/**
 * Represents a set of user permissions for a given object.
 *
 * @property canArchive Indicates whether this object can be archived.
 * @property canDelete Indicates whether this object can be permanently deleted.
 * @property canChangeType Indicates whether the object's type can be changed.
 * @property canTemplateSetAsDefault Indicates whether this template object can be set as the default for a type.
 * @property canApplyTemplates Indicates whether templates can be applied to this object.
 * @property canMakeAsTemplate Indicates whether this object can be turned into a template.
 * @property canDuplicate Indicates whether this object can be duplicated.
 * @property canUndoRedo Indicates whether undo and redo operations are allowed for this object.
 * @property canFavorite Indicates whether this object can be marked as a favorite.
 * @property canLinkItself Indicates whether this object can be linked to itself (if applicable).
 * @property canLock Indicates whether this object can be locked.
 * @property canChangeIcon Indicates whether the object icon can be changed.
 * @property canChangeCover Indicates whether the object cover can be changed.
 * @property canChangeLayout Indicates whether the layout of the object can be changed.
 * @property canEditRelationValues Indicates whether relation values on this object can be edited.
 * @property canEditRelationsList Indicates whether the list of relations for this object can be edited.
 * @property canEditBlocks Indicates whether blocks in this object can be edited.
 * @property canEditDetails Indicates whether general details on this object can be edited.
 * @property editBlocks Specifies the permission level regarding block editing (e.g., read-only vs. editable).
 * @property canCreateObjectThisType Indicates whether object with this type can be created.
 */
data class ObjectPermissions(
    val canArchive: Boolean = false,
    val canDelete: Boolean = false,
    val canChangeType: Boolean = false,
    val canTemplateSetAsDefault: Boolean = false,
    val canApplyTemplates: Boolean = false,
    val canMakeAsTemplate: Boolean = false,
    val canDuplicate: Boolean = false,
    val canUndoRedo: Boolean = false,
    val canFavorite: Boolean = false,
    val canLinkItself: Boolean = false,
    val canLock: Boolean = false,
    val canChangeIcon: Boolean = false,
    val canChangeCover: Boolean = false,
    val canChangeLayout: Boolean = false,
    val canEditRelationValues: Boolean = false,
    val canEditRelationsList: Boolean = false,
    val canEditBlocks: Boolean = false,
    val canEditDetails: Boolean = false,
    val editBlocks: EditBlocksPermission,
    val canCreateObjectThisType: Boolean = false
)

/**
 * Converts this [ObjectView] instance into an [ObjectPermissions] by inspecting
 * its current state (e.g., archived, locked) and the user's participant edit rights.
 *
 * @param participantCanEdit Flag indicating whether the participant can perform edits.
 * @return An [ObjectPermissions] instance that encapsulates the allowed actions.
 */
fun ObjectView.toObjectPermissions(
    participantCanEdit: Boolean
): ObjectPermissions {
    val rootBlock = blocks.find { it.id == root }
    val isLocked = rootBlock?.fields?.isLocked == true
    val isArchived = details[root]?.getSingleValue<Boolean>(Relations.IS_ARCHIVED) == true

    val objTypeId = details[root]?.getSingleValue<String>(Relations.TYPE)
    val typeUniqueKey = if (objTypeId != null) {
        details[objTypeId]?.getSingleValue<Id>(Relations.TYPE_UNIQUE_KEY)
    } else {
        null
    }
    val isTemplateObject = (typeUniqueKey == ObjectTypeIds.TEMPLATE)

    val currentLayout = when (val value = details[root]?.getOrDefault(Relations.LAYOUT, null)) {
        is Double -> ObjectType.Layout.entries.singleOrNull { layout ->
            layout.code == value.toInt()
        }

        else -> null
    }

    val canEditRelations = !isLocked && !isArchived && participantCanEdit
    val canEdit = canEditRelations && !SupportedLayouts.isFileLayout(currentLayout)
    val canApplyUneditableActions = !isArchived && participantCanEdit
    val isProfileOwnerIdentity =
        details[root]?.getSingleValue<String>(Relations.PROFILE_OWNER_IDENTITY)
    val canEditDetails = !objectRestrictions.contains(ObjectRestriction.DETAILS)

    val editBlocksPermission = when {
        isLocked -> EditBlocksPermission.ReadOnly
        isArchived -> EditBlocksPermission.ReadOnly
        !participantCanEdit -> EditBlocksPermission.ReadOnly
        objectRestrictions.contains(ObjectRestriction.BLOCKS) -> EditBlocksPermission.ReadOnly

        else -> EditBlocksPermission.Edit
    }

    return ObjectPermissions(
        canArchive = participantCanEdit && !objectRestrictions.contains(ObjectRestriction.DELETE) && !isArchived,
        canDelete = isArchived && participantCanEdit && !objectRestrictions.contains(
            ObjectRestriction.DELETE
        ),
        canChangeType = canEdit &&
                !isTemplateObject &&
                !objectRestrictions.contains(ObjectRestriction.TYPE_CHANGE),
        canTemplateSetAsDefault = canEdit && isTemplateObject,
        canApplyTemplates = canEdit && !isTemplateObject,
        canMakeAsTemplate = templatesAllowedLayouts.contains(currentLayout) &&
                !isTemplateObject &&
                isProfileOwnerIdentity.isNullOrEmpty() &&
                !objectRestrictions.contains(ObjectRestriction.TEMPLATE) &&
                canApplyUneditableActions,
        canDuplicate = canApplyUneditableActions && !objectRestrictions.contains(ObjectRestriction.DUPLICATE),
        canUndoRedo = canEdit && undoRedoLayouts.contains(currentLayout),
        canFavorite = canApplyUneditableActions && !isTemplateObject,
        canLinkItself = canApplyUneditableActions && !isTemplateObject,
        canLock = lockLayouts.contains(currentLayout) &&
                canApplyUneditableActions &&
                !isTemplateObject,
        canChangeIcon = canEditDetails && layoutsWithIcon.contains(currentLayout) && canEdit,
        canChangeCover = canEditDetails && layoutsWithCover.contains(currentLayout) && canEdit,
        canChangeLayout = canEditDetails &&
                possibleToChangeLayoutLayouts.contains(currentLayout) &&
                canEdit,
        canEditRelationValues = canEditRelations && canEditDetails,
        canEditRelationsList = canEditRelations &&
                canEditDetails &&
                !objectRestrictions.contains(ObjectRestriction.RELATIONS),
        canEditBlocks = (editBlocksPermission == EditBlocksPermission.Edit),
        canEditDetails = canEditDetails && canEdit,
        editBlocks = editBlocksPermission,
        canCreateObjectThisType = !objectRestrictions.contains(ObjectRestriction.CREATE_OBJECT_OF_THIS_TYPE) && canApplyUneditableActions
    )
}

val templatesAllowedLayouts = listOf(
    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE,
    ObjectType.Layout.TODO
)

val undoRedoLayouts = listOf(
    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE,
    ObjectType.Layout.TODO,
    ObjectType.Layout.NOTE,
    ObjectType.Layout.BOOKMARK
)

val lockLayouts = listOf(
    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE,
    ObjectType.Layout.TODO,
    ObjectType.Layout.NOTE,
    ObjectType.Layout.BOOKMARK
)

val layoutsWithIcon = listOf(
    ObjectType.Layout.FILE,
    ObjectType.Layout.IMAGE,
    ObjectType.Layout.VIDEO,
    ObjectType.Layout.AUDIO,
    ObjectType.Layout.PDF,

    ObjectType.Layout.SET,
    ObjectType.Layout.COLLECTION,

    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE
)

val layoutsWithCover = layoutsWithIcon + listOf(
    ObjectType.Layout.TODO,
    ObjectType.Layout.BOOKMARK
)

val possibleToChangeLayoutLayouts = listOf(
    ObjectType.Layout.BASIC,
    ObjectType.Layout.PROFILE,
    ObjectType.Layout.TODO,
    ObjectType.Layout.NOTE
)
