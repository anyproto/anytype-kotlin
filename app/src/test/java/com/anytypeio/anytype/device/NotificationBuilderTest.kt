package com.anytypeio.anytype.device

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.DecryptedPushContent
import kotlin.test.Test
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.clearInvocations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // API 28
class NotificationBuilderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationBuilder
    private val testSpaceId = "space123"

    // A simple stub for DecryptedPushContent.Message
    private val message = DecryptedPushContent.Message(
        chatId = "chat456",
        senderName = "Alice",
        spaceName = "My Space",
        msgId = "msg789",
        text = "Hello, this is a test message.",
        hasAttachments = false
    )

    @Before
    fun setUp() {
        notificationManager = mock()
        builder = NotificationBuilder(context, notificationManager)
    }

    @After
    fun tearDown() {
        clearInvocations(notificationManager)
    }

    @Test
    fun `buildAndNotify should create channel and post notification`() {
        // When
        builder.buildAndNotify(message, testSpaceId)

        // Then: a channel should be created with correct id and name
        verify(notificationManager).createNotificationChannel(argThat {
            id == testSpaceId && name == "My Space"
        })
        // And a notification should be posted
        verify(notificationManager).notify(any(), any<android.app.Notification>())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O]) // API 26+ for channels
    fun `clearNotificationChannel should cancel active and delete channel`() {
        // Prepare two mock StatusBarNotifications
        val notif1: android.app.Notification = mock {
            on { channelId } doReturn testSpaceId
        }
        val notif2: android.app.Notification = mock {
            on { channelId } doReturn "other"
        }

        // Wrap them in StatusBarNotification mocks
        val sbn1: StatusBarNotification = mock {
            on { notification } doReturn notif1
            on { id } doReturn 1
        }
        val sbn2: StatusBarNotification = mock {
            on { notification } doReturn notif2
            on { id } doReturn 2
        }
        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbn1, sbn2))

        // Ensure channel exists
        builder.buildAndNotify(message, testSpaceId)

        // When
        builder.clearNotificationChannel(testSpaceId)

        // Then active notifications for this channel are cancelled
        verify(notificationManager).cancel(sbn1.id)
        // Other channels remain
        verify(notificationManager, never()).cancel(sbn2.id)
        // And channel is deleted
        verify(notificationManager).deleteNotificationChannel(testSpaceId)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1]) // API < M [22]
    fun `clearNotificationChannel on preO should cancelAll`() {
        // When
        builder.clearNotificationChannel(testSpaceId)

        // Then
        verify(notificationManager).cancelAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O]) // API 26+ for channels
    fun `clearNotificationChannel with multiple spaces should only clear specified channel`() {
        // Prepare three mock notifications for spaces A, B, C
        val spaceA = "spaceA"
        val spaceB = "spaceB"
        val spaceC = "spaceC"
        val notifA: Notification = mock {
            on { channelId } doReturn spaceA
        }
        val notifB: Notification = mock {
            on { channelId } doReturn spaceB
        }
        val notifC: Notification = mock {
            on { channelId } doReturn spaceC
        }
        val sbnA: StatusBarNotification = mock {
            on { notification } doReturn notifA
            on { id } doReturn 10
        }
        val sbnB: StatusBarNotification = mock {
            on { notification } doReturn notifB
            on { id } doReturn 20
        }
        val sbnC: StatusBarNotification = mock {
            on { notification } doReturn notifC
            on { id } doReturn 30
        }
        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbnA, sbnB, sbnC))

        // Ensure channels exist by sending a dummy notification for each
        val dummyMessage = DecryptedPushContent.Message(
            chatId = "chat456",
            senderName = "Alice",
            spaceName = "My Space",
            msgId = "msg789",
            text = "Hello, this is a test message.",
            hasAttachments = false
        )

        builder.buildAndNotify(dummyMessage, spaceA)
        builder.buildAndNotify(dummyMessage, spaceB)
        builder.buildAndNotify(dummyMessage, spaceC)

        // Clear only spaceB
        builder.clearNotificationChannel(spaceB)

        // Verify only notifications for spaceB were cancelled
        verify(notificationManager, never()).cancel(10)
        verify(notificationManager).cancel(20)
        verify(notificationManager, never()).cancel(30)
        // Verify only the specified channel was deleted
        verify(notificationManager).deleteNotificationChannel(spaceB)
        verify(notificationManager, never()).deleteNotificationChannel(spaceA)
        verify(notificationManager, never()).deleteNotificationChannel(spaceC)
    }
}
