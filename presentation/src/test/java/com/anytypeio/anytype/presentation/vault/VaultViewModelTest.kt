package com.anytypeio.anytype.presentation.vault

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.chats.Chat
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
import com.anytypeio.anytype.domain.vault.UnpinSpace
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
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
    private lateinit var setSpaceOrder: SetSpaceOrder
    private lateinit var unpinSpace: UnpinSpace
    private lateinit var analytics: Analytics
    private lateinit var getSpaceWallpapers: GetSpaceWallpapers

    @Before
    fun setup() {
        spaceViewSubscriptionContainer = mock()
        chatPreviewContainer = mock()
        userPermissionProvider = mock()
        notificationPermissionManager = mock()
        stringResourceProvider = mock()
        setSpaceOrder = mock()
        unpinSpace = mock()
        analytics = mock()
        getSpaceWallpapers = mock()
        getSpaceWallpapers.stub {
            onBlocking { async(Unit) }.thenReturn(Resultat.Success(emptyMap()))
        }
    }

    @Test
    fun `transformToVaultSpaceViews creates Chat views for CHAT spaceUxType and Space views for other types`() = runTest {
        turbineScope {
            // Given - Simple test with one chat space and one data space
            val dataSpaceId = "data_space"
            val chatSpaceId = "chat_space"
            val chatId = "chat_id"
            
            val dataSpace = StubSpaceView(
                id = dataSpaceId,
                targetSpaceId = dataSpaceId,
                spaceUxType = SpaceUxType.DATA,
                chatId = null,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK
            )
            
            val chatSpace = StubSpaceView(
                id = chatSpaceId,
                targetSpaceId = chatSpaceId,
                spaceUxType = SpaceUxType.CHAT,
                chatId = chatId,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK
            )
            
            val spaceViews = listOf(dataSpace, chatSpace)
            val chatPreviews = emptyList<Chat.Preview>()
            val permissions = emptyMap<String, SpaceMemberPermissions>()
            
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaceViews))
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(chatPreviews))
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)
            whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")
            whenever(stringResourceProvider.getUntitledCreatorName()).thenReturn("Unknown")
            
            val viewModel = VaultViewModelFabric.create(
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                chatPreviewContainer = chatPreviewContainer,
                userPermissionProvider = userPermissionProvider,
                notificationPermissionManager = notificationPermissionManager,
                stringResourceProvider = stringResourceProvider,
                getSpaceWallpaper = getSpaceWallpapers
            )
            
            // When
            viewModel.uiState.test {
                val loading = awaitItem()
                assertTrue(loading is VaultUiState.Loading)
                val sections = awaitItem()
                check(sections is VaultUiState.Sections) {
                    "Expected Sections state, got $sections"
                }
                val allSpaces = sections.pinnedSpaces + sections.mainSpaces
                
                // Then - Verify we have two spaces
                assertEquals("Should have 2 spaces", 2, allSpaces.size)
                
                // Find the spaces
                val dataSpaceView = allSpaces.find { it.space.id == dataSpaceId }
                val chatSpaceView = allSpaces.find { it.space.id == chatSpaceId }
                
                // Verify they exist
                assertTrue("Data space should exist", dataSpaceView != null)
                assertTrue("Chat space should exist", chatSpaceView != null)
                
                // Verify types - this tests our spaceUxType logic
                assertTrue("Data space should be VaultSpaceView.Space", dataSpaceView is VaultSpaceView.Space)
                assertTrue("Chat space should be VaultSpaceView.Chat due to spaceUxType", chatSpaceView is VaultSpaceView.Chat)
            }
        }
    }

    @Test
    fun `transformToVaultSpaceViews creates Chat view for CHAT spaceUxType even without preview`() = runTest {
        turbineScope {
            // Given - Chat space without preview
            val chatSpaceId = "chat_space_no_preview"
            val chatId = "chat_id"
            
            val chatSpace = StubSpaceView(
                id = chatSpaceId,
                targetSpaceId = chatSpaceId,
                spaceUxType = SpaceUxType.CHAT,
                chatId = chatId,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK
            )
            
            val spaceViews = listOf(chatSpace)
            val chatPreviews = emptyList<com.anytypeio.anytype.core_models.chats.Chat.Preview>() // No preview
            val permissions = emptyMap<String, SpaceMemberPermissions>()
            
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaceViews))
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(chatPreviews))
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)
            whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")
            whenever(stringResourceProvider.getUntitledCreatorName()).thenReturn("Unknown")
            
            val viewModel = VaultViewModelFabric.create(
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                chatPreviewContainer = chatPreviewContainer,
                userPermissionProvider = userPermissionProvider,
                notificationPermissionManager = notificationPermissionManager,
                stringResourceProvider = stringResourceProvider,
                getSpaceWallpaper = getSpaceWallpapers
            )
            
            // When
            viewModel.uiState.test {
                skipItems(1) // Skip loading state
                val sections = awaitItem() as VaultUiState.Sections
                val allSpaces = sections.pinnedSpaces + sections.mainSpaces
                
                // Then - Verify chat space is still Chat view type even without preview
                val chatSpaceView = allSpaces.find { it.space.id == chatSpaceId }
                
                assertTrue("Chat space should be VaultSpaceView.Chat even without preview", 
                    chatSpaceView is VaultSpaceView.Chat)
                
                // Verify Chat view handles null preview gracefully
                val chatViewTyped = chatSpaceView as VaultSpaceView.Chat
                assertEquals("Chat space should have null preview", null, chatViewTyped.chatPreview)
                assertEquals("Unread message count should be 0", 0, chatViewTyped.unreadMessageCount)
                assertEquals("Unread mention count should be 0", 0, chatViewTyped.unreadMentionCount)
                assertEquals("Message text should be null", null, chatViewTyped.messageText)
                assertEquals("Creator name should be null", null, chatViewTyped.creatorName)
            }
        }
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
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(chatPreviews)
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(permissionStateFlow)
        whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            getSpaceWallpaper = getSpaceWallpapers
        )

        // Then
        assertEquals(VaultUiState.Loading, viewModel.uiState.value)
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
    fun `partitions and sorts pinned, unpinned, and chat spaces with previews according to VaultViewModel logic`() =
        runTest {
            turbineScope {
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

                val spacesList =
                    listOf(pinnedSpace2, pinnedSpace, unpinnedSpace, chatSpace1, chatSpace2)
                val chatPreviews = listOf(chatPreview1, chatPreview2)
                val permissions = mapOf(
                    pinnedSpaceId to SpaceMemberPermissions.OWNER,
                    pinnedSpace2Id to SpaceMemberPermissions.OWNER,
                    unpinnedSpaceId to SpaceMemberPermissions.OWNER,
                    chatSpace1Id to SpaceMemberPermissions.OWNER,
                    chatSpace2Id to SpaceMemberPermissions.OWNER
                )

                whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
                whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                    flowOf(
                        ChatPreviewContainer.PreviewState.Ready(chatPreviews)
                    )
                )
                whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
                whenever(notificationPermissionManager.permissionState()).thenReturn(
                    MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
                )
                whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Shared")

                val viewModel = VaultViewModelFabric.create(
                    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                    chatPreviewContainer = chatPreviewContainer,
                    userPermissionProvider = userPermissionProvider,
                    notificationPermissionManager = notificationPermissionManager,
                    stringResourceProvider = stringResourceProvider,
                    getSpaceWallpaper = getSpaceWallpapers
                )

                viewModel.uiState.test {
                    // First emission should be Loading
                    val loading = awaitItem()
                    assertTrue(loading is VaultUiState.Loading)

                    // Second emission should be Sections with our data
                    val sections = awaitItem() as VaultUiState.Sections
                    val pinned = sections.pinnedSpaces
                    val main = sections.mainSpaces

                    // Assert pinned spaces are sorted by spaceOrder
                    assertEquals(
                        listOf(pinnedSpace, pinnedSpace2).map { it.id },
                        pinned.map { it.space.id })

                    // Assert main spaces (unpinned and chat) are sorted by lastMessageDate (desc), then createdDate (desc)
                    // chatSpace1 (latest message), chatSpace2, unpinnedSpace (no chat preview)
                    assertEquals(
                        listOf(chatSpace1, chatSpace2, unpinnedSpace).map { it.id },
                        main.map { it.space.id })
                }
            }
        }

    //region Drag and Drop Tests

    @Test
    fun `onOrderChanged should update local state immediately with new order`() = runTest {
        turbineScope {
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
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(
                    ChatPreviewContainer.PreviewState.Ready(emptyList())
                )
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(
                    NotificationPermissionManagerImpl.PermissionState.Granted
                )
            )
            whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

            val viewModel = VaultViewModelFabric.create(
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                chatPreviewContainer = chatPreviewContainer,
                userPermissionProvider = userPermissionProvider,
                notificationPermissionManager = notificationPermissionManager,
                stringResourceProvider = stringResourceProvider,
                getSpaceWallpaper = getSpaceWallpapers
            )

            viewModel.uiState.test {
                // Skip Loading state
                skipItems(1)

                // Get initial Sections state
                val initialState = awaitItem() as VaultUiState.Sections
                assertEquals(
                    listOf(space1Id, space2Id, space3Id),
                    initialState.pinnedSpaces.map { it.space.id })

                // When - Move space1 to position 2 (the position of space3)
                viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space3Id)

                // Then - onOrderChanged should emit new state immediately with updated order
                val updatedState = awaitItem() as VaultUiState.Sections
                assertEquals(
                    listOf(space2Id, space3Id, space1Id),
                    updatedState.pinnedSpaces.map { it.space.id })
            }
        }
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
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(emptyList())
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            getSpaceWallpaper = getSpaceWallpapers
        )

        viewModel.uiState.test {
            // First emission should be Loading
            val loading = awaitItem()
            assertTrue(loading is VaultUiState.Loading)

            // Second emission should be Sections with our data
            val sections = awaitItem() as VaultUiState.Sections
            val initialOrder = sections.pinnedSpaces.map { it.space.id }

            // When - Try to move space1 to itself
            viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space1Id)

            // Then - Order should remain unchanged (no new emission expected)
            expectNoEvents()

            // Verify final state
            val finalSections = viewModel.uiState.value as VaultUiState.Sections
            assertEquals(initialOrder, finalSections.pinnedSpaces.map { it.space.id })
        }
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
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(emptyList())
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            getSpaceWallpaper = getSpaceWallpapers
        )

        viewModel.uiState.test {
            // First emission should be Loading
            val loading = awaitItem()
            assertTrue(loading is VaultUiState.Loading)

            // Second emission should be Sections with our data
            val sections = awaitItem() as VaultUiState.Sections
            val initialOrder = sections.pinnedSpaces.map { it.space.id }

            // When - Try to move from non-existent space
            viewModel.onOrderChanged(fromSpaceId = nonExistentSpaceId, toSpaceId = space1Id)

            // Then - Order should remain unchanged (no new emission expected)
            expectNoEvents()

            // Verify final state
            val finalSections = viewModel.uiState.value as VaultUiState.Sections
            assertEquals(initialOrder, finalSections.pinnedSpaces.map { it.space.id })
        }
    }

    @Test
    fun `onDragEnd should call SetSpaceOrder with correct parameters`() = runTest {
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
        val setSpaceOrder = mock<SetSpaceOrder>()
        whenever(setSpaceOrder.async(any())).thenReturn(
            com.anytypeio.anytype.domain.base.Resultat.Success(
                listOf(space1Id, space2Id)
            )
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(emptyList())
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = setSpaceOrder,
            getSpaceWallpaper = getSpaceWallpapers
        )

        turbineScope {
            viewModel.uiState.test {
                // Skip Loading state
                skipItems(1)

                // Get initial Sections state
                awaitItem() as VaultUiState.Sections

                // When - Perform drag operation
                viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space3Id)

                // onOrderChanged should emit immediate state change
                val dragState = awaitItem() as VaultUiState.Sections
                assertEquals(
                    listOf(space2Id, space3Id, space1Id),
                    dragState.pinnedSpaces.map { it.space.id })

                viewModel.onDragEnd()

                // No backend response state update expected with direct state approach

                // Give the coroutine time to complete
                advanceUntilIdle()

                // Then - SetSpaceOrder should be called with correct parameters
                val expectedNewOrder = listOf(space2Id, space3Id, space1Id)
                val expectedParams = SetSpaceOrder.Params(
                    spaceViewId = space1Id,
                    spaceViewOrder = expectedNewOrder
                )

                org.mockito.kotlin.verify(setSpaceOrder).async(expectedParams)
            }
        }
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

        val setSpaceOrder = mock<SetSpaceOrder>()

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(emptyList())
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = setSpaceOrder,
            getSpaceWallpaper = getSpaceWallpapers
        )

        viewModel.uiState.test {
            // First emission should be Loading
            val loading = awaitItem()
            assertTrue(loading is VaultUiState.Loading)

            // Second emission should be Sections with our data
            awaitItem() as VaultUiState.Sections

            // When - Call onDragEnd without any prior onOrderChanged
            viewModel.onDragEnd()

            // Then - No new emissions expected and SetSpaceOrder should not be called
            expectNoEvents()
            org.mockito.kotlin.verifyNoInteractions(setSpaceOrder)
        }
    }

    @Test
    fun `onDragEnd should handle SetSpaceOrder failure gracefully`() = runTest {
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

        // Mock the SetSpaceOrder use case to return failure
        val setSpaceOrder = mock<SetSpaceOrder>()
        val errorMessage = "Network error"
        whenever(setSpaceOrder.async(any())).thenReturn(Resultat.Failure(Exception(errorMessage)))

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(emptyList())
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            setSpaceOrder = setSpaceOrder,
            getSpaceWallpaper = getSpaceWallpapers
        )

        turbineScope {
            viewModel.uiState.test {
                // Skip Loading state
                skipItems(1)

                // Get initial Sections state
                awaitItem() as VaultUiState.Sections

                // When - Perform drag operation that will fail
                viewModel.onOrderChanged(fromSpaceId = space1Id, toSpaceId = space2Id)

                // onOrderChanged should emit immediate state change
                val dragState = awaitItem() as VaultUiState.Sections
                assertEquals(listOf(space2Id, space1Id), dragState.pinnedSpaces.map { it.space.id })

                viewModel.onDragEnd()

                // No backend response state update expected with direct state approach

                // Give the coroutine time to complete
                advanceUntilIdle()

                // Then - Error should be set in notificationError
                assertEquals(errorMessage, viewModel.notificationError.value)
            }
        }
    }

    //endregion

    //region PinnedCount Tests

    @Test
    fun `transformToVaultSpaceViews should calculate pinnedCount correctly with mixed spaces`() =
        runTest {
            turbineScope {
                // Given - 2 pinned spaces, 1 unpinned space
                val pinnedSpace1Id = "pinned1"
                val pinnedSpace2Id = "pinned2"
                val unpinnedSpaceId = "unpinned1"

                val pinnedSpace1 = StubSpaceView(
                    id = pinnedSpace1Id,
                    targetSpaceId = pinnedSpace1Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--aa"
                )
                val pinnedSpace2 = StubSpaceView(
                    id = pinnedSpace2Id,
                    targetSpaceId = pinnedSpace2Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--bb"
                )
                val unpinnedSpace = StubSpaceView(
                    id = unpinnedSpaceId,
                    targetSpaceId = unpinnedSpaceId,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                )

                val spacesList = listOf(pinnedSpace1, pinnedSpace2, unpinnedSpace)
                val permissions = mapOf(
                    pinnedSpace1Id to SpaceMemberPermissions.OWNER,
                    pinnedSpace2Id to SpaceMemberPermissions.OWNER,
                    unpinnedSpaceId to SpaceMemberPermissions.OWNER
                )

                whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
                whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                    flowOf(
                        ChatPreviewContainer.PreviewState.Ready(emptyList())
                    )
                )
                whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
                whenever(notificationPermissionManager.permissionState()).thenReturn(
                    MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
                )
                whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

                val viewModel = VaultViewModelFabric.create(
                    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                    chatPreviewContainer = chatPreviewContainer,
                    userPermissionProvider = userPermissionProvider,
                    notificationPermissionManager = notificationPermissionManager,
                    stringResourceProvider = stringResourceProvider,
                    getSpaceWallpaper = getSpaceWallpapers
                )

                viewModel.uiState.test {
                    // First emission should be Loading
                    val loading = awaitItem()
                    assertTrue(loading is VaultUiState.Loading)

                    // Second emission should be Sections with our data
                    val sections = awaitItem() as VaultUiState.Sections

                    // Then - pinnedCount should be 2
                    assertEquals(2, sections.pinnedSpaces.size)

                    // All pinned spaces should be marked as pinned
                    sections.pinnedSpaces.forEach { space ->
                        assertEquals(true, space.isPinned)
                    }

                    // Unpinned spaces should not be marked as pinned
                    sections.mainSpaces.forEach { space ->
                        assertEquals(false, space.isPinned)
                    }
                }
            }
        }

    @Test
    fun `transformToVaultSpaceViews should correctly separate spaces when at MAX_PINNED_SPACES limit`() =
        runTest {
            // Given - 6 pinned spaces (at MAX_PINNED_SPACES limit), 1 unpinned space
            val pinnedSpace1Id = "pinned1"
            val pinnedSpace2Id = "pinned2"
            val pinnedSpace3Id = "pinned3"
            val pinnedSpace4Id = "pinned4"
            val pinnedSpace5Id = "pinned5"
            val pinnedSpace6Id = "pinned6"
            val unpinnedSpaceId = "unpinned1"

            val pinnedSpace1 = StubSpaceView(
                id = pinnedSpace1Id,
                targetSpaceId = pinnedSpace1Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--aa"
            )
            val pinnedSpace2 = StubSpaceView(
                id = pinnedSpace2Id,
                targetSpaceId = pinnedSpace2Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--bb"
            )
            val pinnedSpace3 = StubSpaceView(
                id = pinnedSpace3Id,
                targetSpaceId = pinnedSpace3Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--cc"
            )
            val pinnedSpace4 = StubSpaceView(
                id = pinnedSpace4Id,
                targetSpaceId = pinnedSpace4Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--dd"
            )
            val pinnedSpace5 = StubSpaceView(
                id = pinnedSpace5Id,
                targetSpaceId = pinnedSpace5Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--ee"
            )
            val pinnedSpace6 = StubSpaceView(
                id = pinnedSpace6Id,
                targetSpaceId = pinnedSpace6Id,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK,
                spaceOrder = "--ff"
            )
            val unpinnedSpace = StubSpaceView(
                id = unpinnedSpaceId,
                targetSpaceId = unpinnedSpaceId,
                spaceAccessType = SpaceAccessType.DEFAULT,
                spaceAccountStatus = SpaceStatus.OK,
                spaceLocalStatus = SpaceStatus.OK
            )

            val spacesList = listOf(
                pinnedSpace1,
                pinnedSpace2,
                pinnedSpace3,
                pinnedSpace4,
                pinnedSpace5,
                pinnedSpace6,
                unpinnedSpace
            )
            val permissions = mapOf(
                pinnedSpace1Id to SpaceMemberPermissions.OWNER,
                pinnedSpace2Id to SpaceMemberPermissions.OWNER,
                pinnedSpace3Id to SpaceMemberPermissions.OWNER,
                pinnedSpace4Id to SpaceMemberPermissions.OWNER,
                pinnedSpace5Id to SpaceMemberPermissions.OWNER,
                pinnedSpace6Id to SpaceMemberPermissions.OWNER,
                unpinnedSpaceId to SpaceMemberPermissions.OWNER
            )

            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(
                    ChatPreviewContainer.PreviewState.Ready(emptyList())
                )
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(
                    NotificationPermissionManagerImpl.PermissionState.Granted
                )
            )
            whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

            val viewModel = VaultViewModelFabric.create(
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                chatPreviewContainer = chatPreviewContainer,
                userPermissionProvider = userPermissionProvider,
                notificationPermissionManager = notificationPermissionManager,
                stringResourceProvider = stringResourceProvider,
                getSpaceWallpaper = getSpaceWallpapers
            )

            viewModel.uiState.test {
                // First emission should be Loading
                val loading = awaitItem()
                assertTrue(loading is VaultUiState.Loading)

                // Second emission should be Sections with our data
                val sections = awaitItem() as VaultUiState.Sections

                // Then - pinnedCount should be 6 (at MAX_PINNED_SPACES limit)
                assertEquals(6, sections.pinnedSpaces.size)

                // All pinned spaces should be marked as pinned
                sections.pinnedSpaces.forEach { space ->
                    assertEquals(true, space.isPinned)
                }

                // Unpinned spaces should not be marked as pinned
                sections.mainSpaces.forEach { space ->
                    assertEquals(false, space.isPinned)
                }
            }
        }

    @Test
    fun `transformToVaultSpaceViews should correctly handle spaces when no spaces are pinned`() =
        runTest {
            turbineScope {
                // Given - 0 pinned spaces, 2 unpinned spaces
                val unpinnedSpace1Id = "unpinned1"
                val unpinnedSpace2Id = "unpinned2"

                val unpinnedSpace1 = StubSpaceView(
                    id = unpinnedSpace1Id,
                    targetSpaceId = unpinnedSpace1Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                )
                val unpinnedSpace2 = StubSpaceView(
                    id = unpinnedSpace2Id,
                    targetSpaceId = unpinnedSpace2Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                )

                val spacesList = listOf(unpinnedSpace1, unpinnedSpace2)
                val permissions = mapOf(
                    unpinnedSpace1Id to SpaceMemberPermissions.OWNER,
                    unpinnedSpace2Id to SpaceMemberPermissions.OWNER
                )

                whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
                whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                    flowOf(
                        ChatPreviewContainer.PreviewState.Ready(emptyList())
                    )
                )
                whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
                whenever(notificationPermissionManager.permissionState()).thenReturn(
                    MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
                )
                whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

                val viewModel = VaultViewModelFabric.create(
                    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                    chatPreviewContainer = chatPreviewContainer,
                    userPermissionProvider = userPermissionProvider,
                    notificationPermissionManager = notificationPermissionManager,
                    stringResourceProvider = stringResourceProvider,
                    getSpaceWallpaper = getSpaceWallpapers
                )

                viewModel.uiState.test {
                    val firstState = awaitItem()
                    val secondState = awaitItem() as VaultUiState.Sections
                    assertTrue(firstState is VaultUiState.Loading)
                    assertEquals(0, secondState.pinnedSpaces.size)
                    // All unpinned spaces should not be marked as pinned
                    secondState.mainSpaces.forEach { space ->
                        assertEquals(
                            false,
                            space.isPinned
                        )
                    }
                }
            }
        }

    @Test
    fun `transformToVaultSpaceViews should correctly identify pinned and unpinned chat spaces`() = runTest {
        // Given - 1 pinned chat space, 1 unpinned chat space
        val pinnedChatSpaceId = "pinnedChat"
        val unpinnedChatSpaceId = "unpinnedChat"
        val chatId1 = "chatId1"
        val chatId2 = "chatId2"

        val pinnedChatSpace = StubSpaceView(
            id = pinnedChatSpaceId,
            targetSpaceId = pinnedChatSpaceId,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = chatId1,
            spaceUxType = SpaceUxType.CHAT,
            spaceOrder = "--aa"
        )
        val unpinnedChatSpace = StubSpaceView(
            id = unpinnedChatSpaceId,
            targetSpaceId = unpinnedChatSpaceId,
            spaceAccessType = SpaceAccessType.SHARED,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = chatId2,
            spaceUxType = SpaceUxType.CHAT
        )

        val chatPreview1 = stubChatPreview(pinnedChatSpaceId, chatId1, System.currentTimeMillis())
        val chatPreview2 = stubChatPreview(unpinnedChatSpaceId, chatId2, System.currentTimeMillis())

        val spacesList = listOf(pinnedChatSpace, unpinnedChatSpace)
        val chatPreviews = listOf(chatPreview1, chatPreview2)
        val permissions = mapOf(
            pinnedChatSpaceId to SpaceMemberPermissions.OWNER,
            unpinnedChatSpaceId to SpaceMemberPermissions.OWNER
        )

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(
                ChatPreviewContainer.PreviewState.Ready(chatPreviews)
            )
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(
                NotificationPermissionManagerImpl.PermissionState.Granted
            )
        )
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Shared")

        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            getSpaceWallpaper = getSpaceWallpapers
        )

        viewModel.uiState.test {
            // First emission should be Loading
            val loading = awaitItem()
            assertTrue(loading is VaultUiState.Loading)

            // Second emission should be Sections with our data
            val sections = awaitItem() as VaultUiState.Sections

            // Then - pinnedCount should be 1
            assertEquals(1, sections.pinnedSpaces.size)

            // Pinned chat space should be marked as pinned
            sections.pinnedSpaces.forEach { space ->
                assertEquals(true, space.isPinned)
            }

            // Unpinned chat space should not be marked as pinned
            sections.mainSpaces.forEach { space ->
                assertEquals(false, space.isPinned)
            }
        }
    }

    @Test
    fun `transformToVaultSpaceViews should correctly update space collections when unpinning space at MAX_PINNED_SPACES limit`() =
        runTest {
            // Given - 6 pinned spaces (at MAX_PINNED_SPACES limit), 4 unpinned spaces
            val pinnedSpace1Id = "pinned1"
            val pinnedSpace2Id = "pinned2"
            val pinnedSpace3Id = "pinned3"
            val pinnedSpace4Id = "pinned4"
            val pinnedSpace5Id = "pinned5"
            val pinnedSpace6Id = "pinned6"
            val unpinnedSpace1Id = "unpinned1"
            val unpinnedSpace2Id = "unpinned2"
            val unpinnedSpace3Id = "unpinned3"
            val unpinnedSpace4Id = "unpinned4"

            // Initial state with 6 pinned spaces
            val initialPinnedSpaces = listOf(
                StubSpaceView(
                    id = pinnedSpace1Id,
                    targetSpaceId = pinnedSpace1Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--aa"
                ),
                StubSpaceView(
                    id = pinnedSpace2Id,
                    targetSpaceId = pinnedSpace2Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--bb"
                ),
                StubSpaceView(
                    id = pinnedSpace3Id,
                    targetSpaceId = pinnedSpace3Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--cc"
                ),
                StubSpaceView(
                    id = pinnedSpace4Id,
                    targetSpaceId = pinnedSpace4Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--dd"
                ),
                StubSpaceView(
                    id = pinnedSpace5Id,
                    targetSpaceId = pinnedSpace5Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--ee"
                ),
                StubSpaceView(
                    id = pinnedSpace6Id,
                    targetSpaceId = pinnedSpace6Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK,
                    spaceOrder = "--ff"
                )
            )

            val unpinnedSpaces = listOf(
                StubSpaceView(
                    id = unpinnedSpace1Id,
                    targetSpaceId = unpinnedSpace1Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                ),
                StubSpaceView(
                    id = unpinnedSpace2Id,
                    targetSpaceId = unpinnedSpace2Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                ),
                StubSpaceView(
                    id = unpinnedSpace3Id,
                    targetSpaceId = unpinnedSpace3Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                ),
                StubSpaceView(
                    id = unpinnedSpace4Id,
                    targetSpaceId = unpinnedSpace4Id,
                    spaceAccessType = SpaceAccessType.DEFAULT,
                    spaceAccountStatus = SpaceStatus.OK,
                    spaceLocalStatus = SpaceStatus.OK
                )
            )

            val initialSpacesList = initialPinnedSpaces + unpinnedSpaces
            val permissions =
                (initialPinnedSpaces + unpinnedSpaces).associate { it.id to SpaceMemberPermissions.OWNER }

            // Create a MutableStateFlow to simulate dynamic updates
            val spacesFlow = MutableStateFlow(initialSpacesList)

            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(spacesFlow)
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(
                    ChatPreviewContainer.PreviewState.Ready(emptyList())
                )
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(
                    NotificationPermissionManagerImpl.PermissionState.Granted
                )
            )
            whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

            val viewModel = VaultViewModelFabric.create(
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                chatPreviewContainer = chatPreviewContainer,
                userPermissionProvider = userPermissionProvider,
                notificationPermissionManager = notificationPermissionManager,
                stringResourceProvider = stringResourceProvider,
                getSpaceWallpaper = getSpaceWallpapers
            )

            viewModel.uiState.test {
                // First emission should be Loading
                val loading = awaitItem()
                assertTrue(loading is VaultUiState.Loading)

                // Second emission should be initial Sections with our data
                val initialSections = awaitItem() as VaultUiState.Sections
                assertEquals(6, initialSections.pinnedSpaces.size)
                assertEquals(4, initialSections.mainSpaces.size)

                // All pinned spaces should be marked as pinned
                initialSections.pinnedSpaces.forEach { space ->
                    assertEquals(true, space.isPinned)
                }

                // All unpinned spaces should not be marked as pinned
                initialSections.mainSpaces.forEach { space ->
                    assertEquals(false, space.isPinned)
                }

                // When - User unpins one space (pinnedSpace6)
                val updatedSpacesList = initialPinnedSpaces.take(5) + listOf(
                    StubSpaceView(
                        id = pinnedSpace6Id,
                        targetSpaceId = pinnedSpace6Id,
                        spaceAccessType = SpaceAccessType.DEFAULT,
                        spaceAccountStatus = SpaceStatus.OK,
                        spaceLocalStatus = SpaceStatus.OK
                    ) // Remove spaceOrder to unpin
                ) + unpinnedSpaces

                spacesFlow.value = updatedSpacesList

                // Third emission should be updated Sections after unpinning
                val updatedSections = awaitItem() as VaultUiState.Sections
                assertEquals(5, updatedSections.pinnedSpaces.size)
                assertEquals(
                    5,
                    updatedSections.mainSpaces.size
                ) // 4 original unpinned + 1 newly unpinned

                // All pinned spaces should be marked as pinned
                updatedSections.pinnedSpaces.forEach { space ->
                    assertEquals(true, space.isPinned)
                }

                // All unpinned spaces should not be marked as pinned
                updatedSections.mainSpaces.forEach { space ->
                    assertEquals(
                        "Space ${space.space.id} should not be pinned",
                        false,
                        space.isPinned
                    )
                }
            }
        }

    //endregion
} 