package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugSpace
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.settings.ProfileFragment
import com.anytypeio.anytype.ui_settings.account.ProfileViewModel
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ProfileModule::class])
@PerScreen
interface ProfileSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ProfileModule): Builder
        fun build(): ProfileSubComponent
    }

    fun inject(fragment: ProfileFragment)
}

@Module(includes = [ProfileModule.Bindings::class])
object ProfileModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        deleteAccount: DeleteAccount,
        analytics: Analytics,
        storelessSubscriptionContainer: StorelessSubscriptionContainer,
        setObjectDetails: SetObjectDetails,
        configStorage: ConfigStorage,
        urlBuilder: UrlBuilder,
        setDocumentImageIcon: SetDocumentImageIcon,
        spaceGradientProvider: SpaceGradientProvider
    ): ProfileViewModel.Factory = ProfileViewModel.Factory(
        deleteAccount = deleteAccount,
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
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @Provides
    @PerScreen
    fun provideDebugSync(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DebugSpace = DebugSpace(repo = repo, dispatchers = dispatchers)

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

    @Module
    interface Bindings {
        // Add bindings if needed
    }
}