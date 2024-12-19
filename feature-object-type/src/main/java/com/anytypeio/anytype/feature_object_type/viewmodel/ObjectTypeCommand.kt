package com.anytypeio.anytype.feature_object_type.viewmodel


sealed class ObjectTypeCommand {

    sealed class SendToast : ObjectTypeCommand() {
        data class Error(val message: String) : SendToast()
        data class UnexpectedLayout(val layout: String) : SendToast()
    }

    data object Back : ObjectTypeCommand()
}