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
                is AppNavigation.Command.OpenObject -> navigation.openDocument(
                    target = command.target,
                    space = command.space
                )
                is AppNavigation.Command.OpenModalTemplateSelect -> navigation.openModalTemplateSelect(
                    template = command.template,
                    templateTypeId = command.templateTypeId,
                    templateTypeKey = command.templateTypeKey,
                    space = command.space
                )
                is AppNavigation.Command.OpenSetOrCollection -> navigation.openObjectSet(
                    target = command.target,
                    space = command.space,
                    isPopUpToDashboard = command.isPopUpToDashboard
                )
                is AppNavigation.Command.LaunchObjectSet -> navigation.launchObjectSet(
                    target = command.target,
                    space = command.space
                )
                is AppNavigation.Command.LaunchDocument -> navigation.launchDocument(
                    target = command.target,
                    space = command.space
                )
                is AppNavigation.Command.OpenUserSettingsScreen -> navigation.openUserSettingsScreen()
                is AppNavigation.Command.Exit -> navigation.exit()
                is AppNavigation.Command.ExitToDesktop -> navigation.exitToDesktop()
                is AppNavigation.Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
                is AppNavigation.Command.OpenPageSearch -> navigation.openPageSearch()
                is AppNavigation.Command.OpenUpdateAppScreen -> navigation.openUpdateAppScreen()
                is AppNavigation.Command.DeletedAccountScreen -> navigation.deletedAccountScreen(
                    command.deadline
                )
                is AppNavigation.Command.OpenTemplates -> navigation.openTemplatesModal(
                    typeId = command.typeId
                )
                is AppNavigation.Command.OpenLibrary -> navigation.openLibrary(command.space)
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