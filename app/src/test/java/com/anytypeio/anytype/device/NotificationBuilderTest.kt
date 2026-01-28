package com.anytypeio.anytype.device

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.clearInvocations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // API 28
class NotificationBuilderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var notificationManager: NotificationManager
    private lateinit var stringResourceProvider: StringResourceProvider
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    private lateinit var chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer
    private lateinit var builder: NotificationBuilderImpl
    private val testSpaceId = "space123"
    private val testChatId = "chat456"
    private val testGroupId = "group789"

    // A simple stub for DecryptedPushContent.Message
    private val message = DecryptedPushContent.Message(
        chatId = testChatId,
        senderName = "Alice",
        spaceName = "My Space",
        msgId = "msg789",
        text = "Hello, this is a test message.",
        hasAttachments = false
    )

    @Before
    fun setUp() {
        stringResourceProvider = mock<StringResourceProvider> {
            on { getAttachmentText() } doReturn "[attachment]"
            on { getMessagesCountText(any()) } doReturn "new messages"
        }
        notificationManager = mock()
        urlBuilder = mock()
        spaceViewSubscriptionContainer = mock()
        chatsDetailsSubscriptionContainer = mock()

        builder = NotificationBuilderImpl(
            context = context,
            notificationManager = notificationManager,
            resourceProvider = stringResourceProvider,
            urlBuilder = urlBuilder,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatsDetailsSubscriptionContainer = chatsDetailsSubscriptionContainer
        )
    }

    @After
    fun tearDown() {
        clearInvocations(notificationManager)
    }

    @Test
    fun `buildAndNotify should create channel and post notification with group`() = runBlocking {
        // When
        builder.buildAndNotify(message, testSpaceId, testGroupId)

        // Then: a channel should be created with correct id and name
        verify(notificationManager).createNotificationChannel(argThat {
            id == "${testSpaceId}_${testChatId}" && name == "My Space"
        })
        // And a notification should be posted with group tag
        verify(notificationManager).notify(eq(testGroupId), any(), any<Notification>())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O]) // API 26+ for channels
    fun `clearNotificationChannel should cancel active and delete channel`() = runBlocking {
        val channelId = "${testSpaceId}_${testChatId}"
        // Prepare two mock StatusBarNotifications
        val notif1: android.app.Notification = mock()
        val notif2: android.app.Notification = mock()

        whenever(notif1.channelId).thenReturn(channelId)
        whenever(notif2.channelId).thenReturn("other")

        // Wrap them in StatusBarNotification mocks
        val sbn1: StatusBarNotification = mock()
        val sbn2: StatusBarNotification = mock()

        whenever(sbn1.notification).thenReturn(notif1)
        whenever(sbn1.id).thenReturn(1)
        whenever(sbn2.notification).thenReturn(notif2)
        whenever(sbn2.id).thenReturn(2)

        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbn1, sbn2))

        // Ensure channel exists
        builder.buildAndNotify(message, testSpaceId, testGroupId)

        // When
        builder.clearNotificationChannel(testSpaceId, testChatId)

        // Then active notifications for this channel are cancelled
        verify(notificationManager).cancel(sbn1.id)
        // Other channels remain
        verify(notificationManager, never()).cancel(sbn2.id)
        // And channel is deleted
        verify(notificationManager).deleteNotificationChannel(channelId)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O]) // API 26+ for channels
    fun `clearNotificationChannel with multiple chats in same space should only clear specified chat`() = runBlocking {
        // Prepare three mock notifications for different chats in the same space
        val chat1 = "chat1"
        val chat2 = "chat2"
        val chat3 = "chat3"
        val notif1: Notification = mock {
            on { channelId } doReturn "${testSpaceId}_${chat1}"
        }
        val notif2: Notification = mock {
            on { channelId } doReturn "${testSpaceId}_${chat2}"
        }
        val notif3: Notification = mock {
            on { channelId } doReturn "${testSpaceId}_${chat3}"
        }
        val sbn1: StatusBarNotification = mock {
            on { notification } doReturn notif1
            on { id } doReturn 10
        }
        val sbn2: StatusBarNotification = mock {
            on { notification } doReturn notif2
            on { id } doReturn 20
        }
        val sbn3: StatusBarNotification = mock {
            on { notification } doReturn notif3
            on { id } doReturn 30
        }
        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbn1, sbn2, sbn3))

        // Ensure channels exist by sending a dummy notification for each chat
        builder.buildAndNotify(message.copy(chatId = chat1), testSpaceId, "${testGroupId}_1")
        builder.buildAndNotify(message.copy(chatId = chat2), testSpaceId, "${testGroupId}_2")
        builder.buildAndNotify(message.copy(chatId = chat3), testSpaceId, "${testGroupId}_3")

        // Clear only chat2
        builder.clearNotificationChannel(testSpaceId, chat2)

        // Verify only notifications for chat2 were cancelled
        verify(notificationManager, never()).cancel(10)
        verify(notificationManager).cancel(20)
        verify(notificationManager, never()).cancel(30)
        // Verify only the specified channel was deleted
        verify(notificationManager).deleteNotificationChannel("${testSpaceId}_${chat2}")
        verify(notificationManager, never()).deleteNotificationChannel("${testSpaceId}_${chat1}")
        verify(notificationManager, never()).deleteNotificationChannel("${testSpaceId}_${chat3}")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O]) // API 26+ for channels
    fun `clearNotificationChannel with multiple spaces and chats should only clear specified chat`() = runBlocking {
        // Prepare notifications for different spaces and chats
        val space1 = "space1"
        val space2 = "space2"
        val chat1 = "chat1"
        val chat2 = "chat2"
        
        val notif1: Notification = mock {
            on { channelId } doReturn "${space1}_${chat1}"
        }
        val notif2: Notification = mock {
            on { channelId } doReturn "${space1}_${chat2}"
        }
        val notif3: Notification = mock {
            on { channelId } doReturn "${space2}_${chat1}"
        }
        val notif4: Notification = mock {
            on { channelId } doReturn "${space2}_${chat2}"
        }
        
        val sbn1: StatusBarNotification = mock {
            on { notification } doReturn notif1
            on { id } doReturn 10
        }
        val sbn2: StatusBarNotification = mock {
            on { notification } doReturn notif2
            on { id } doReturn 20
        }
        val sbn3: StatusBarNotification = mock {
            on { notification } doReturn notif3
            on { id } doReturn 30
        }
        val sbn4: StatusBarNotification = mock {
            on { notification } doReturn notif4
            on { id } doReturn 40
        }
        
        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbn1, sbn2, sbn3, sbn4))

        // Ensure channels exist by sending a dummy notification for each
        builder.buildAndNotify(message.copy(chatId = chat1), space1, "${testGroupId}_s1c1")
        builder.buildAndNotify(message.copy(chatId = chat2), space1, "${testGroupId}_s1c2")
        builder.buildAndNotify(message.copy(chatId = chat1), space2, "${testGroupId}_s2c1")
        builder.buildAndNotify(message.copy(chatId = chat2), space2, "${testGroupId}_s2c2")

        // Clear only space1_chat2
        builder.clearNotificationChannel(space1, chat2)

        // Verify only notifications for space1_chat2 were cancelled
        verify(notificationManager, never()).cancel(10)
        verify(notificationManager).cancel(20)
        verify(notificationManager, never()).cancel(30)
        verify(notificationManager, never()).cancel(40)
        
        // Verify only the specified channel was deleted
        verify(notificationManager).deleteNotificationChannel("${space1}_${chat2}")
        verify(notificationManager, never()).deleteNotificationChannel("${space1}_${chat1}")
        verify(notificationManager, never()).deleteNotificationChannel("${space2}_${chat1}")
        verify(notificationManager, never()).deleteNotificationChannel("${space2}_${chat2}")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M]) // API 23+ for activeNotifications
    fun `clearNotificationsByGroupId should cancel all notifications with matching group`() {
        val groupId = "test_group"
        
        // Create mock notifications with different tags/groups
        val notif1: Notification = mock {
            on { group } doReturn groupId
        }
        val notif2: Notification = mock {
            on { group } doReturn "other_group"
        }
        val notif3: Notification = mock {
            on { group } doReturn groupId
        }
        
        val sbn1: StatusBarNotification = mock {
            on { notification } doReturn notif1
            on { tag } doReturn groupId
            on { id } doReturn 10
        }
        val sbn2: StatusBarNotification = mock {
            on { notification } doReturn notif2
            on { tag } doReturn "other_group"
            on { id } doReturn 20
        }
        val sbn3: StatusBarNotification = mock {
            on { notification } doReturn notif3
            on { tag } doReturn groupId
            on { id } doReturn 30
        }
        
        whenever(notificationManager.activeNotifications).thenReturn(arrayOf(sbn1, sbn2, sbn3))
        
        // When
        builder.clearNotificationsByGroupId(groupId)
        
        // Then: notifications with matching group should be cancelled
        verify(notificationManager).cancel(groupId, 10)
        verify(notificationManager).cancel(groupId, 30)
        // Other notifications should not be affected
        verify(notificationManager, never()).cancel("other_group", 20)
        
        // Summary notification should also be cancelled
        verify(notificationManager).cancel(eq("chat_summary"), any())
    }

    @Test
    fun `clearNotificationsByGroupId should work on older Android versions without activeNotifications`() = runBlocking {
        // Simulate older Android version by returning empty active notifications
        whenever(notificationManager.activeNotifications).thenReturn(emptyArray())
        
        val groupId = "test_group"
        
        // Create a notification first to populate internal tracking
        builder.buildAndNotify(message, testSpaceId, groupId)
        
        // When clearing by group ID
        builder.clearNotificationsByGroupId(groupId)
        
        // Then: method should complete without errors
        // (This mainly tests that it doesn't crash on older Android versions)
        verify(notificationManager, times(2)).cancel(any(), any()) // Individual + summary notification cancel
    }

    @Test
    fun `buildAndNotify with multiple messages should create summary notification`() = runBlocking {
        val groupId = "test_group"
        val message1 = message.copy(msgId = "msg1", text = "First message")
        val message2 = message.copy(msgId = "msg2", text = "Second message")
        
        // When sending multiple messages for same group
        builder.buildAndNotify(message1, testSpaceId, groupId)
        builder.buildAndNotify(message2, testSpaceId, groupId)
        
        // Then: individual notifications should be posted (2 calls with group tag)
        verify(notificationManager, times(2)).notify(eq(groupId), any(), any<Notification>())
        
        // And summary notification should be created (1 call with summary tag)
        verify(notificationManager).notify(eq("chat_summary"), any(), any<Notification>())
        
        // Verify getMessagesCountText was called for summary
        verify(stringResourceProvider).getMessagesCountText(2)
        Unit
    }

    @Test
    fun `buildAndNotify should use author name as title for Data space notifications`() = runBlocking {
        // Given: A Data space with a chat that has a specific name
        val chatName = "Team Discussion"
        val spaceName = "My Workspace"
        val senderName = "Alice"

        val spaceView = ObjectWrapper.SpaceView(
            map = mapOf(
                "id" to testSpaceId,
                "name" to spaceName,
                "spaceUxType" to SpaceUxType.DATA.code.toDouble()
            )
        )

        val chatObject = ObjectWrapper.Basic(
            map = mapOf(
                "id" to testChatId,
                "name" to chatName
            )
        )

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(spaceView)
        whenever(chatsDetailsSubscriptionContainer.get(testChatId)).thenReturn(chatObject)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            // Extract title from notification extras
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

    @Test
    fun `buildAndNotify should use author name as title for Chat space notifications`() = runBlocking {
        // Given: A Chat space (1-to-1 chat) where space and chat are the same
        val spaceName = "Bob's Space"
        val senderName = "Bob"

        val spaceView = ObjectWrapper.SpaceView(
            map = mapOf(
                "id" to testSpaceId,
                "name" to spaceName,
                "spaceUxType" to SpaceUxType.CHAT.code.toDouble()
            )
        )

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(spaceView)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

    @Test
    fun `buildAndNotify should use author name as title when chat name is unavailable in Data space`() = runBlocking {
        // Given: A Data space but chat object has no name
        val spaceName = "My Workspace"
        val senderName = "Alice"

        val spaceView = ObjectWrapper.SpaceView(
            map = mapOf(
                "id" to testSpaceId,
                "name" to spaceName,
                "spaceUxType" to SpaceUxType.DATA.code.toDouble()
            )
        )

        val chatObject = ObjectWrapper.Basic(
            map = mapOf(
                "id" to testChatId
                // name is null
            )
        )

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(spaceView)
        whenever(chatsDetailsSubscriptionContainer.get(testChatId)).thenReturn(chatObject)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name (line 2 falls back to space name)
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

    @Test
    fun `buildAndNotify should use author name as title when chat object is not found in Data space`() = runBlocking {
        // Given: A Data space but chat object doesn't exist
        val spaceName = "My Workspace"
        val senderName = "Alice"

        val spaceView = ObjectWrapper.SpaceView(
            map = mapOf(
                "id" to testSpaceId,
                "name" to spaceName,
                "spaceUxType" to SpaceUxType.DATA.code.toDouble()
            )
        )

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(spaceView)
        whenever(chatsDetailsSubscriptionContainer.get(testChatId)).thenReturn(null) // Chat not found

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name (line 2 falls back to space name)
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

    @Test
    fun `buildAndNotify should use author name as title when space view is not found`() = runBlocking {
        // Given: Space view is not found (container returns null)
        val spaceName = "My Workspace"
        val senderName = "Alice"

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(null)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

    @Test
    fun `buildAndNotify should trim whitespace from author name`() = runBlocking {
        // Given: A message with sender name that has extra whitespace
        val senderNameWithWhitespace = "  Alice  "
        val senderNameTrimmed = "Alice"
        val spaceName = "My Workspace"

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(null)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderNameWithWhitespace)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be trimmed author name
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderNameTrimmed
        })
    }

    @Test
    fun `buildAndNotify should use author name as title when chat name is blank`() = runBlocking {
        // Given: A Data space with a chat that has a blank name
        val chatName = "   "
        val spaceName = "My Workspace"
        val senderName = "Alice"

        val spaceView = ObjectWrapper.SpaceView(
            map = mapOf(
                "id" to testSpaceId,
                "name" to spaceName,
                "spaceUxType" to SpaceUxType.DATA.code.toDouble()
            )
        )

        val chatObject = ObjectWrapper.Basic(
            map = mapOf(
                "id" to testChatId,
                "name" to chatName
            )
        )

        whenever(spaceViewSubscriptionContainer.get(SpaceId(testSpaceId))).thenReturn(spaceView)
        whenever(chatsDetailsSubscriptionContainer.get(testChatId)).thenReturn(chatObject)

        val testMessage = message.copy(spaceName = spaceName, senderName = senderName)

        // When
        builder.buildAndNotify(testMessage, testSpaceId, testGroupId)

        // Then: Notification title should be author name (line 2 falls back to space name when chat name is blank)
        verify(notificationManager).notify(eq(testGroupId), any(), argThat { notification ->
            val title = notification.extras?.getCharSequence("android.title")?.toString()
            title == senderName
        })
    }

}
