package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSoulCreationViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingSoulCreationDependencies::class],
    modules = [
        OnboardingSoulCreationModule::class,
        OnboardingSoulCreationModule.Declarations::class
    ]
)
@SoulCreationScreenScope
interface OnboardingSoulCreationComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingSoulCreationDependencies): OnboardingSoulCreationComponent
    }

    fun getViewModel(): OnboardingSoulCreationViewModel
}

@Module
object OnboardingSoulCreationModule {


    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun provideSetObjectDetailsUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo = repository,
        dispatchers = dispatchers
    )

    @Module
    interface Declarations {

        @Binds
        @SoulCreationScreenScope
        fun bindViewModelFactory(factory: OnboardingSoulCreationViewModel.Factory): ViewModelProvider.Factory

    }
}

interface OnboardingSoulCreationDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun analytics(): Analytics
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SoulCreationScreenScope