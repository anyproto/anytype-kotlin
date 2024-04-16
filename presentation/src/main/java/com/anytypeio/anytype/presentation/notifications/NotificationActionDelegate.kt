package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.GetSpaceMemberByIdentity
import com.anytypeio.anytype.domain.notifications.ReplyNotifications
import javax.inject.Inject
import timber.log.Timber

interface NotificationActionDelegate {

    suspend fun proceedWithNotificationAction(action: NotificationAction)

    class Default @Inject constructor(
        private val getSpaceMemberByIdentity: GetSpaceMemberByIdentity,
        private val replyNotifications: ReplyNotifications
    ) : NotificationActionDelegate {

        override suspend fun proceedWithNotificationAction(action: NotificationAction) {
            Timber.d("Proceeding with notification action: $action")
            when(action) {
                is NotificationAction.Multiplayer.ViewSpaceJoinRequest -> {
                    proceedWithSpaceJoinRequest(action)
                }
                is NotificationAction.Multiplayer.ViewSpaceLeaveRequest -> {
                    TODO()
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
                        val command = NotificationsViewModel.Command.ViewSpaceJoinRequest(
                            space = action.space,
                            member = member.id
                        )
                        Timber.d("Command: $command")
                    } else {
                        Timber.w("Space member not found")
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while searching space member by identity")
                }
            )
        }
    }
}