package com.anytypeio.anytype.di.feature.vault

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
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
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.core_utils.tools.AppInfo
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.vault.VaultViewModelFactory
import com.anytypeio.anytype.ui.vault.VaultFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope

@Component(
    dependencies = [VaultComponentDependencies::class],
    modules = [
        VaultModule::class,
        VaultModule.Declarations::class
    ]
)
@PerScreen
interface VaultComponent {

    fun inject(fragment: VaultFragment)

    @Component.Factory
    interface Factory {
        fun create(dependencies: VaultComponentDependencies): VaultComponent
    }
}

@Module
object VaultModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: VaultViewModelFactory
        ): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun deepLinkToObjectDelegate(
            default: DeepLinkToObjectDelegate.Default
        ): DeepLinkToObjectDelegate
    }

    @PerScreen
    @Provides
    fun provideSpaceInviteResolver(): SpaceInviteResolver = DefaultSpaceInviteResolver

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
    fun provideGetSpaceWallpapers(
        userSettingsRepository: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetSpaceWallpapers = GetSpaceWallpapers(userSettingsRepository, dispatchers)
}

interface VaultComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun userSettingsRepository(): UserSettingsRepository
    fun spaceManager(): SpaceManager
    fun userPermissionProvider(): UserPermissionProvider
    fun auth(): AuthRepository
    fun appActionManager(): AppActionManager
    fun logger(): Logger
    fun awaitAccount(): AwaitAccountStartManager
    fun profileContainer(): ProfileSubscriptionManager
    fun pendingIntentStore(): PendingIntentStore
    fun stringResourceProvider(): StringResourceProvider
    fun dateProvider(): DateProvider
    fun fieldParser(): FieldParser
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun notificationPermissionManager(): NotificationPermissionManager
    fun provideChatEventChannel(): ChatEventChannel
    fun provideStorelessSubscriptionContainer(): StorelessSubscriptionContainer
    fun provideVaultChatPreviewContainer(): ChatPreviewContainer
    fun appInfo(): AppInfo
    @Named(DEFAULT_APP_COROUTINE_SCOPE) fun scope(): CoroutineScope
    fun chatSubscriptionContainer(): ChatsDetailsSubscriptionContainer
}