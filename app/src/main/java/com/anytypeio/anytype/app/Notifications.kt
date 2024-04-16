package com.anytypeio.anytype.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import javax.inject.Inject
import timber.log.Timber

class AnytypeNotificationService @Inject constructor(
    private val context: Context
) : SystemNotificationService {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    override fun notify(
        notification: Notification
    ) {
        when(val payload = notification.payload) {
            is NotificationPayload.ParticipantPermissionsChange -> {
                val placeholder = context.resources.getString(R.string.untitled)
                val title = context.resources.getString(
                    R.string.multiplayer_notification_member_permissions_changed
                )
                val body = if (payload.permissions.isOwnerOrEditor())
                    context.resources.getString(
                        R.string.multiplayer_notification_member_permission_change_edit,
                        payload.spaceName.ifEmpty { placeholder }
                    )
                else
                    context.resources.getString(
                        R.string.multiplayer_notification_member_permission_change_read,
                        payload.spaceName.ifEmpty { placeholder }
                    )
                showBasicNotification(
                    id = PERMISSIONS_CHANGED_ID,
                    title = title,
                    body = body
                )
            }
            is NotificationPayload.ParticipantRemove -> {
                val body = context.resources.getString(
                    R.string.multiplayer_notification_member_removed_from_space
                )
                showBasicNotification(
                    id = MEMBER_REMOVED_ID,
                    body = body
                )
            }
            is NotificationPayload.ParticipantRequestApproved -> {
                val placeholder = context.resources.getString(R.string.untitled)
                val title = context.resources.getString(
                    R.string.multiplayer_notification_member_request_approved
                )
                val body = if (payload.permissions.isOwnerOrEditor()) {
                    context.resources.getString(
                        R.string.multiplayer_notification_member_request_approved_with_edit_rights,
                        payload.spaceName.ifEmpty { placeholder }
                    )
                } else {
                    context.resources.getString(
                        R.string.multiplayer_notification_member_request_approved_with_read_only_rights,
                        payload.spaceName.ifEmpty { placeholder }
                    )
                }
                showBasicNotification(
                    id = REQUEST_APPROVED_ID,
                    title = title,
                    body = body
                )
            }
            is NotificationPayload.ParticipantRequestDecline -> {
                val placeholder = context.resources.getString(R.string.untitled)
                val title = context.resources.getString(
                    R.string.multiplayer_notification_request_declined
                )
                val body = context.resources.getString(
                    com.anytypeio.anytype.core_ui.R.string.multiplayer_notification_member_join_request_declined,
                    payload.spaceName.ifEmpty { placeholder }
                )
                showBasicNotification(
                    id = REQUEST_DECLINED_ID,
                    title = title,
                    body = body
                )
            }
            is NotificationPayload.RequestToJoin -> {
                val placeholder = context.resources.getString(R.string.untitled)
                val title = context.resources.getString(R.string.multiplayer_notification_new_join_request)
                val body = context.resources.getString(
                    R.string.multiplayer_notification_member_user_sends_join_request,
                    payload.identityName.ifEmpty { placeholder },
                    payload.spaceName.ifEmpty { placeholder },
                )
                showBasicNotification(
                    id = REQUEST_TO_JOIN_ID,
                    title = title,
                    body = body
                )
            }
            is NotificationPayload.RequestToLeave -> {
                val placeholder = context.resources.getString(R.string.untitled)
                val title = context.resources.getString(R.string.multiplayer_leave_request)
                val body = context.resources.getString(
                    R.string.multiplayer_notification_member_user_sends_leave_request,
                    payload.identityName.ifEmpty { placeholder },
                    payload.spaceName.ifEmpty { placeholder },
                )
                showBasicNotification(
                    id = REQUEST_TO_LEAVE_ID,
                    title = title,
                    body = body
                )
            }
            else -> {
                Timber.w("Ignoring notification")
            }
        }
    }

    private fun showBasicNotification(
        id: Int,
        title: String? = null,
        body: String
    ) {
        Timber.d("Attempt to show notification")

        val builder = NotificationCompat.Builder(
            context,
            AndroidApplication.NOTIFICATION_CHANNEL_ID
        )

        val notification = with(builder) {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            if (!title.isNullOrEmpty()) {
                setContentTitle(title)
            }
            setPriority(NotificationManager.IMPORTANCE_HIGH)
            setAutoCancel(true)
            setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        notificationManager.notify(
            id,
            notification.build()
        )
    }

    companion object {
        const val REQUEST_TO_JOIN_ID = 0
        const val REQUEST_TO_LEAVE_ID = 1
        const val REQUEST_APPROVED_ID = 2
        const val REQUEST_DECLINED_ID = 3
        const val MEMBER_REMOVED_ID = 4
        const val PERMISSIONS_CHANGED_ID = 5
    }
}

class AnytypeNotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("TODO: onReceive")
    }
}