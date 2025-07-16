package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_models.stubChatPreview
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.vault.SetSpaceOrder
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

    //region Drag and Drop Tests

    @Test
    fun `onOrderChanged should update local state immediately with new order`() = runTest {
        // Given
        val space1Id = "space1"
        val space2Id = "space2"
        val space3Id = "space3"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )
        val space2 = StubSpaceView(
            id = space2Id,
            targetSpaceId = space2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--bb",
            createdDate = now - 1000
        )
        val space3 = StubSpaceView(
            id = space3Id,
            targetSpaceId = space3Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--cc",
            createdDate = now - 2000
        )

        val spacesList = listOf(space1, space2, space3)
        val permissions = mapOf(
            space1Id to SpaceMemberPermissions.OWNER,
            space2Id to SpaceMemberPermissions.OWNER,
            space3Id to SpaceMemberPermissions.OWNER
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        advanceUntilIdle()

        // Initial order should be space1, space2, space3 (sorted by spaceOrder)
        val initialPinnedSpaces = viewModel.sections.value.pinnedSpaces
        assertEquals(listOf(space1Id, space2Id, space3Id), initialPinnedSpaces.map { it.space.id })

        // When - Move space1 to position 2 (the position of space3)
        viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space3Id)

        // Then - Local state should be updated immediately
        val updatedPinnedSpaces = viewModel.sections.value.pinnedSpaces
        assertEquals(listOf(space2Id, space3Id, space1Id), updatedPinnedSpaces.map { it.space.id })
    }

    @Test
    fun `onOrderChanged should do nothing when fromSpaceId equals toSpaceId`() = runTest {
        // Given
        val space1Id = "space1"
        val space2Id = "space2"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )
        val space2 = StubSpaceView(
            id = space2Id,
            targetSpaceId = space2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--bb",
            createdDate = now - 1000
        )

        val spacesList = listOf(space1, space2)
        val permissions = mapOf(
            space1Id to SpaceMemberPermissions.OWNER,
            space2Id to SpaceMemberPermissions.OWNER
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        advanceUntilIdle()

        val initialPinnedSpaces = viewModel.sections.value.pinnedSpaces
        val initialOrder = initialPinnedSpaces.map { it.space.id }

        // When - Try to move space1 to itself
        viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space1Id)

        // Then - Order should remain unchanged
        val updatedPinnedSpaces = viewModel.sections.value.pinnedSpaces
        assertEquals(initialOrder, updatedPinnedSpaces.map { it.space.id })
    }

    @Test
    fun `onOrderChanged should do nothing when space is not found in pinned spaces`() = runTest {
        // Given
        val space1Id = "space1"
        val space2Id = "space2"
        val nonExistentSpaceId = "nonExistent"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )
        val space2 = StubSpaceView(
            id = space2Id,
            targetSpaceId = space2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--bb",
            createdDate = now - 1000
        )

        val spacesList = listOf(space1, space2)
        val permissions = mapOf(
            space1Id to SpaceMemberPermissions.OWNER,
            space2Id to SpaceMemberPermissions.OWNER
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        advanceUntilIdle()

        val initialPinnedSpaces = viewModel.sections.value.pinnedSpaces
        val initialOrder = initialPinnedSpaces.map { it.space.id }

        // When - Try to move from non-existent space
        viewModel.onOrderChanged(fromSpaceId = nonExistentSpaceId, toSpaceId = space1Id)

        // Then - Order should remain unchanged
        val updatedPinnedSpaces = viewModel.sections.value.pinnedSpaces
        assertEquals(initialOrder, updatedPinnedSpaces.map { it.space.id })
    }

    @Test
    fun `onDragEnd should call ReorderPinnedSpaces with correct parameters`() = runTest {
        // Given
        val space1Id = "space1"
        val space2Id = "space2"
        val space3Id = "space3"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )
        val space2 = StubSpaceView(
            id = space2Id,
            targetSpaceId = space2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--bb",
            createdDate = now - 1000
        )
        val space3 = StubSpaceView(
            id = space3Id,
            targetSpaceId = space3Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--cc",
            createdDate = now - 2000
        )

        val spacesList = listOf(space1, space2, space3)
        val permissions = mapOf(
            space1Id to SpaceMemberPermissions.OWNER,
            space2Id to SpaceMemberPermissions.OWNER,
            space3Id to SpaceMemberPermissions.OWNER
        )

        // Mock the ReorderPinnedSpaces use case
        val reorderPinnedSpaces = mock<SetSpaceOrder>()
        whenever(reorderPinnedSpaces.async(any())).thenReturn(com.anytypeio.anytype.domain.base.Resultat.Success(Unit))

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = reorderPinnedSpaces
        )

        advanceUntilIdle()

        // When - Perform drag operation
        viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space3Id)
        viewModel.onDragEnd()

        advanceUntilIdle()

        // Then - ReorderPinnedSpaces should be called with correct parameters
        val expectedNewOrder = listOf(space2Id, space3Id, space1Id)
        val expectedParams = com.anytypeio.anytype.domain.vault.SetSpaceOrder.Params(
            spaceViewId = space1Id,
            spaceViewOrder = expectedNewOrder
        )

        org.mockito.kotlin.verify(reorderPinnedSpaces).async(expectedParams)
    }

    @Test
    fun `onDragEnd should do nothing when no pending order changes`() = runTest {
        // Given
        val space1Id = "space1"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )

        val spacesList = listOf(space1)
        val permissions = mapOf(space1Id to SpaceMemberPermissions.OWNER)

        val reorderPinnedSpaces = mock<SetSpaceOrder>()

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = reorderPinnedSpaces
        )

        advanceUntilIdle()

        // When - Call onDragEnd without any prior onOrderChanged
        viewModel.onDragEnd()

        advanceUntilIdle()

        // Then - ReorderPinnedSpaces should not be called
        org.mockito.kotlin.verifyNoInteractions(reorderPinnedSpaces)
    }

    @Test
    fun `onDragEnd should handle ReorderPinnedSpaces failure gracefully`() = runTest {
        // Given
        val space1Id = "space1"
        val space2Id = "space2"
        val now = System.currentTimeMillis().toDouble()

        val space1 = StubSpaceView(
            id = space1Id,
            targetSpaceId = space1Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--aa",
            createdDate = now
        )
        val space2 = StubSpaceView(
            id = space2Id,
            targetSpaceId = space2Id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            spaceOrder = "--bb",
            createdDate = now - 1000
        )

        val spacesList = listOf(space1, space2)
        val permissions = mapOf(
            space1Id to SpaceMemberPermissions.OWNER,
            space2Id to SpaceMemberPermissions.OWNER
        )

        // Mock the ReorderPinnedSpaces use case to return failure
        val reorderPinnedSpaces = mock<SetSpaceOrder>()
        val errorMessage = "Network error"
        whenever(reorderPinnedSpaces.async(any())).thenReturn(Resultat.Failure(Exception(errorMessage)))

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviews()).thenReturn(flowOf(emptyList()))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted))
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = reorderPinnedSpaces
        )

        advanceUntilIdle()

        // When - Perform drag operation that will fail
        viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space2Id)
        viewModel.onDragEnd()

        advanceUntilIdle()

        // Then - Error should be set in notificationError
        assertEquals(errorMessage, viewModel.notificationError.value)
    }

    //endregion
} 