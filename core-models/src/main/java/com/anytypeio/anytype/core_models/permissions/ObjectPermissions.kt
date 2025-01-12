package com.anytypeio.anytype.core_models.permissions

data class ObjectPermissions(
    var canChangeType: Boolean = false,
    var canDelete: Boolean = false,
    var canTemplateSetAsDefault: Boolean = false,
    var canArchive: Boolean = false,
    var canDuplicate: Boolean = false,
    var canUndoRedo: Boolean = false,
    var canMakeAsTemplate: Boolean = false,
    var canCreateWidget: Boolean = false,
    var canFavorite: Boolean = false,
    var canLinkItself: Boolean = false,
    var canLock: Boolean = false,
    var canChangeIcon: Boolean = false,
    var canChangeCover: Boolean = false,
    var canChangeLayout: Boolean = false,
    var canEditRelationValues: Boolean = false,
    var canEditRelationsList: Boolean = false,
    var canApplyTemplates: Boolean = false,
    var canShare: Boolean = false,
    var canEditBlocks: Boolean = false,
    var canEditMessages: Boolean = false,
    var canShowVersionHistory: Boolean = false,
    var canRestoreVersionHistory: Boolean = false,
    var canEditDetails: Boolean = false,
    var editBlocks: EditBlocksPermission
)
