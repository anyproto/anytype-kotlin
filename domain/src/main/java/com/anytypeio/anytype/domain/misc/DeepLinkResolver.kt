package com.anytypeio.anytype.domain.misc

interface DeepLinkResolver {

    fun resolve(deeplink: String) : Action

    sealed class Action {
        data object Unknown : Action()
        sealed class Import : Action() {
            data class Experience(val type: String, val source: String) : Action()
        }
        data class Invite(val link: String) : Action()
    }
}