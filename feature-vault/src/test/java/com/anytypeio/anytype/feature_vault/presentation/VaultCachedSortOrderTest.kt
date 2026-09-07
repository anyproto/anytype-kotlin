package com.anytypeio.anytype.feature_vault.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.stubChatPreview
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManager
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.vault.ShouldShowCreateSpaceBadge
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.feature_vault.util.DefaultCoroutineTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
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
 * The vault used to block its whole first render on
 * Chat.SubscribeToMessagePreviews, which was measured at 8.6s on a cold start with
 * 79 spaces. It now paints from the space list alone and orders the main section
 * with last-message dates cached from the previous session (DROID-4589).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultCachedSortOrderTest {

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

    @Test
    fun `renders before previews arrive, ordered by cached last-message dates`() = runTest {
        turbineScope {
            // "chatty" was created first but has the most recent message; "quiet" was
            // created later. Creation order and message-recency order disagree, so the
            // assertion can only pass if the cached sort keys are used.
            val chatty = StubSpaceView(
                id = CHATTY,
                targetSpaceId = CHATTY,
                spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = 1_000.0
            )
            val quiet = StubSpaceView(
                id = QUIET,
                targetSpaceId = QUIET,
                spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = 3_000.0
            )

            setupSpaces(listOf(quiet, chatty))
            // Previews never become Ready: this is the cold-start window.
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Loading)
            )

            val viewModel = createViewModel(
                cachedSortKeys = mapOf(CHATTY to 9_000L, QUIET to 2_000L)
            )

            viewModel.uiState.test {
                skipItems(1) // Loading
                val sections = awaitItem() as VaultUiState.Sections
                assertEquals(
                    "Vault must render without waiting for chat previews",
                    2,
                    sections.mainSpaces.size
                )
                assertEquals(
                    "Space with the newest cached message must come first",
                    listOf(CHATTY, QUIET),
                    sections.mainSpaces.map { it.space.id }
                )
            }
        }
    }

    @Test
    fun `falls back to creation date when nothing is cached`() = runTest {
        turbineScope {
            val older = StubSpaceView(
                id = "older",
                targetSpaceId = "older",
                spaceOrder = null,
                spaceUxType = SpaceUxType.DATA,
                createdDate = 1_000.0
            )
            val newer = StubSpaceView(
                id = "newer",
                targetSpaceId = "newer",
                spaceOrder = null,
                spaceUxType = SpaceUxType.DATA,
                createdDate = 5_000.0
            )

            setupSpaces(listOf(older, newer))
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Loading)
            )

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                skipItems(1) // Loading
                val sections = awaitItem() as VaultUiState.Sections
                assertEquals(
                    listOf("newer", "older"),
                    sections.mainSpaces.map { it.space.id }
                )
            }
        }
    }

    @Test
    fun `stays on Loading while the space list is still empty`() = runTest {
        turbineScope {
            // Both spaceFlow and the space container are seeded with an empty list.
            // Publishing that seed painted an empty vault for up to ~950ms on device.
            val spaces = MutableStateFlow<List<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>>(emptyList())
            val permissions = MutableStateFlow<Map<String, SpaceMemberPermissions>>(emptyMap())
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(spaces)
            whenever(userPermissionProvider.all()).thenReturn(permissions)
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Loading)
            )

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                skipItems(1) // Loading
                spaces.value = listOf(
                    StubSpaceView(
                        id = CHATTY, targetSpaceId = CHATTY, spaceOrder = null,
                        spaceUxType = SpaceUxType.CHAT, createdDate = 1_000.0
                    )
                )
                permissions.value = mapOf(CHATTY to SpaceMemberPermissions.OWNER)
                val sections = awaitItem() as VaultUiState.Sections
                assertEquals(
                    "The first painted frame must never be an empty list",
                    1,
                    sections.mainSpaces.size
                )
            }
        }
    }

    @Test
    fun `an account with no spaces resolves off the spinner once previews are ready`() = runTest {
        turbineScope {
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(emptyList()))
            whenever(userPermissionProvider.all()).thenReturn(flowOf(emptyMap()))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(emptyList()))
            )

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                skipItems(1) // Loading
                val sections = awaitItem() as VaultUiState.Sections
                assertEquals(0, sections.mainSpaces.size)
                assertEquals(0, sections.pinnedSpaces.size)
            }
        }
    }

    @Test
    fun `keeps previews when the container drops back to Loading mid-session`() = runTest {
        turbineScope {
            // MainViewModel.onRestore() restarts ChatPreviewContainer on a plain
            // configuration change, which resets it to Loading while this ViewModel
            // survives. Treating that as "no previews" blanked every chat row.
            val space = StubSpaceView(
                id = CHATTY,
                targetSpaceId = CHATTY,
                spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT,
                createdDate = 1_000.0
            )
            val permissions = MutableStateFlow(
                mapOf(CHATTY to SpaceMemberPermissions.OWNER)
            )
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(listOf(space)))
            whenever(userPermissionProvider.all()).thenReturn(permissions)
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )

            val previews = MutableStateFlow<ChatPreviewContainer.PreviewState>(
                ChatPreviewContainer.PreviewState.Ready(
                    listOf(stubChatPreview(spaceId = CHATTY, chatId = "chat", lastMessageDate = 9_000L))
                )
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(previews)

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                skipItems(1) // Loading
                val enriched = awaitItem() as VaultUiState.Sections
                val before = enriched.mainSpaces.first() as VaultSpaceView.DataSpaceWithChat
                assertEquals("Hello, user1", before.messageText)

                // Drop to Loading, then force a rebuild from another upstream so the
                // pipeline definitely re-emits and the assertion cannot pass by timing.
                previews.value = ChatPreviewContainer.PreviewState.Loading
                permissions.value = mapOf(CHATTY to SpaceMemberPermissions.READER)
                val after = awaitItem() as VaultUiState.Sections
                val row = after.mainSpaces.first() as VaultSpaceView.DataSpaceWithChat
                assertEquals(
                    "Rows must keep their message text across a Loading blip",
                    "Hello, user1",
                    row.messageText
                )
            }
        }
    }

    @Test
    fun `an empty preview list does not wipe the cached order`() = runTest {
        turbineScope {
            // ChatPreviewContainer swallows an RPC failure and publishes
            // Ready(emptyList()), indistinguishable from "account has no chats".
            val chatty = StubSpaceView(
                id = CHATTY, targetSpaceId = CHATTY, spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT, createdDate = 1_000.0
            )
            val quiet = StubSpaceView(
                id = QUIET, targetSpaceId = QUIET, spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT, createdDate = 3_000.0
            )
            val permissions = MutableStateFlow(
                mapOf(CHATTY to SpaceMemberPermissions.OWNER, QUIET to SpaceMemberPermissions.OWNER)
            )
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(listOf(quiet, chatty)))
            whenever(userPermissionProvider.all()).thenReturn(permissions)
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(emptyList()))
            )

            val viewModel = createViewModel(
                cachedSortKeys = mapOf(CHATTY to 9_000L, QUIET to 2_000L)
            )

            viewModel.uiState.test {
                skipItems(1) // Loading
                val first = awaitItem() as VaultUiState.Sections
                assertEquals(
                    "A failed previews RPC must not reshuffle the vault",
                    listOf(CHATTY, QUIET),
                    first.mainSpaces.map { it.space.id }
                )
                // Let the 1s persist debounce fire, then force a rebuild: if the empty
                // map were allowed through it would have replaced the in-memory
                // fallback and this second frame would fall back to createdDate.
                awaitRealTime(millis = 1_400)
                permissions.value = mapOf(
                    CHATTY to SpaceMemberPermissions.READER,
                    QUIET to SpaceMemberPermissions.OWNER
                )
                val second = awaitItem() as VaultUiState.Sections
                assertEquals(
                    "The cached order must survive an empty preview list",
                    listOf(CHATTY, QUIET),
                    second.mainSpaces.map { it.space.id }
                )
            }
        }
    }

    @Test
    fun `a preview with no message stops using the stale cached date`() = runTest {
        turbineScope {
            // The last message in CHATTY was deleted, so its preview carries no
            // message. The cached date must not keep it pinned near the top.
            val chatty = StubSpaceView(
                id = CHATTY, targetSpaceId = CHATTY, spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT, createdDate = 1_000.0
            )
            val quiet = StubSpaceView(
                id = QUIET, targetSpaceId = QUIET, spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT, createdDate = 3_000.0
            )
            setupSpaces(listOf(chatty, quiet))

            val emptied = stubChatPreview(
                spaceId = CHATTY, chatId = "chat", lastMessageDate = 0L
            ).copy(message = null)
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Ready(listOf(emptied)))
            )

            val viewModel = createViewModel(
                cachedSortKeys = mapOf(CHATTY to 9_000L)
            )

            viewModel.uiState.test {
                skipItems(1) // Loading
                val sections = awaitItem() as VaultUiState.Sections
                assertEquals(
                    "CHATTY has a preview now, so it sorts by createdDate (1000) below QUIET (3000)",
                    listOf(QUIET, CHATTY),
                    sections.mainSpaces.map { it.space.id }
                )
            }
        }
    }

    @Test
    fun `does not render before permissions are known`() = runTest {
        turbineScope {
            // isOwner drives whether the space menu offers "Delete space" or "Leave
            // space", and both confirmations route to the same Space.Delete call. If
            // the vault paints before permissions land, an owner is shown the leave
            // wording for an action that destroys the space for every member.
            // permissionsFlow is derived from the space list, so it always trails it.
            val space = StubSpaceView(
                id = CHATTY, targetSpaceId = CHATTY, spaceOrder = null,
                spaceUxType = SpaceUxType.CHAT, createdDate = 1_000.0
            )
            val permissions = MutableStateFlow<Map<String, SpaceMemberPermissions>>(emptyMap())
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(listOf(space)))
            whenever(userPermissionProvider.all()).thenReturn(permissions)
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Loading)
            )

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                skipItems(1) // Loading
                // Real elapsed time, not virtual: the rebuild pipeline runs on
                // Dispatchers.Default, so without this the unguarded ViewModel has no
                // chance to paint its isOwner=false frame and the test would pass
                // whether or not the gate is present.
                awaitRealTime()
                permissions.value = mapOf(CHATTY to SpaceMemberPermissions.OWNER)
                val sections = awaitItem() as VaultUiState.Sections
                assertTrue(
                    "The first painted frame must already know the user owns this space",
                    sections.mainSpaces.first().isOwner
                )
            }
        }
    }

    /**
     * Blocks for real wall-clock time. `runTest` fast-forwards `delay`, but the vault
     * rebuild and persist pipelines both run on `Dispatchers.Default`, so a negative
     * assertion ("no frame was painted yet") needs genuine elapsed time to mean
     * anything.
     */
    private suspend fun awaitRealTime(millis: Long = 600) {
        withContext(Dispatchers.IO) { Thread.sleep(millis) }
    }

    private fun setupSpaces(
        spaces: List<com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView>
    ) {
        val permissions = spaces.associate { it.id to SpaceMemberPermissions.OWNER }
        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaces))
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(
            MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
        )
    }

    private fun createViewModel(cachedSortKeys: Map<String, Long>): VaultViewModel {
        val settings: UserSettingsRepository = mock {
            on { observeCompactModeEnabled() }.thenReturn(flowOf(false))
            on { observeQuickCaptureEnabled() }.thenReturn(flowOf(false))
            onBlocking { getVaultSortKeys() }.thenReturn(cachedSortKeys)
        }
        return VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider,
            getSpaceWallpaper = getSpaceWallpapers,
            chatsDetailsContainer = chatsDetailsSubscriptionContainer,
            participantSubscriptionContainer = participantSubscriptionContainer,
            userSettingsRepository = settings
        )
    }

    companion object {
        private const val CHATTY = "chatty-space"
        private const val QUIET = "quiet-space"
    }
}
