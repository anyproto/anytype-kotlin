package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.ui.settings.AboutAppFragment
import com.anytypeio.anytype.ui_settings.about.AboutAppViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    modules = [AboutAppModule::class],
    dependencies = [AboutAppDependencies::class]
)
@AboutAppScope
interface AboutAppComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: AboutAppDependencies): AboutAppComponent
    }

    fun inject(fragment: AboutAppFragment)
}

@Module
object AboutAppModule {

    @JvmStatic
    @Provides
    @AboutAppScope
    fun provideGetAccountUseCase(
        repo: AuthRepository
    ): GetAccount = GetAccount(repo = repo)

    @JvmStatic
    @Provides
    @AboutAppScope
    fun provideGetVersion(
        repo: AuthRepository
    ): GetLibraryVersion = GetLibraryVersion(repo)

    @Module
    interface Declarations {

        @AboutAppScope
        @Binds
        fun bindViewModelFactory(
            factory: AboutAppViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface AboutAppDependencies : ComponentDependencies {
    fun authRepo(): AuthRepository
    fun configStorage(): ConfigStorage
    fun analytics(): Analytics
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AboutAppScope