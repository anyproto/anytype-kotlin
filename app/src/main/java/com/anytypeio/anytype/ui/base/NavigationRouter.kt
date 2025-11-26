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
                is AppNavigation.Command.OpenModalTemplateEdit -> navigation.openModalTemplateEdit(
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
                is AppNavigation.Command.OpenChat -> navigation.openChat(
                    target = command.target,
                    space = command.space,
                    popUpToVault = command.popUpToVault
                )
                is AppNavigation.Command.LaunchObjectSet -> navigation.launchObjectSet(
                    target = command.target,
                    space = command.space
                )
                is AppNavigation.Command.LaunchDocument -> navigation.launchDocument(
                    target = command.target,
                    space = command.space
                )
                is AppNavigation.Command.Exit -> navigation.exit(command.space)
                is AppNavigation.Command.ExitToDesktop -> navigation.exitToDesktop(command.space)
                is AppNavigation.Command.ExitToVault -> navigation.exitToVault()
                is AppNavigation.Command.ExitToSpaceHome -> navigation.exitToSpaceHome()
                is AppNavigation.Command.OpenGlobalSearch -> navigation.openGlobalSearch(
                    space = command.space
                )
                is AppNavigation.Command.OpenShareScreen -> navigation.openShareScreen(
                    space = command.space
                )
                is AppNavigation.Command.OpenUpdateAppScreen -> navigation.openUpdateAppScreen()
                is AppNavigation.Command.DeletedAccountScreen -> navigation.deletedAccountScreen(
                    command.deadline
                )
                is AppNavigation.Command.OpenTemplates -> navigation.openTemplatesModal(
                    typeId = command.typeId
                )
                is AppNavigation.Command.OpenDateObject -> navigation.openDateObject(
                    objectId = command.objectId,
                    space = command.space
                )
                is AppNavigation.Command.OpenTypeObject -> {
                    navigation.openObjectType(
                        objectId = command.target,
                        space = command.space
                    )
                }
                is AppNavigation.Command.OpenParticipant -> navigation.openParticipantObject(
                    objectId = command.objectId,
                    space = command.space
                )
                else -> Timber.d("Nav command ignored: $command")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while navigation")
        }
    }
}

fun Fragment.navigation() = (requireActivity() as AppNavigation.Provider).nav()