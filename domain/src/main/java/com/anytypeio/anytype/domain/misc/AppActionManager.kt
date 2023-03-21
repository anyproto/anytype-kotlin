package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Id

interface AppActionManager {

    fun setup(action: Action)

    sealed class Action {
        data class CreateNew(val type: Id, val name: String) : Action()
        object ClearAll: Action()
    }
}