package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.primitives.TypeKey

interface AppActionManager {

    fun setup(action: Action)

    fun setup(actions: List<Action.CreateNew>)

    sealed class Action {
        data class CreateNew(val type: TypeKey, val name: String) : Action()
        object ClearAll: Action()
    }
}