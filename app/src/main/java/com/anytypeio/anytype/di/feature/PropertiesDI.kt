package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_properties.AddPropertyVmFactory
import com.anytypeio.anytype.feature_properties.add.AddPropertyVmParams
import com.anytypeio.anytype.ui.primitives.AddPropertyFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

//region ADD PROPERTY SCREEN
@PerModal
@Component(
    modules = [
        AddPropertiesModule::class,
        AddPropertiesModule.Declarations::class
    ],
    dependencies = [AddPropertiesDependencies::class]
)
interface AddPropertiesComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: AddPropertyVmParams,
            dependencies: AddPropertiesDependencies
        ): AddPropertiesComponent
    }

    fun inject(fragment: AddPropertyFragment)
}

@Module
object AddPropertiesModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideTypeSetRecommendedFields(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectTypeRecommendedFields = SetObjectTypeRecommendedFields(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerModal
    fun createRelation(
        repo: BlockRepository,
        storeOfRelations: StoreOfRelations,
    ) = CreateRelation(
        repo = repo,
        storeOfRelations = storeOfRelations
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(
        repo,
        dispatchers
    )

    @Module
    interface Declarations {
        @PerModal
        @Binds
        fun bindViewModelFactory(
            factory: AddPropertyVmFactory
        ): ViewModelProvider.Factory
    }
}

interface AddPropertiesDependencies : ComponentDependencies {
    fun provideStringResourceProvider(): StringResourceProvider
    fun provideStoreOfRelations(): StoreOfRelations
    fun provideStoreOfObjectTypes(): StoreOfObjectTypes
    fun provideBlockRepository(): BlockRepository
    fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers
}
//endregion