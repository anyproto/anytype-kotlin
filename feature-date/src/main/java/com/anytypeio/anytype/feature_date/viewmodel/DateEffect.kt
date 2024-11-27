package com.anytypeio.anytype.feature_date.viewmodel

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

sealed class DateEffect {
    data class OpenChat(val target: Id, val space: SpaceId) : DateEffect()
    data class NavigateToEditor(val id: Id, val space: SpaceId) : DateEffect()
    data class NavigateToSetOrCollection(val id: Id, val space: SpaceId) : DateEffect()
    data class NavigateToDateObject(val objectId: Id, val space: SpaceId) : DateEffect()
    data object TypeSelectionScreen : DateEffect()
    data object ExitToSpaceWidgets : DateEffect()
    sealed class SendToast : DateEffect() {
        data class Error(val message: String) : SendToast()
        data class ObjectArchived(val name: String) : SendToast()
        data class UnexpectedLayout(val layout: String) : SendToast()
    }
    data object OpenGlobalSearch : DateEffect()
    data object ExitToVault : DateEffect()
    data object Back : DateEffect()
}