package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [CreateObjectFeatureDependencies::class],
    modules = [
        CreateObjectFeatureModule::class,
        CreateObjectFeatureModule.Declarations::class
    ]
)
@PerScreen
interface CreateObjectFeatureComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: NewCreateObjectViewModel.VmParams,
            dependencies: CreateObjectFeatureDependencies
        ): CreateObjectFeatureComponent
    }

    // Inject method for future Fragment (when UI is wired up)
    // fun inject(fragment: CreateObjectFragment)
}

@Module
object CreateObjectFeatureModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: CreateObjectViewModelFactory
        ): ViewModelProvider.Factory
    }
}

interface CreateObjectFeatureDependencies : ComponentDependencies {
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
}
