package com.anytypeio.anytype.feature_object_type.viewmodel

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key


sealed class ObjectTypeCommand {

    sealed class SendToast : ObjectTypeCommand() {
        data class Error(val message: String) : SendToast()
        data class UnexpectedLayout(val layout: String) : SendToast()
    }

    data object Back : ObjectTypeCommand()

    data class OpenTemplate(
        val templateId: Id,
        val typeId: Id,
        val typeKey: Key,
        val spaceId: Id
    ): ObjectTypeCommand()

    data object OpenEmojiPicker : ObjectTypeCommand()

    data object OpenFieldsScreen : ObjectTypeCommand()

    data object OpenEditFieldScreen : ObjectTypeCommand()
}