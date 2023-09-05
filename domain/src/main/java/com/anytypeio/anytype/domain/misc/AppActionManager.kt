package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Key

interface AppActionManager {

    fun setup(action: Action)

    sealed class Action {
        data class CreateNew(val type: Key, val name: String) : Action()
        object ClearAll: Action()
    }
}