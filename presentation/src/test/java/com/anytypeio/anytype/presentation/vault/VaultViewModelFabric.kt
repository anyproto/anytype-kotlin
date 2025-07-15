package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
import com.anytypeio.anytype.domain.vault.ReorderPinnedSpaces
import com.anytypeio.anytype.domain.vault.SetSpaceOrder
import com.anytypeio.anytype.domain.vault.UnpinSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
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
        profileContainer: ProfileSubscriptionManager = mock(),
        chatPreviewContainer: ChatPreviewContainer = mock(),
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
        reorderPinnedSpaces: ReorderPinnedSpaces = mock(),
        setSpaceOrder: SetSpaceOrder = mock()
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
        reorderPinnedSpaces = reorderPinnedSpaces,
        setSpaceOrder = setSpaceOrder
    )
} 