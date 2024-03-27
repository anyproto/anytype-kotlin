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
    fun openDocument(target: Id, space: Id)
    fun openModalTemplateSelect(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key,
        space: Id
    )
    fun openModalTemplateEdit(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key,
        space: Id
    )

    fun launchDocument(target: String, space: Id)
    fun launchCollections(subscription: Subscription, space: Id)
    fun launchObjectSet(target: Id, space: Id)

    fun openKeychainScreen()
    fun openUserSettingsScreen()

    fun exit()
    fun exitToDesktop()
    fun openDebugSettings()
    fun openPageSearch()
    fun openUpdateAppScreen()
    fun openRemoteFilesManageScreen(subscription: Id)

    fun deletedAccountScreen(deadline: Long)

    fun openLibrary(space: Id)

    fun logout()

    fun migrationErrorScreen()

    fun openTemplatesModal(typeId: Id)

    sealed class Command {

        object Exit : Command()
        object ExitToDesktop : Command()

        object ExitFromMigrationScreen : Command()

        data class OpenObject(val target: Id, val space: Id) : Command()
        data class LaunchDocument(val target: Id, val space: Id) : Command()
        data class OpenModalTemplateSelect(
            val template: Id,
            val templateTypeId: Id,
            val templateTypeKey: Key,
            val space: Id
        ) : Command()

        object OpenSettings : Command()
        object OpenUserSettingsScreen : Command()
        object MigrationErrorScreen: Command()
        object OpenDebugSettingsScreen : Command()

        object OpenPageSearch : Command()

        data class OpenSetOrCollection(
            val target: Id,
            val space: Id,
            val isPopUpToDashboard: Boolean = false
        ) : Command()

        data class LaunchObjectSet(val target: Id, val space: Id) : Command()

        object OpenUpdateAppScreen : Command()

        data class DeletedAccountScreen(val deadline: Long) : Command()

        data class OpenTemplates(val typeId: Id) : Command()

        data class OpenLibrary(val space: Id): Command()

        data class OpenRemoteFilesManageScreen(val subscription: Id) : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
