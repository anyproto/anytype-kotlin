package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.device.share.debug.DebugSpaceDeviceFileContentSaver
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.DebugSpaceContentSaver
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
import com.anytypeio.anytype.ui.settings.MainSettingFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [
        MainSettingsModule::class,
        MainSettingsModule.Bindings::class
    ]
)
@PerScreen
interface MainSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: MainSettingsModule): Builder
        fun build(): MainSettingsSubComponent
    }

    fun inject(fragment: MainSettingFragment)
}

@Module
object MainSettingsModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        analytics: Analytics,
        storelessSubscriptionContainer: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        setObjectDetails: SetObjectDetails,
        spaceGradientProvider: SpaceGradientProvider,
        debugSpaceShareDownloader: DebugSpaceShareDownloader,
        spaceManager: SpaceManager
    ): MainSettingsViewModel.Factory = MainSettingsViewModel.Factory(
        analytics = analytics,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        urlBuilder = urlBuilder,
        setObjectDetails = setObjectDetails,
        spaceGradientProvider = spaceGradientProvider,
        debugSpaceShareDownloader = debugSpaceShareDownloader,
        spaceManager = spaceManager
    )

    @Module
    interface Bindings {
        @PerScreen
        @Binds
        fun bindUriFileProvider(
            defaultProvider: DefaultUriFileProvider
        ): UriFileProvider

        @PerScreen
        @Binds
        fun bindSpaceDebugDeviceSharer(
            saver: DebugSpaceDeviceFileContentSaver
        ): DebugSpaceContentSaver
    }
}