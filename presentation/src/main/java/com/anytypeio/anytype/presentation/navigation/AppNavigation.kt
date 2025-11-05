package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.widgets.collection.Subscription

interface AppNavigation {

    fun openSpaceSettings()

    fun openObjectSet(
        target: Id,
        space: Id,
        view: Id? = null,
        isPopUpToDashboard: Boolean = false
    )
    fun openChat(target: Id, space: Id, popUpToVault: Boolean = true)
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
    fun openDateObject(
        objectId: Id,
        space: Id
    )

    fun openParticipantObject(
        objectId: Id,
        space: Id
    )

    fun openObjectType(
        objectId: Id,
        space: Id
    )

    fun openCurrentObjectTypeFields(
        objectId: Id,
        space: Id
    )

    fun launchDocument(target: String, space: Id)
    fun launchCollections(subscription: Subscription, space: Id)
    fun launchObjectSet(target: Id, space: Id)

    fun exit(space: Id)
    fun exitToDesktop(space: Id)
    fun exitToVault()
    fun exitToSpaceHome()
    fun openGlobalSearch(space: Id)
    fun openShareScreen(space: SpaceId)
    fun openUpdateAppScreen()
    fun openRemoteFilesManageScreen(subscription: Id, space: Id)

    fun deletedAccountScreen(deadline: Long)

    fun logout()

    fun openTemplatesModal(typeId: Id)

    fun openAllContent(space: Id)
    fun openRelationCreationScreen(id: Id, name: String, space: Id)

    fun openCreateObjectTypeScreen(spaceId: Id)

    sealed class Command {

        @Deprecated("To be deleted. Too generic and confusing.")
        data class Exit(val space: Id) : Command()
        data class ExitToDesktop(val space: Id) : Command()
        data object ExitToVault : Command()
        data object ExitToSpaceHome : Command()
        data class OpenObject(val target: Id, val space: Id) : Command()
        data class OpenChat(val target: Id, val space: Id, val popUpToVault: Boolean) : Command()
        data class LaunchDocument(val target: Id, val space: Id) : Command()
        data class OpenModalTemplateSelect(
            val template: Id,
            val templateTypeId: Id,
            val templateTypeKey: Key,
            val space: Id
        ) : Command()

        data class OpenModalTemplateEdit(
            val template: Id,
            val templateTypeId: Id,
            val templateTypeKey: Key,
            val space: Id
        ) : Command()

        data object OpenSettings : Command()

        data class OpenShareScreen(
            val space: SpaceId
        ) : Command()

        data class OpenGlobalSearch(
            val space: Id
        ) : Command()

        data class OpenSetOrCollection(
            val target: Id,
            val space: Id,
            val isPopUpToDashboard: Boolean = false
        ) : Command()

        data class OpenDateObject(
            val objectId: Id,
            val space: Id
        ) : Command()


        data class OpenTypeObject(
            val target: Id,
            val space: Id
        ) : Command()

        data class OpenParticipant(
            val objectId: Id,
            val space: Id
        ): Command()

        data class LaunchObjectSet(val target: Id, val space: Id) : Command()

        data object OpenUpdateAppScreen : Command()

        data class DeletedAccountScreen(val deadline: Long) : Command()

        data class OpenTemplates(val typeId: Id) : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
