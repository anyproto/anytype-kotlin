package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.AppShutdown
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.ObserveShowSpacesIntroduction
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugRunProfiler
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.vault.SetSpacesIntroductionShown
import com.anytypeio.anytype.domain.wallpaper.ObserveSpaceWallpaper
import com.anytypeio.anytype.domain.workspace.DeepLinkToObjectDelegate
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.notifications.NotificationActionDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.ui.main.MainActivity
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicatorImpl
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope

@Subcomponent(
    modules = [MainEntryModule::class]
)
@PerScreen
interface MainEntrySubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MainEntrySubComponent
        fun module(module: MainEntryModule): Builder
    }

    fun inject(activity: MainActivity)
}

@Module
object MainEntryModule {

    @Provides
    fun provideUnqualifiedScope(
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): CoroutineScope = scope

    @JvmStatic
    @PerScreen
    @Provides
    fun provideMainViewModelFactory(
        resumeAccount: ResumeAccount,
        analytics: Analytics,
        interceptAccountStatus: InterceptAccountStatus,
        logout: Logout,
        checkAuthorizationStatus: CheckAuthorizationStatus,
        configStorage: ConfigStorage,
        localeProvider: LocaleProvider,
        notificationsProvider: NotificationsProvider,
        notificator: SystemNotificationService,
        notificationActionDelegate: NotificationActionDelegate,
        deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
        awaitAccountStartManager: AwaitAccountStartManager,
        membershipProvider: MembershipProvider,
        globalSubscriptionManager: GlobalSubscriptionManager,
        spaceInviteResolver: SpaceInviteResolver,
        spaceManager: SpaceManager,
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        pendingIntentStore: PendingIntentStore,
        observeSpaceWallpaper: ObserveSpaceWallpaper,
        urlBuilder: UrlBuilder,
        appShutdown: AppShutdown,
        scope: CoroutineScope,
        observeShowSpacesIntroduction: ObserveShowSpacesIntroduction,
        setSpacesIntroductionShown: SetSpacesIntroductionShown,
        appInfo: com.anytypeio.anytype.core_utils.tools.AppInfo,
        chatPreviewContainer: ChatPreviewContainer,
        chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer,
        participantSubscriptionContainer: ParticipantSubscriptionContainer,
        userSettingsRepository: UserSettingsRepository,
        debugRunProfiler: DebugRunProfiler
    ): MainViewModelFactory = MainViewModelFactory(
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
        spaceViews = spaceViewSubscriptionContainer,
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
        participantSubscriptionContainer = participantSubscriptionContainer,
        userSettingsRepository = userSettingsRepository,
        debugRunProfiler = debugRunProfiler
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideResumeAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        configStorage: ConfigStorage,
        initialParamsProvider: InitialParamsProvider,
        awaitAccountStartManager: AwaitAccountStartManager,
        spaceManager: SpaceManager,
        settingsRepository: UserSettingsRepository
    ): ResumeAccount = ResumeAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        configStorage = configStorage,
        initialParamsProvider = initialParamsProvider,
        awaitAccountStartManager = awaitAccountStartManager,
        spaceManager = spaceManager,
        settings = settingsRepository
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetupThemeUseCase(
        repo: UserSettingsRepository
    ): GetTheme = GetTheme(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideThemeApplicator(): ThemeApplicator = ThemeApplicatorImpl()

    @JvmStatic
    @PerScreen
    @Provides
    fun provideInterceptAccountStatus(
        channel: AccountStatusChannel,
        dispatchers: AppCoroutineDispatchers
    ): InterceptAccountStatus = InterceptAccountStatus(
        channel = channel,
        dispatchers = dispatchers
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLogoutUseCase(
        repo: AuthRepository,
        provider: ConfigStorage,
        user: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        awaitAccountStartManager: AwaitAccountStartManager
    ): Logout = Logout(
        repo = repo,
        user = user,
        config = provider,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        awaitAccountStartManager = awaitAccountStartManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCheckAuthStatus(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): CheckAuthorizationStatus = CheckAuthorizationStatus(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideNotificationActionDelegate(
        default: NotificationActionDelegate.Default
    ) : NotificationActionDelegate = default

    @JvmStatic
    @PerScreen
    @Provides
    fun provideDeepLinkToObjectDelegate(
        default: DeepLinkToObjectDelegate.Default
    ): DeepLinkToObjectDelegate = default

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSpaceInviteResolver(): SpaceInviteResolver = DefaultSpaceInviteResolver

    @JvmStatic
    @PerScreen
    @Provides
    fun provideDebugRunProfiler(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): DebugRunProfiler = DebugRunProfiler(repo, dispatchers)
}