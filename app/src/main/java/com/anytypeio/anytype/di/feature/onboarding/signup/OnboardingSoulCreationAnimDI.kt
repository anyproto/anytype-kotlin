package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSoulCreationAnimViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingSoulCreationAnimDependencies::class],
    modules = [
        OnboardingSoulCreationAnimModule::class,
        OnboardingSoulCreationAnimModule.Declarations::class
    ]
)
@SoulCreationAnimScreenScope
interface OnboardingSoulCreationAnimComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingSoulCreationAnimDependencies): OnboardingSoulCreationAnimComponent
    }

    fun getViewModel(): OnboardingSoulCreationAnimViewModel
}

@Module
object OnboardingSoulCreationAnimModule {


    @JvmStatic
    @Provides
    @SoulCreationAnimScreenScope
    fun provideSetObjectDetailsUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo = repository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @SoulCreationAnimScreenScope
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @Module
    interface Declarations {

        @Binds
        @SoulCreationAnimScreenScope
        fun bindViewModelFactory(
            factory: OnboardingSoulCreationAnimViewModel.Factory
        ): ViewModelProvider.Factory

        @SoulCreationAnimScreenScope
        @Binds
        fun bindContainer(
            container: StorelessSubscriptionContainer.Impl
        ): StorelessSubscriptionContainer

    }
}

interface OnboardingSoulCreationAnimDependencies : ComponentDependencies {
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun urlBuilder(): UrlBuilder
    fun repo(): BlockRepository
    fun channel(): SubscriptionEventChannel
    fun logger(): Logger
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SoulCreationAnimScreenScope