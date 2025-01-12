package com.anytypeio.anytype.core_models.permissions

sealed class BlocksReadonlyReason {
    object Locked : BlocksReadonlyReason()
    object Archived : BlocksReadonlyReason()
    object SpaceIsReadonly : BlocksReadonlyReason()
    object Restrictions : BlocksReadonlyReason()
}

sealed class EditBlocksPermission {
    data object Edit : EditBlocksPermission()
    data object ReadOnly: EditBlocksPermission()
}