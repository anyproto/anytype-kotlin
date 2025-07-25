package com.anytypeio.anytype.di.feature.home

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.bin.EmptyBin
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.spaces.ClearLastOpenedSpace
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.Unsubscriber
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.DefaultObjectViewReducer
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Component(
    dependencies = [HomeScreenDependencies::class],
    modules = [
        HomeScreenModule::class,
        HomeScreenModule.Declarations::class
    ]
)
@PerScreen
interface HomeScreenComponent {
    @Component.Factory
    interface Factory {
        fun create(dependencies: HomeScreenDependencies): HomeScreenComponent
    }

    fun inject(fragment: HomeScreenFragment)
}

@Module
object HomeScreenModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun openObject(
        repo: BlockRepository,
        settings: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): OpenObject = OpenObject(
        repo = repo,
        dispatchers = dispatchers,
        settings = settings
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun closeObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CloseObject = CloseObject(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun setObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun move(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) : Move = Move(
        repo = repo,
        appCoroutineDispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun emptyBin(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager
    ) : EmptyBin = EmptyBin(
        repo = repo,
        dispatchers = dispatchers,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        settings: UserSettingsRepository,
    ): GetObject = GetObject(
        repo = repo,
        dispatchers = dispatchers,
        settings = settings
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun createObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        getDefaultEditorType: GetDefaultObjectType,
        spaceManager: SpaceManager
    ): CreateObject = CreateObject(
        repo = repo,
        dispatchers = dispatchers,
        getDefaultObjectType = getDefaultEditorType
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) : GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getTemplates(
        repo: BlockRepository,
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers,
    ) : GetTemplates = GetTemplates(
        repo = repo,
        spaceManager = spaceManager,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun interceptEvents(channel: EventChannel): InterceptEvents = InterceptEvents(
        context = Dispatchers.IO,
        channel = channel
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @PerScreen
    fun coverHashProvider() : CoverImageHashProvider = DefaultCoverImageHashProvider()

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSpaceInviteResolver(): SpaceInviteResolver = DefaultSpaceInviteResolver

    @JvmStatic
    @PerScreen
    @Provides
    fun provideExitToVaultDelegate(
        spaceManager: SpaceManager,
        clearLastOpenedSpace: ClearLastOpenedSpace
    ) : ExitToVaultDelegate = ExitToVaultDelegate.Default(
        spaceManager = spaceManager,
        clearLastOpenedSpace = clearLastOpenedSpace
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun factory(factory: HomeScreenViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun container(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer

        @PerScreen
        @Binds
        fun holder(holder: WidgetActiveViewStateHolder.Impl): WidgetActiveViewStateHolder

        @PerScreen
        @Binds
        fun unsubscriber(impl: Unsubscriber.Impl) : Unsubscriber

        @PerScreen
        @Binds
        fun collapsedWidgetStateHolder(
            holder: CollapsedWidgetStateHolder.Impl
        ): CollapsedWidgetStateHolder

        @PerScreen
        @Binds
        fun widgetSessionStateHolder(
            holder: WidgetSessionStateHolder.Impl
        ): WidgetSessionStateHolder

        @PerScreen
        @Binds
        fun objectWatcherReducer(
            default: DefaultObjectViewReducer
        ): ObjectWatcher.Reducer

        @PerScreen
        @Binds
        fun deepLinkToObjectDelegate(
            default: DeepLinkToObjectDelegate.Default
        ) : DeepLinkToObjectDelegate
    }
}

interface HomeScreenDependencies : ComponentDependencies {
    fun dispatcherWidgets(): Dispatcher<WidgetDispatchEvent>
    fun dispatcherPayload(): Dispatcher<Payload>
    fun blockRepo(): BlockRepository
    fun authRepo(): AuthRepository
    fun userRepo(): UserSettingsRepository
    fun config(): ConfigStorage
    fun urlBuilder(): UrlBuilder
    fun objectStore(): ObjectStore
    fun subscriptionEventChannel(): SubscriptionEventChannel
    fun analytics(): Analytics
    fun eventChannel(): EventChannel
    fun dispatchers(): AppCoroutineDispatchers
    fun appActionManager(): AppActionManager
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun dateProvider(): DateProvider
    fun logger(): Logger
    fun spaceManager(): SpaceManager
    fun userPermissionProvider(): UserPermissionProvider
    fun notificationChannel(): NotificationsChannel
    fun activeSpaceMembers() : ActiveSpaceMemberSubscriptionContainer
    fun analyticSpaceHelperDelegate(): AnalyticSpaceHelperDelegate
    fun storeOfRelations(): StoreOfRelations
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun featureToggles(): FeatureToggles
    fun payloadDelegator(): PayloadDelegator
    fun fieldParser(): FieldParser
    fun notificationPermissionManager(): NotificationPermissionManager
    fun provideChatEventChannel(): ChatEventChannel
    fun provideChatPreviewContainer(): ChatPreviewContainer
}