package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_models.stubChatPreview
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.StringResourceProviderImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    private lateinit var chatPreviewContainer: ChatPreviewContainer
    private lateinit var userPermissionProvider: UserPermissionProvider
    private lateinit var notificationPermissionManager: NotificationPermissionManager
    private lateinit var stringResourceProvider: StringResourceProvider

    @Before
    fun setup() {
        spaceViewSubscriptionContainer = mock()
        chatPreviewContainer = mock()
        userPermissionProvider = mock()
        notificationPermissionManager = mock()
        stringResourceProvider = mock()
    }

    @Test
    fun `init should subscribe to flows and update state`() = runTest {
        // Given
        val spaceViews = emptyList<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>()
        val chatPreviews = emptyList<com.anytypeio.anytype.core_models.chats.Chat.Preview>()
        val permissions = emptyMap<String, SpaceMemberPermissions>()
        val permissionState = NotificationPermissionManagerImpl.PermissionState.Granted
        val permissionStateFlow = MutableStateFlow(permissionState)

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaceViews))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(chatPreviews))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(permissionStateFlow)
        whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager
        )

        // Then
        // After init, spaces and sections should be empty
        assertEquals(emptyList<VaultSpaceView>(), viewModel.spaces.value)
        assertEquals(VaultSectionView(), viewModel.sections.value)
        // Notification badge should be not disabled (since areNotificationsEnabled returns true)
        assertEquals(false, viewModel.isNotificationDisabled.value)
    }

    @Test
    fun `transformToVaultSpaceViews partitions and sorts pinned and unpinned spaces, maps chat previews`() = runTest {
        // Arrange
        val pinnedSpaceId = "pinned1"
        val unpinnedSpaceId = "unpinned1"
        val chatSpaceId = "chat1"
        val chatId = "chatId1"
        val now = System.currentTimeMillis().toDouble()
        val earlier = now - 10000

        val pinnedSpace = StubSpaceView(
            id = pinnedSpaceId,
            targetSpaceId = pinnedSpaceId,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            spaceOrder = "--aa",
            createdDate = now
        )
        val pinnedSpace2 = StubSpaceView(
            id = "pinned2",
            targetSpaceId = "pinned2",
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            spaceOrder = "--bb",
            createdDate = now - 5000
        )
        val pinnedSpace3 = StubSpaceView(
            id = "pinned3",
            targetSpaceId = "pinned3",
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            spaceOrder = "--ss",
            createdDate = now - 15000
        )
        val unpinnedSpace = StubSpaceView(
            id = unpinnedSpaceId,
            targetSpaceId = unpinnedSpaceId,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            createdDate = earlier
        )
        val unpinnedChatSpace = StubSpaceView(
            id = chatSpaceId,
            targetSpaceId = chatSpaceId,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = chatId,
            spaceUxType = SpaceUxType.CHAT,
            createdDate = now - 20000
        )

        val chatPreview = stubChatPreview(
            spaceId = chatSpaceId,
            chatId = chatId,
            lastMessageDate = 123456789L
        )

        val spacesList = listOf(pinnedSpace3, pinnedSpace2, pinnedSpace, unpinnedSpace, unpinnedChatSpace)
        val chatPreviewsList = listOf(chatPreview)
        val permissions = mapOf(
            pinnedSpace3.targetSpaceId!! to SpaceMemberPermissions.OWNER,
            pinnedSpace2.targetSpaceId!! to SpaceMemberPermissions.WRITER,
            pinnedSpace.targetSpaceId!! to SpaceMemberPermissions.WRITER,
            unpinnedSpace.targetSpaceId!! to SpaceMemberPermissions.WRITER,
            unpinnedChatSpace.targetSpaceId!! to SpaceMemberPermissions.WRITER
        )
        val permissionState = NotificationPermissionManagerImpl.PermissionState.Granted
        val permissionStateFlow = MutableStateFlow(permissionState)

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(chatPreviewsList))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(permissionStateFlow)
        whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)
        whenever(stringResourceProvider.getSpaceAccessTypeName(SpaceAccessType.SHARED)).thenReturn("Shared")
        whenever(stringResourceProvider.getSpaceAccessTypeName(SpaceAccessType.DEFAULT)).thenReturn("Default")
        whenever(stringResourceProvider.getSpaceAccessTypeName(SpaceAccessType.PRIVATE)).thenReturn("Private")
        whenever(stringResourceProvider.getSpaceAccessTypeName(null)).thenReturn("Unknown")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        advanceUntilIdle()

        // Assert
        val sections = viewModel.sections.value

        // Assert pinned spaces are sorted by spaceOrder
        val pinnedIds = sections.pinnedSpaces.map { it.space.id }
        assertEquals(listOf(pinnedSpace.id, pinnedSpace2.id, pinnedSpace3.id), pinnedIds)

        // Assert unpinned/main spaces are sorted by lastMessageDate or createdDate
        val mainIds = sections.mainSpaces.map { it.space.id }
        // If unpinnedChatSpace has a chat preview, it should come first, otherwise by createdDate
        assertEquals(listOf(unpinnedChatSpace.id, unpinnedSpace.id), mainIds)
    }
} 