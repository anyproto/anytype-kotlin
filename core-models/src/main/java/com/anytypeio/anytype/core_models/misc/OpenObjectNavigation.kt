package com.anytypeio.anytype.core_models.misc

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType

sealed class OpenObjectNavigation {
    data class OpenEditor(val target: Id, val space: Id, val effect: SideEffect = SideEffect.None) :
        OpenObjectNavigation()

    data class OpenDataView(
        val target: Id,
        val space: Id,
        val effect: SideEffect = SideEffect.None
    ) : OpenObjectNavigation()

    data class UnexpectedLayoutError(val layout: ObjectType.Layout?) : OpenObjectNavigation()
    data object NonValidObject : OpenObjectNavigation()
    data class OpenChat(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenDateObject(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenParticipant(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenType(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenBookmarkUrl(val url: String) :
        OpenObjectNavigation() // For opening bookmark URLs

    sealed class SideEffect {
        data object None : SideEffect()
        data class AttachToChat(val chat: Id, val space: Id) : SideEffect()
    }
}