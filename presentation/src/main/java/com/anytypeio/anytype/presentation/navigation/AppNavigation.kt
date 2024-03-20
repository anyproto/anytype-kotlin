package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.presentation.widgets.collection.Subscription

interface AppNavigation {

    fun exitFromMigrationScreen()

    fun openSpaceSettings()

    fun openObjectSet(
        target: Id,
        space: Id,
        isPopUpToDashboard: Boolean = false
    )
    @Deprecated("Provide space id")
    fun openDocument(id: String)
    fun openModalTemplateSelect(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key
    )
    fun openModalTemplateEdit(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key
    )

    fun launchDocument(id: String)
    fun launchCollections(subscription: Subscription)
    fun launchObjectFromSplash(id: Id)
    fun launchObjectSetFromSplash(id: Id)
    fun launchObjectSet(id: Id)

    fun openKeychainScreen()
    fun openUserSettingsScreen()

    fun exit()
    fun exitToDesktop()
    fun openDebugSettings()
    fun openPageSearch()
    fun exitToDesktopAndOpenPage(pageId: String)
    fun openUpdateAppScreen()
    fun openRemoteFilesManageScreen(subscription: Id)

    fun deletedAccountScreen(deadline: Long)

    fun openLibrary()

    fun logout()

    fun migrationErrorScreen()

    fun openTemplatesModal(typeId: Id)

    sealed class Command {

        object Exit : Command()
        object ExitToDesktop : Command()

        object ExitFromMigrationScreen : Command()

        data class OpenObject(val id: String) : Command()

        data class LaunchDocument(val id: String) : Command()
        data class LaunchObjectFromSplash(val target: Id) : Command()
        data class LaunchObjectSetFromSplash(val target: Id) : Command()
        data class OpenModalTemplateSelect(
            val template: Id,
            val templateTypeId: Id,
            val templateTypeKey: Key
        ) : Command()

        object OpenSettings : Command()
        object OpenUserSettingsScreen : Command()
        object MigrationErrorScreen: Command()
        object OpenDebugSettingsScreen : Command()

        data class ExitToDesktopAndOpenPage(val pageId: String) : Command()
        object OpenPageSearch : Command()

        data class OpenSetOrCollection(
            val target: Id,
            val space: Id,
            val isPopUpToDashboard: Boolean = false
        ) : Command()

        data class LaunchObjectSet(val target: Id) : Command()

        object OpenUpdateAppScreen : Command()

        data class DeletedAccountScreen(val deadline: Long) : Command()

        data class OpenTemplates(val typeId: Id) : Command()

        object OpenLibrary: Command()

        data class OpenRemoteFilesManageScreen(val subscription: Id) : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
