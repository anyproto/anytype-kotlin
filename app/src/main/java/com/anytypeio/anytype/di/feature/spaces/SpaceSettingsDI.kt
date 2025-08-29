package com.anytypeio.anytype.di.feature.spaces

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.device.share.debug.DebugSpaceDeviceFileContentSaver
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugSpaceContentSaver
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.SetSpaceNotificationMode
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.wallpaper.SetWallpaper
import com.anytypeio.anytype.domain.wallpaper.WallpaperStore
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.device.DefaultGradientCollectionProvider
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.settings.space.SpaceSettingsFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [SpaceSettingsDependencies::class],
    modules = [
        SpaceSettingsModule::class,
        SpaceSettingsModule.Bindings::class
    ]
)
@PerScreen
interface SpaceSettingsComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: SpaceSettingsViewModel.VmParams,
            dependencies: SpaceSettingsDependencies
        ) : SpaceSettingsComponent
    }

    fun inject(fragment: SpaceSettingsFragment)
}

@Module
object SpaceSettingsModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetAccount = GetAccount(repo = repo, dispatcher = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetSpaceNotificationModeUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetSpaceNotificationMode = SetSpaceNotificationMode(repository, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetWallpaper(
        repo: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ) : SetWallpaper = SetWallpaper(
        repo = repo,
        store = WallpaperStore.Default,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetCoverGradientCollectionUseCase(
    ): GetCoverGradientCollection = GetCoverGradientCollection(DefaultGradientCollectionProvider)

    @Module
    interface Bindings {

        @PerScreen
        @Binds
        fun bindSpaceDebugDeviceSharer(
            saver: DebugSpaceDeviceFileContentSaver
        ): DebugSpaceContentSaver
        @PerScreen
        @Binds
        fun factory(factory: SpaceSettingsViewModel.Factory): ViewModelProvider.Factory
    }
}

interface SpaceSettingsDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun auth(): AuthRepository
    fun appActions(): AppActionManager
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun settings(): UserSettingsRepository
    fun urlBuilder(): UrlBuilder
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
    fun container(): StorelessSubscriptionContainer
    fun context(): Context
    fun userPermission(): UserPermissionProvider
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun activeSpaceMemberSubscriptionContainer(): ActiveSpaceMemberSubscriptionContainer
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
    fun profileContainer(): ProfileSubscriptionManager
    fun uriFileProvider(): UriFileProvider
    fun notificationsPermissionManager(): NotificationPermissionManager
    fun deviceTokenStoreService(): DeviceTokenStoringService
    fun clipboard() : Clipboard
    fun logger(): Logger
    fun spaceInviteLinkStore() : SpaceInviteLinkStore
}