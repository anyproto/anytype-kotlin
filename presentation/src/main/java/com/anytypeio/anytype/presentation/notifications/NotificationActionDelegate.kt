package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.GetSpaceMemberByIdentity
import com.anytypeio.anytype.domain.notifications.ReplyNotifications
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

interface NotificationActionDelegate {

    val dispatcher: SharedFlow<NotificationCommand>

    suspend fun proceedWithNotificationAction(action: NotificationAction)

    class Default @Inject constructor(
        private val getSpaceMemberByIdentity: GetSpaceMemberByIdentity,
        private val replyNotifications: ReplyNotifications,
        private val systemNotificationService: SystemNotificationService,
        private val spaceManager: SpaceManager,
        private val saveCurrentSpace: SaveCurrentSpace
    ) : NotificationActionDelegate {

        override val dispatcher: MutableSharedFlow<NotificationCommand> = MutableSharedFlow()

        override suspend fun proceedWithNotificationAction(action: NotificationAction) {
            Timber.d("Proceeding with notification action: $action")
            when(action) {
                is NotificationAction.Multiplayer.ViewSpaceJoinRequest -> {
                    proceedWithSpaceJoinRequest(action)
                }
                is NotificationAction.Multiplayer.ViewSpaceLeaveRequest -> {
                    proceedWithSpaceLeaveRequest(action)
                }
                is NotificationAction.Multiplayer.GoToSpace -> {
                    proceedWithGoToSpaceAction(action)
                }
            }
        }

        private suspend fun proceedWithSpaceJoinRequest(
            action: NotificationAction.Multiplayer.ViewSpaceJoinRequest
        ) {
            getSpaceMemberByIdentity.async(
                GetSpaceMemberByIdentity.Params(
                    space = action.space,
                    identity = action.identity
                )
            ).fold(
                onSuccess = { member ->
                    if (member != null && member.spaceId == action.space.id) {
                        if (action.notification.isNotEmpty()) {
                            replyNotifications.async(
                                params = listOf(action.notification)
                            ).fold(
                                onSuccess = {
                                    Timber.d("Replied notification: ${action.notification}")
                                },
                                onFailure = {
                                    Timber.e(it, "Error while replying notification")
                                }
                            )
                        }
                        dispatcher.emit(
                            NotificationCommand.ViewSpaceJoinRequest(
                                space = action.space,
                                member = member.id
                            )
                        )
                    } else {
                        Timber.w("Space member not found")
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while searching space member by identity")
                }
            )
            systemNotificationService.cancel(action.notification)
        }

        private suspend fun proceedWithSpaceLeaveRequest(
            action: NotificationAction.Multiplayer.ViewSpaceLeaveRequest
        ) {
            replyNotifications.async(
                params = listOf(action.notification)
            ).fold(
                onSuccess = {
                    Timber.d("Replied notification: ${action.notification}")
                },
                onFailure = {
                    Timber.e(it, "Error while replying notification")
                }
            )
            dispatcher.emit(NotificationCommand.ViewSpaceLeaveRequest(space = action.space,))
            systemNotificationService.cancel(action.notification)
        }

        private suspend fun proceedWithGoToSpaceAction(action: NotificationAction.Multiplayer.GoToSpace) {
            // TODO check permissions before navigating
            replyNotifications.async(
                params = listOf(action.notification)
            ).fold(
                onSuccess = {
                    Timber.d("Replied notification: ${action.notification}")
                },
                onFailure = {
                    Timber.e(it, "Error while replying notification")
                }
            )
            val result = spaceManager.set(space = action.space.id)
            if (result.isSuccess) {
                saveCurrentSpace
                    .async(SaveCurrentSpace.Params(space = action.space))
                    .fold(
                        onSuccess = {
                            Timber.d("Saved current space")
                        },
                        onFailure = {
                            Timber.e(it, "Error while saving current space")
                        }
                    )
                dispatcher.emit(NotificationCommand.GoToSpace(space = action.space,))
            } else {
                // TODO show error msg
            }
            systemNotificationService.cancel(action.notification)
        }
    }
}