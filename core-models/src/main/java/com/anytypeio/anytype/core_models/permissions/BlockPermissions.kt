package com.anytypeio.anytype.core_models.permissions

sealed class BlocksReadonlyReason {
    object Locked : BlocksReadonlyReason()
    object Archived : BlocksReadonlyReason()
    object SpaceIsReadonly : BlocksReadonlyReason()
    object Restrictions : BlocksReadonlyReason()
}

sealed class EditBlocksPermission {
    object Edit : EditBlocksPermission()
    data class ReadOnly(val reason: BlocksReadonlyReason) : EditBlocksPermission()

    val canEdit: Boolean
        get() = when (this) {
            is Edit -> true
            is ReadOnly -> false
        }
}