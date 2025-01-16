package com.anytypeio.anytype.di.feature.profile

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.profile.ProfileViewModel
import com.anytypeio.anytype.ui.profile.ProfileScreenFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [ProfileScreenComponentDependencies::class],
    modules = [
        ProfileScreenModule::class,
        ProfileScreenModule.Declarations::class
    ]
)
interface ProfileScreenComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: ProfileViewModel.VmParams,
            dependencies: ProfileScreenComponentDependencies
        ): ProfileScreenComponent
    }

    fun inject(fragment: ProfileScreenFragment)

}

@Module
object ProfileScreenModule {

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
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers,
        logger = logger
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ProfileViewModel.Factory
        ): ViewModelProvider.Factory
    }

}

interface ProfileScreenComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun userSettingsRepository(): UserSettingsRepository
    fun logger(): Logger
    fun localeProvider(): LocaleProvider
    fun userPermissionProvider(): UserPermissionProvider
    fun fieldsProvider(): FieldParser
    fun provideMembershipProvider(): MembershipProvider
    fun subEventChannel(): SubscriptionEventChannel
}