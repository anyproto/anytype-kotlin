package com.anytypeio.anytype.feature_vault.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.StubSpaceView
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
            whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(emptyList()))
            whenever(userPermissionProvider.all()).thenReturn(flowOf(emptyMap()))
            whenever(notificationPermissionManager.permissionState()).thenReturn(
                MutableStateFlow(NotificationPermissionManagerImpl.PermissionState.Granted)
            )
            whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(
                flowOf(ChatPreviewContainer.PreviewState.Loading)
            )

            val viewModel = createViewModel(cachedSortKeys = emptyMap())

            viewModel.uiState.test {
                assertEquals(VaultUiState.Loading, awaitItem())
                expectNoEvents()
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
