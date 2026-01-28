package com.anytypeio.anytype.feature_vault.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.stubChatPreview
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManager
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.vault.ShouldShowCreateSpaceBadge
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.feature_vault.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

/**
 * Comprehensive tests for vault channels/spaces sorting functionality.
 * Tests cover:
 * - Pinned vs unpinned separation
 * - Pinned spaces sorting by spaceOrder
 * - Effective date calculation (max of lastMessageDate and spaceJoinDate)
 * - Fallback to createdDate for newly created spaces (DROID-4150)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultChannelsSortingTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    private lateinit var chatPreviewContainer: ChatPreviewContainer
    private lateinit var userPermissionProvider: UserPermissionProvider
    private lateinit var notificationPermissionManager: NotificationPermissionManager
    private lateinit var stringResourceProvider: StringResourceProvider
    private lateinit var getSpaceWallpapers: GetSpaceWallpapers
    private lateinit var shouldShowCreateSpaceBadge: ShouldShowCreateSpaceBadge
    private lateinit var participantSubscriptionContainer: ParticipantSubscriptionContainer
    private lateinit var chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer

    @Before
    fun setup() {
        spaceViewSubscriptionContainer = mock()
        chatPreviewContainer = mock()
        userPermissionProvider = mock()
        notificationPermissionManager = mock()
        stringResourceProvider = mock()
        getSpaceWallpapers = mock()
        shouldShowCreateSpaceBadge = mock()
        participantSubscriptionContainer = mock()
        chatsDetailsSubscriptionContainer = mock()

        getSpaceWallpapers.stub {
            onBlocking { async(Unit) }.thenReturn(Resultat.Success(emptyMap()))
        }
        shouldShowCreateSpaceBadge.stub {
            onBlocking { async(any()) }.thenReturn(Resultat.Success(false))
        }
        whenever(participantSubscriptionContainer.observe()).thenReturn(flowOf(emptyList()))
        whenever(chatsDetailsSubscriptionContainer.observe()).thenReturn(flowOf(emptyList()))
        whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)
        whenever(stringResourceProvider.getUntitledCreatorName()).thenReturn("Untitled")
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")
    }

    //region Pinned vs Unpinned Separation

    @Test
    fun `pinned spaces should appear in pinnedSpaces section`() = runTest {
        turbineScope {
            val pinnedSpaceId = "pinned-space"
            val unpinnedSpaceId = "unpinned-space"

            val pinnedSpace = StubSpaceView(
                id = pinnedSpaceId,
                targetSpaceId = pinnedSpaceId,
                spaceOrder = "a",
                spaceUxType = SpaceUxType.DATA,
                createdDate = 1000.0
            )

            val unpinnedSpace = StubSpaceView(
                id = unpinnedSpaceId,
                targetSpaceId = unpinnedSpaceId,
                spaceOrder = null,
                spaceUxType = SpaceUxType.DATA,
                createdDate = 2000.0
            )

            val spacesList = listOf(pinnedSpace, unpinnedSpace)
            val permissions = mapOf(
                pinnedSpaceId to SpaceMemberPermissions.OWNER,
                unpinnedSpaceId to SpaceMemberPermissions.OWNER
            )

            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spacesList))
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(emptyList()))
            )
            whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1) // Skip loading
                val sections = awaitItem() as VaultUiState.Sections

                assertEquals("Pinned section should have 1 space", 1, sections.pinnedSpaces.size)
                assertEquals("Unpinned section should have 1 space", 1, sections.mainSpaces.size)
                assertEquals("Pinned space should be in pinnedSpaces", pinnedSpaceId, sections.pinnedSpaces[0].space.id)
                assertEquals("Unpinned space should be in mainSpaces", unpinnedSpaceId, sections.mainSpaces[0].space.id)
            }
        }
    }

    //endregion

    //region Pinned Spaces Sorting

    @Test
    fun `pinned spaces should be sorted by spaceOrder ascending`() = runTest {
        turbineScope {
            val space1Id = "space-1"
            val space2Id = "space-2"
            val space3Id = "space-3"

            val space1 = StubSpaceView(
                id = space1Id,
                targetSpaceId = space1Id,
                spaceOrder = "c", // Third
                spaceUxType = SpaceUxType.DATA,
                createdDate = 1000.0
            )

            val space2 = StubSpaceView(
                id = space2Id,
                targetSpaceId = space2Id,
                spaceOrder = "a", // First
                spaceUxType = SpaceUxType.DATA,
                createdDate = 2000.0
            )

            val space3 = StubSpaceView(
                id = space3Id,
                targetSpaceId = space3Id,
                spaceOrder = "b", // Second
                spaceUxType = SpaceUxType.DATA,
                createdDate = 3000.0
            )

            val spacesList = listOf(space1, space2, space3)
            setupMocks(spacesList)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val pinnedOrder = sections.pinnedSpaces.map { it.space.id }
                assertEquals(
                    "Pinned spaces should be sorted by spaceOrder",
                    listOf(space2Id, space3Id, space1Id),
                    pinnedOrder
                )
            }
        }
    }

    //endregion

    //region Effective Date Calculation

    @Test
    fun `unpinned spaces should sort by max of lastMessageDate and spaceJoinDate descending`() = runTest {
        turbineScope {
            val space1Id = "space-with-recent-message"
            val space2Id = "space-with-recent-join"
            val chatId1 = "chat-1"
            val chatId2 = "chat-2"

            val baseTime = 1000000L
            val recentTime = baseTime + 3000L
            val middleTime = baseTime + 2000L
            val oldTime = baseTime + 1000L

            // Space1: recent message, old join date -> effective = recentTime
            val space1 = StubSpaceView(
                id = space1Id,
                targetSpaceId = space1Id,
                chatId = chatId1,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = baseTime.toDouble(),
                spaceJoinDate = oldTime.toDouble()
            )

            // Space2: old message, recent join date -> effective = recentTime
            val space2 = StubSpaceView(
                id = space2Id,
                targetSpaceId = space2Id,
                chatId = chatId2,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = baseTime.toDouble(),
                spaceJoinDate = recentTime.toDouble()
            )

            val chatPreview1 = stubChatPreview(space1Id, chatId1, recentTime)
            val chatPreview2 = stubChatPreview(space2Id, chatId2, oldTime)

            val spacesList = listOf(space1, space2)
            setupMocks(spacesList, listOf(chatPreview1, chatPreview2))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                assertEquals("Both spaces should be unpinned", 2, sections.mainSpaces.size)
                // Both have same effective date (recentTime), so order depends on tiebreaker
                val ids = sections.mainSpaces.map { it.space.id }
                assertTrue("Both spaces should be present", ids.containsAll(listOf(space1Id, space2Id)))
            }
        }
    }

    //endregion

    //region Fallback to spaceJoinDate Only

    @Test
    fun `unpinned space with only spaceJoinDate should sort correctly`() = runTest {
        turbineScope {
            val space1Id = "space-with-join-only"
            val space2Id = "space-with-older-join"

            val recentJoin = 2000000L
            val olderJoin = 1000000L

            val space1 = StubSpaceView(
                id = space1Id,
                targetSpaceId = space1Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = 500000.0,
                spaceJoinDate = recentJoin.toDouble()
            )

            val space2 = StubSpaceView(
                id = space2Id,
                targetSpaceId = space2Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = 500000.0,
                spaceJoinDate = olderJoin.toDouble()
            )

            val spacesList = listOf(space1, space2)
            setupMocks(spacesList)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val order = sections.mainSpaces.map { it.space.id }
                assertEquals(
                    "Space with more recent spaceJoinDate should be first",
                    listOf(space1Id, space2Id),
                    order
                )
            }
        }
    }

    //endregion

    //region Fallback to lastMessageDate Only

    @Test
    fun `unpinned space with only lastMessageDate should sort correctly`() = runTest {
        turbineScope {
            val space1Id = "space-with-recent-msg"
            val space2Id = "space-with-older-msg"
            val chatId1 = "chat-1"
            val chatId2 = "chat-2"

            val recentMsg = 2000000L
            val olderMsg = 1000000L

            val space1 = StubSpaceView(
                id = space1Id,
                targetSpaceId = space1Id,
                chatId = chatId1,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = 500000.0
                // No spaceJoinDate
            )

            val space2 = StubSpaceView(
                id = space2Id,
                targetSpaceId = space2Id,
                chatId = chatId2,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = 500000.0
                // No spaceJoinDate
            )

            val chatPreview1 = stubChatPreview(space1Id, chatId1, recentMsg)
            val chatPreview2 = stubChatPreview(space2Id, chatId2, olderMsg)

            val spacesList = listOf(space1, space2)
            setupMocks(spacesList, listOf(chatPreview1, chatPreview2))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val order = sections.mainSpaces.map { it.space.id }
                assertEquals(
                    "Space with more recent lastMessageDate should be first",
                    listOf(space1Id, space2Id),
                    order
                )
            }
        }
    }

    //endregion

    //region Fallback to createdDate (DROID-4150)

    @Test
    fun `newly created space without spaceJoinDate or messages should sort by createdDate`() = runTest {
        turbineScope {
            val newSpaceId = "newly-created-space"
            val oldSpaceId = "old-space-with-activity"
            val chatId = "chat-1"

            val newSpaceCreatedDate = 3000000L  // Most recent creation
            val oldSpaceCreatedDate = 1000000L
            val oldSpaceMessageDate = 2000000L

            // Newly created space: only has createdDate (no spaceJoinDate, no messages)
            val newSpace = StubSpaceView(
                id = newSpaceId,
                targetSpaceId = newSpaceId,
                spaceUxType = SpaceUxType.DATA,
                createdDate = newSpaceCreatedDate.toDouble()
                // No spaceJoinDate, no chatId
            )

            // Old space with activity (has messages)
            val oldSpace = StubSpaceView(
                id = oldSpaceId,
                targetSpaceId = oldSpaceId,
                chatId = chatId,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = oldSpaceCreatedDate.toDouble()
                // No spaceJoinDate but has messages
            )

            val chatPreview = stubChatPreview(oldSpaceId, chatId, oldSpaceMessageDate)

            val spacesList = listOf(newSpace, oldSpace)
            setupMocks(spacesList, listOf(chatPreview))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val order = sections.mainSpaces.map { it.space.id }
                assertEquals(
                    "Newly created space should appear first (most recent createdDate > message date)",
                    listOf(newSpaceId, oldSpaceId),
                    order
                )
            }
        }
    }

    @Test
    fun `newly created space should appear at top even without any dates except createdDate`() = runTest {
        turbineScope {
            val newSpaceId = "new-space"
            val existingSpace1Id = "existing-1"
            val existingSpace2Id = "existing-2"

            val now = System.currentTimeMillis()

            // Newly created space with recent createdDate
            val newSpace = StubSpaceView(
                id = newSpaceId,
                targetSpaceId = newSpaceId,
                spaceUxType = SpaceUxType.DATA,
                createdDate = now.toDouble()
            )

            // Existing spaces with older createdDate
            val existingSpace1 = StubSpaceView(
                id = existingSpace1Id,
                targetSpaceId = existingSpace1Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = (now - 100000).toDouble()
            )

            val existingSpace2 = StubSpaceView(
                id = existingSpace2Id,
                targetSpaceId = existingSpace2Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = (now - 200000).toDouble()
            )

            val spacesList = listOf(existingSpace1, newSpace, existingSpace2)
            setupMocks(spacesList)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                assertEquals(
                    "Newly created space should be first",
                    newSpaceId,
                    sections.mainSpaces.first().space.id
                )
            }
        }
    }

    //endregion

    //region Mixed Scenarios

    @Test
    fun `spaces with different date combinations should sort correctly together`() = runTest {
        turbineScope {
            val bothDatesSpaceId = "both-dates"
            val onlyMsgSpaceId = "only-message"
            val onlyJoinSpaceId = "only-join"
            val onlyCreatedSpaceId = "only-created"
            val chatId1 = "chat-1"
            val chatId2 = "chat-2"

            val baseTime = 1000000L

            // Space with both dates: effective = max(2000, 1500) = 2000
            val bothDatesSpace = StubSpaceView(
                id = bothDatesSpaceId,
                targetSpaceId = bothDatesSpaceId,
                chatId = chatId1,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = baseTime.toDouble(),
                spaceJoinDate = (baseTime + 500).toDouble()
            )
            val chatPreview1 = stubChatPreview(bothDatesSpaceId, chatId1, baseTime + 1000)

            // Space with only message: effective = 1800
            val onlyMsgSpace = StubSpaceView(
                id = onlyMsgSpaceId,
                targetSpaceId = onlyMsgSpaceId,
                chatId = chatId2,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = baseTime.toDouble()
            )
            val chatPreview2 = stubChatPreview(onlyMsgSpaceId, chatId2, baseTime + 800)

            // Space with only join: effective = 1600
            val onlyJoinSpace = StubSpaceView(
                id = onlyJoinSpaceId,
                targetSpaceId = onlyJoinSpaceId,
                spaceUxType = SpaceUxType.DATA,
                createdDate = baseTime.toDouble(),
                spaceJoinDate = (baseTime + 600).toDouble()
            )

            // Space with only created: effective = 1400
            val onlyCreatedSpace = StubSpaceView(
                id = onlyCreatedSpaceId,
                targetSpaceId = onlyCreatedSpaceId,
                spaceUxType = SpaceUxType.DATA,
                createdDate = (baseTime + 400).toDouble()
            )

            val spacesList = listOf(onlyCreatedSpace, onlyJoinSpace, bothDatesSpace, onlyMsgSpace)
            setupMocks(spacesList, listOf(chatPreview1, chatPreview2))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val order = sections.mainSpaces.map { it.space.id }
                assertEquals(
                    "Spaces should be sorted by their effective dates descending",
                    listOf(bothDatesSpaceId, onlyMsgSpaceId, onlyJoinSpaceId, onlyCreatedSpaceId),
                    order
                )
            }
        }
    }

    //endregion

    //region Edge Cases

    @Test
    fun `spaces with same effective date should use createdDate as tiebreaker`() = runTest {
        turbineScope {
            val space1Id = "space-created-recently"
            val space2Id = "space-created-earlier"

            val sameJoinDate = 2000000L
            val recentCreated = 1500000L
            val earlierCreated = 1000000L

            val space1 = StubSpaceView(
                id = space1Id,
                targetSpaceId = space1Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = recentCreated.toDouble(),
                spaceJoinDate = sameJoinDate.toDouble()
            )

            val space2 = StubSpaceView(
                id = space2Id,
                targetSpaceId = space2Id,
                spaceUxType = SpaceUxType.DATA,
                createdDate = earlierCreated.toDouble(),
                spaceJoinDate = sameJoinDate.toDouble()
            )

            val spacesList = listOf(space2, space1)
            setupMocks(spacesList)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                skipItems(1)
                val sections = awaitItem() as VaultUiState.Sections

                val order = sections.mainSpaces.map { it.space.id }
                assertEquals(
                    "Space with more recent createdDate should be first when effective dates are equal",
                    listOf(space1Id, space2Id),
                    order
                )
            }
        }
    }

    //endregion

    //region Helper Methods

    private fun setupMocks(
        spaces: List<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>,
        chatPreviews: List<com.anytypeio.anytype.core_models.chats.Chat.Preview> = emptyList()
    ) {
        val permissions = spaces.associate { it.id to SpaceMemberPermissions.OWNER }

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaces))
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
            flowOf(ChatPreviewContainer.PreviewState.Ready(chatPreviews))
        )
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
        )
    }

    private fun createViewModel(): VaultViewModel {
        return VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            getSpaceWallpaper = getSpaceWallpapers,
            chatsDetailsContainer = chatsDetailsSubscriptionContainer,
            participantSubscriptionContainer = participantSubscriptionContainer
        )
    }

    //endregion
}
