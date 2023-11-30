package com.anytypeio.anytype.domain.misc

interface DeepLinkResolver {

    fun resolve(deeplink: String) : Action

    sealed class Action {
        object Unknown : Action()
        sealed class Import : Action() {
            object Experience : Action()
        }
    }
}