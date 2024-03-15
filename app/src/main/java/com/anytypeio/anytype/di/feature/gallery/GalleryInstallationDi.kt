package com.anytypeio.anytype.di.feature.gallery

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModelFactory
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

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
    fun analytics(): Analytics
}