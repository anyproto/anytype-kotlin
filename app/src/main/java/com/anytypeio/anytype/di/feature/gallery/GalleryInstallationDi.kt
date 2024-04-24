package com.anytypeio.anytype.di.feature.gallery

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.workspace.EventProcessChannel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModelFactory
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [GalleryInstallationComponentDependencies::class],
    modules = [
        GalleryInstallationModule::class,
        GalleryInstallationModule.Declarations::class
    ]
)
@PerScreen
interface GalleryInstallationComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: GalleryInstallationComponentDependencies): Builder

        @BindsInstance
        fun withParams(params: GalleryInstallationViewModel.ViewModelParams): Builder
        fun build(): GalleryInstallationComponent
    }

    fun inject(fragment: GalleryInstallationFragment)
}

@Module
object GalleryInstallationModule {

    @Provides
    @PerScreen
    fun provideGradientProvider(): SpaceGradientProvider {
        return SpaceGradientProvider.Default
    }

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: GalleryInstallationViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface GalleryInstallationComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun userPermissionProvider(): UserPermissionProvider
    fun eventProcessChannel(): EventProcessChannel
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}