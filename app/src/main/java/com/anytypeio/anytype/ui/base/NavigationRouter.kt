package com.anytypeio.anytype.ui.base

import androidx.fragment.app.Fragment
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import timber.log.Timber

class NavigationRouter(
    private val navigation: AppNavigation
) {
    fun navigate(command: AppNavigation.Command) {
        Timber.d("Navigate to $command")
        try {
            when (command) {
                is AppNavigation.Command.ExitFromMigrationScreen -> navigation.exitFromMigrationScreen()
                is AppNavigation.Command.OpenSettings -> navigation.openSpaceSettings()
                is AppNavigation.Command.OpenObject -> navigation.openDocument(command.id)
                is AppNavigation.Command.OpenModalTemplateSelect -> navigation.openModalTemplateSelect(
                    template = command.template,
                    templateTypeId = command.templateTypeId,
                    templateTypeKey = command.templateTypeKey
                )
                is AppNavigation.Command.OpenSetOrCollection -> navigation.openObjectSet(
                    command.target,
                    command.isPopUpToDashboard
                )
                is AppNavigation.Command.LaunchObjectSet -> navigation.launchObjectSet(command.target)
                is AppNavigation.Command.LaunchDocument -> navigation.launchDocument(command.id)
                is AppNavigation.Command.LaunchObjectFromSplash -> navigation.launchObjectFromSplash(
                    command.target
                )

                is AppNavigation.Command.LaunchObjectSetFromSplash -> navigation.launchObjectSetFromSplash(
                    command.target
                )

                is AppNavigation.Command.OpenUserSettingsScreen -> navigation.openUserSettingsScreen()

                is AppNavigation.Command.Exit -> navigation.exit()
                is AppNavigation.Command.ExitToDesktop -> navigation.exitToDesktop()
                is AppNavigation.Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
                is AppNavigation.Command.ExitToDesktopAndOpenPage -> navigation.exitToDesktopAndOpenPage(
                    command.pageId
                )

                is AppNavigation.Command.OpenPageSearch -> navigation.openPageSearch()
                is AppNavigation.Command.OpenUpdateAppScreen -> navigation.openUpdateAppScreen()
                is AppNavigation.Command.DeletedAccountScreen -> navigation.deletedAccountScreen(
                    command.deadline
                )

                is AppNavigation.Command.OpenTemplates -> navigation.openTemplatesModal(
                    typeId = command.typeId
                )

                is AppNavigation.Command.OpenLibrary -> navigation.openLibrary()
                is AppNavigation.Command.MigrationErrorScreen -> navigation.migrationErrorScreen()
                is AppNavigation.Command.OpenRemoteFilesManageScreen -> navigation.openRemoteFilesManageScreen(
                    command.subscription
                )

                else -> Timber.d("Nav command ignored: $command")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while navigation")
        }
    }
}

fun Fragment.navigation() = (requireActivity() as AppNavigation.Provider).nav()