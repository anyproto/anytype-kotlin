package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.GetSpaceMemberByIdentity
import com.anytypeio.anytype.domain.notifications.ReplyNotifications
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
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
        private val systemNotificationService: SystemNotificationService
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
            }
        }

        private suspend fun proceedWithSpaceJoinRequest(action: NotificationAction.Multiplayer.ViewSpaceJoinRequest) {
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

        private suspend fun proceedWithSpaceLeaveRequest(action: NotificationAction.Multiplayer.ViewSpaceLeaveRequest) {
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
    }
}