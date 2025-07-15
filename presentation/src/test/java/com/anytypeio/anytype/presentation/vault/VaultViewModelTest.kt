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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
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

    /**
     * Test that VaultViewModel.transformToVaultSpaceViews correctly partitions spaces into pinned and main (unpinned) sections,
     * and sorts them according to the business logic:
     * - Pinned spaces are sorted by spaceOrder (ascending)
     * - Main (unpinned) spaces, including chat spaces with previews, are sorted by lastMessageDate (descending),
     *   then by createdDate (descending)
     * - Chat previews are mapped to their corresponding chat spaces
     *
     * This test covers a mix of pinned, unpinned, and chat spaces, each with different properties and dates.
     */
    @Test
    fun `partitions and sorts pinned, unpinned, and chat spaces with previews according to VaultViewModel logic`() = runTest {
        // Arrange
        val pinnedSpaceId = "pinned1"
        val pinnedSpace2Id = "pinned2"
        val unpinnedSpaceId = "unpinned1"
        val chatSpace1Id = "chat1"
        val chatSpace2Id = "chat2"
        val chatId1 = "chatId1"
        val chatId2 = "chatId2"
        val now = System.currentTimeMillis().toDouble()
        val earlier = now - 10000
        val evenEarlier = now - 20000

        val pinnedSpace = StubSpaceView(
            id = pinnedSpaceId,
            targetSpaceId = pinnedSpaceId,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            spaceOrder = "--aa",
            createdDate = now
        )
        val pinnedSpace2 = StubSpaceView(
            id = pinnedSpace2Id,
            targetSpaceId = pinnedSpace2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            spaceOrder = "--bb",
            createdDate = earlier
        )
        val unpinnedSpace = StubSpaceView(
            id = unpinnedSpaceId,
            targetSpaceId = unpinnedSpaceId,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            createdDate = evenEarlier
        )
        val chatSpace1 = StubSpaceView(
            id = chatSpace1Id,
            targetSpaceId = chatSpace1Id,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = chatId1,
            spaceUxType = SpaceUxType.CHAT,
            createdDate = now
        )
        val chatSpace2 = StubSpaceView(
            id = chatSpace2Id,
            targetSpaceId = chatSpace2Id,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = chatId2,
            spaceUxType = SpaceUxType.CHAT,
            createdDate = earlier
        )

        val chatPreview1 = stubChatPreview(chatSpace1Id, chatId1, (now + 5000).toLong())
        val chatPreview2 = stubChatPreview(chatSpace2Id, chatId2, (now + 1000).toLong())

        val spacesList = listOf(pinnedSpace2, pinnedSpace, unpinnedSpace, chatSpace1, chatSpace2)
        val chatPreviews = listOf(chatPreview1, chatPreview2)
        val permissions = mapOf(
            pinnedSpaceId to SpaceMemberPermissions.OWNER,
            pinnedSpace2Id to SpaceMemberPermissions.OWNER,
            unpinnedSpaceId to SpaceMemberPermissions.OWNER,
            chatSpace1Id to SpaceMemberPermissions.OWNER,
            chatSpace2Id to SpaceMemberPermissions.OWNER
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(chatPreviews))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Shared")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        advanceUntilIdle()

        val sections = viewModel.sections.value
        val pinned = sections.pinnedSpaces
        val main = sections.mainSpaces

        // Assert pinned spaces are sorted by spaceOrder
        assertEquals(listOf(pinnedSpace, pinnedSpace2).map { it.id }, pinned.map { it.space.id })

        // Assert main spaces (unpinned and chat) are sorted by lastMessageDate (desc), then createdDate (desc)
        // chatSpace1 (latest message), chatSpace2, unpinnedSpace (no chat preview)
        assertEquals(listOf(chatSpace1, chatSpace2, unpinnedSpace).map { it.id }, main.map { it.space.id })
    }
} 