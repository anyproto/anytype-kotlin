package com.anytypeio.anytype.feature_object_type.viewmodel

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId


sealed class ObjectTypeCommand {

    sealed class SendToast : ObjectTypeCommand() {
        data class Error(val message: String) : SendToast()
        data class UnexpectedLayout(val layout: String) : SendToast()
    }

    data object Back : ObjectTypeCommand()

    data class OpenTemplate(
        val objectId: Id,
        val spaceId: SpaceId
    ): ObjectTypeCommand()

    data object OpenEmojiPicker : ObjectTypeCommand()
}