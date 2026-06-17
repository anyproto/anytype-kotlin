package com.anytypeio.anytype.presentation.notifications

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.multiplayer.GetSpaceMemberByIdentity
import com.anytypeio.anytype.domain.notifications.ReplyNotifications
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.spaces.ResolveSpaceHomepage
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class NotificationActionDelegateTest {

    @Test
    fun `should buffer notification command emitted before a collector subscribes`() = runTest {
        // GIVEN — a delegate whose leave-request flow can run to completion.
        val replyNotifications = mock<ReplyNotifications>().apply {
            stub { onBlocking { async(any()) } doReturn Resultat.Success(Unit) }
        }
        val delegate = NotificationActionDelegate.Default(
            getSpaceMemberByIdentity = mock<GetSpaceMemberByIdentity>(),
            replyNotifications = replyNotifications,
            systemNotificationService = mock<SystemNotificationService>(),
            spaceManager = mock<SpaceManager>(),
            saveCurrentSpace = mock<SaveCurrentSpace>(),
            resolveSpaceHomepage = mock<ResolveSpaceHomepage>()
        )

        val space = SpaceId("space-1")
        val action = NotificationAction.Multiplayer.ViewSpaceLeaveRequest(
            notification = "notification-1",
            space = space
        )

        // WHEN — the command is dispatched while NOBODY is collecting `dispatcher`.
        // This reproduces a notification tap arriving during the cold-start
        // activity stop -> resume gap, before MainActivity's STARTED-scoped collector
        // has re-attached.
        delegate.proceedWithNotificationAction(action)

        // THEN — a collector subscribing afterwards must still receive the command.
        // With a `replay = 0` SharedFlow the command was silently dropped; the
        // buffered Channel delivers it to the late subscriber (DROID-4523).
        delegate.dispatcher.test {
            assertEquals(
                NotificationCommand.ViewSpaceLeaveRequest(space = space),
                awaitItem()
            )
        }
    }
}
