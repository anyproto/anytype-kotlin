package com.anytypeio.anytype.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.AppShutdown
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.wallpaper.ObserveSpaceWallpaper
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationActionDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import com.anytypeio.anytype.domain.config.ObserveShowSpacesIntroduction
import com.anytypeio.anytype.domain.vault.SetSpacesIntroductionShown
import com.anytypeio.anytype.core_utils.tools.AppInfo

class MainViewModelFactory @Inject constructor(
    private val resumeAccount: ResumeAccount,
    private val analytics: Analytics,
    private val interceptAccountStatus: InterceptAccountStatus,
    private val logout: Logout,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val configStorage: ConfigStorage,
    private val localeProvider: LocaleProvider,
    private val notificationsProvider: NotificationsProvider,
    private val notificator: SystemNotificationService,
    private val notificationActionDelegate: NotificationActionDelegate,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val membershipProvider: MembershipProvider,
    private val globalSubscriptionManager: GlobalSubscriptionManager,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val spaceManager: SpaceManager,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val pendingIntentStore: PendingIntentStore,
    private val observeSpaceWallpaper: ObserveSpaceWallpaper,
    private val urlBuilder: UrlBuilder,
    private val appShutdown: AppShutdown,
    private val scope: CoroutineScope,
    private val observeShowSpacesIntroduction: ObserveShowSpacesIntroduction,
    private val setSpacesIntroductionShown: SetSpacesIntroductionShown,
    private val appInfo: AppInfo,
    private val chatPreviewContainer: ChatPreviewContainer,
    private val chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer,
    private val participantSubscriptionContainer: ParticipantSubscriptionContainer
    ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T = MainViewModel(
        resumeAccount = resumeAccount,
        analytics = analytics,
        interceptAccountStatus = interceptAccountStatus,
        logout = logout,
        checkAuthorizationStatus = checkAuthorizationStatus,
        configStorage = configStorage,
        localeProvider = localeProvider,
        notificationsProvider = notificationsProvider,
        notificator = notificator,
        notificationActionDelegate = notificationActionDelegate,
        deepLinkToObjectDelegate = deepLinkToObjectDelegate,
        awaitAccountStartManager = awaitAccountStartManager,
        membershipProvider = membershipProvider,
        globalSubscriptionManager = globalSubscriptionManager,
        spaceInviteResolver = spaceInviteResolver,
        spaceManager = spaceManager,
        spaceViews = spaceViews,
        pendingIntentStore = pendingIntentStore,
        observeSpaceWallpaper = observeSpaceWallpaper,
        urlBuilder = urlBuilder,
        appShutdown = appShutdown,
        scope = scope,
        observeShowSpacesIntroduction = observeShowSpacesIntroduction,
        setSpacesIntroductionShown = setSpacesIntroductionShown,
        appInfo = appInfo,
        chatPreviewContainer = chatPreviewContainer,
        chatsDetailsSubscriptionContainer = chatsDetailsSubscriptionContainer,
        participantSubscriptionContainer = participantSubscriptionContainer
    ) as T
}