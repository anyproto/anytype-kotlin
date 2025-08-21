package com.anytypeio.anytype.feature_date.viewmodel

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

sealed class DateObjectCommand {
    data class OpenChat(val target: Id, val space: SpaceId) : DateObjectCommand()
    data class OpenType(val target: Id, val space: SpaceId) : DateObjectCommand()
    data class NavigateToEditor(val id: Id, val space: SpaceId) : DateObjectCommand()
    data class NavigateToSetOrCollection(val id: Id, val space: SpaceId) : DateObjectCommand()
    data class NavigateToDateObject(val objectId: Id, val space: SpaceId) : DateObjectCommand()
    data class NavigateToParticipant(val objectId: Id, val space: SpaceId) : DateObjectCommand()
    data class OpenUrl(val url: String) : DateObjectCommand()
    data object TypeSelectionScreen : DateObjectCommand()
    data object ExitToHomeOrChat : DateObjectCommand()
    sealed class SendToast : DateObjectCommand() {
        data class Error(val message: String) : SendToast()
        data class UnexpectedLayout(val layout: String) : SendToast()
    }
    data object OpenGlobalSearch : DateObjectCommand()
    data object ExitToVault : DateObjectCommand()
    data object Back : DateObjectCommand()
}