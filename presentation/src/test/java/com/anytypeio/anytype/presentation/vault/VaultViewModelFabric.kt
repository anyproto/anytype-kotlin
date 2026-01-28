package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.tools.AppInfo
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SearchOneToOneChatByIdentity
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.SetSpaceNotificationMode
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.vault.SetCreateSpaceBadgeSeen
import com.anytypeio.anytype.domain.vault.SetSpaceOrder
import com.anytypeio.anytype.domain.vault.ShouldShowCreateSpaceBadge
import com.anytypeio.anytype.domain.vault.UnpinSpace
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

object VaultViewModelFabric {
    fun create(
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer = mock(),
        urlBuilder: UrlBuilder = mock(),
        spaceManager: SpaceManager = mock(),
        saveCurrentSpace: SaveCurrentSpace = mock(),
        analytics: Analytics = mock(),
        deepLinkToObjectDelegate: DeepLinkToObjectDelegate = mock(),
        appActionManager: AppActionManager = mock(),
        spaceInviteResolver: SpaceInviteResolver = mock(),
        profileContainer: ProfileSubscriptionManager = mock {
            on { observe() }.thenReturn(flowOf(ObjectWrapper.Basic(emptyMap())))
        },
        chatPreviewContainer: ChatPreviewContainer = mock(),
        chatsDetailsContainer: ChatsDetailsSubscriptionContainer = mock {
            on { observe() }.thenReturn(flowOf(emptyList()))
        },
        pendingIntentStore: PendingIntentStore = mock(),
        stringResourceProvider: StringResourceProvider = mock(),
        dateProvider: DateProvider = mock(),
        fieldParser: FieldParser = mock(),
        storeOfObjectTypes: StoreOfObjectTypes = mock(),
        setSpaceNotificationMode: SetSpaceNotificationMode = mock(),
        deleteSpace: DeleteSpace = mock(),
        userPermissionProvider: UserPermissionProvider = mock(),
        notificationPermissionManager: NotificationPermissionManager = mock(),
        unpinSpace: UnpinSpace = mock(),
        setSpaceOrder: SetSpaceOrder = mock(),
        getSpaceWallpaper: GetSpaceWallpapers = mock(),
        searchOneToOneChatByIdentity: SearchOneToOneChatByIdentity = mock(),
        shouldShowCreateSpaceBadge: ShouldShowCreateSpaceBadge = mock {
            on { runBlocking { async(any()) } }.thenReturn(com.anytypeio.anytype.domain.base.Resultat.Success(false))
        },
        setCreateSpaceBadgeSeen: SetCreateSpaceBadgeSeen = mock(),
        appInfo: AppInfo = mock {
            on { versionName }.thenReturn("1.0.0-test")
        },
        participantSubscriptionContainer: ParticipantSubscriptionContainer = mock(),
        configStorage: ConfigStorage = mock {
            on { getOrNull() }.thenReturn(null)
        }
    ): VaultViewModel = VaultViewModel(
        spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
        urlBuilder = urlBuilder,
        spaceManager = spaceManager,
        saveCurrentSpace = saveCurrentSpace,
        analytics = analytics,
        deepLinkToObjectDelegate = deepLinkToObjectDelegate,
        appActionManager = appActionManager,
        spaceInviteResolver = spaceInviteResolver,
        profileContainer = profileContainer,
        chatPreviewContainer = chatPreviewContainer,
        chatsDetailsContainer = chatsDetailsContainer,
        pendingIntentStore = pendingIntentStore,
        stringResourceProvider = stringResourceProvider,
        dateProvider = dateProvider,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes,
        setSpaceNotificationMode = setSpaceNotificationMode,
        deleteSpace = deleteSpace,
        userPermissionProvider = userPermissionProvider,
        notificationPermissionManager = notificationPermissionManager,
        unpinSpace = unpinSpace,
        setSpaceOrder = setSpaceOrder,
        getSpaceWallpapers = getSpaceWallpaper,
        shouldShowCreateSpaceBadge = shouldShowCreateSpaceBadge,
        setCreateSpaceBadgeSeen = setCreateSpaceBadgeSeen,
        appInfo = appInfo,
        participantContainer = participantSubscriptionContainer,
        searchOneToOneChatByIdentity = searchOneToOneChatByIdentity,
        createSpace = mock(),
        deepLinkResolver = mock(),
        configStorage = configStorage
    )
} 