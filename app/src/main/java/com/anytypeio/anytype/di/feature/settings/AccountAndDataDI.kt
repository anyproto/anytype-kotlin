package com.anytypeio.anytype.di.feature.settings

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugSync
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
import com.anytypeio.anytype.ui.settings.AccountAndDataFragment
import com.anytypeio.anytype.ui_settings.account.AccountAndDataViewModel
import com.anytypeio.anytype.ui_settings.account.repo.DebugSyncShareDownloader
import com.anytypeio.anytype.ui_settings.account.repo.FileSaver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AccountAndDataModule::class])
@PerScreen
interface AccountAndDataSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AccountAndDataModule): Builder
        fun build(): AccountAndDataSubComponent
    }

    fun inject(fragment: AccountAndDataFragment)
}

@Module(includes = [AccountAndDataModule.Bindings::class])
object AccountAndDataModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        clearFileCache: ClearFileCache,
        deleteAccount: DeleteAccount,
        debugSyncShareDownloader: DebugSyncShareDownloader,
        analytics: Analytics,
        storelessSubscriptionContainer: StorelessSubscriptionContainer,
        setObjectDetails: SetObjectDetails,
        configStorage: ConfigStorage,
        urlBuilder: UrlBuilder,
        setDocumentImageIcon: SetDocumentImageIcon,
        spaceGradientProvider: SpaceGradientProvider
    ): AccountAndDataViewModel.Factory = AccountAndDataViewModel.Factory(
        clearFileCache = clearFileCache,
        deleteAccount = deleteAccount,
        debugSyncShareDownloader = debugSyncShareDownloader,
        analytics = analytics,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        setObjectDetails = setObjectDetails,
        configStorage = configStorage,
        urlBuilder = urlBuilder,
        setDocumentImageIcon = setDocumentImageIcon,
        spaceGradientProvider = spaceGradientProvider
    )

    @Provides
    @PerScreen
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @Provides
    @PerScreen
    fun provideDebugSync(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DebugSync = DebugSync(repo = repo, dispatchers = dispatchers)

    @Provides
    @PerScreen
    fun provideFileSaver(
        uriFileProvider: UriFileProvider,
        context: Context,
        dispatchers: AppCoroutineDispatchers,
    ): FileSaver = FileSaver(context, uriFileProvider, dispatchers)

    @Provides
    @PerScreen
    fun providesDebugSyncShareDownloader(
        debugSync: DebugSync,
        fileSaver: FileSaver,
        dispatchers: AppCoroutineDispatchers,
    ): DebugSyncShareDownloader = DebugSyncShareDownloader(debugSync, fileSaver, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun clearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLogoutUseCase(
        repo: AuthRepository,
        provider: ConfigStorage,
        user: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): Logout = Logout(
        repo = repo,
        config = provider,
        user = user,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun deleteAccount(repo: AuthRepository): DeleteAccount = DeleteAccount(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo,
        dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocumentImageIcon(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers
    )

    @Module
    interface Bindings {

        @PerScreen
        @Binds
        fun bindUriFileProvider(
            defaultProvider: DefaultUriFileProvider
        ): UriFileProvider
    }
}