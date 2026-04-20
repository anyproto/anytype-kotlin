package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

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

    fun viewModelFactory(): CreateObjectViewModelFactory
}

@Module
object CreateObjectFeatureModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUploadFile(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): UploadFile = UploadFile(repo = repo, dispatchers = dispatchers)

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
    fun blockRepo(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun fileSharer(): FileSharer
}
