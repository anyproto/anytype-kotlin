package com.agileburo.anytype.feature_desktop.navigation

interface DesktopNavigation {
    fun openDocument(id: String)
    fun openProfile()

    sealed class Command {
        data class OpenDocument(val id: String) : Command()
        object OpenProfile : Command()
    }
}