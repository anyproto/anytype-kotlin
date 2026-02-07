package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_properties.EditTypePropertiesViewModelFactory
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.primitives.EditTypePropertiesFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

//region EDIT OBJECT TYPE PROPERTIES SCREEN
@PerModal
@Component(
    modules = [
        EditTypePropertiesModule::class,
        EditTypePropertiesModule.Declarations::class
    ],
    dependencies = [EditTypePropertiesDependencies::class]
)
interface EditTypePropertiesComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: EditTypePropertiesVmParams,
            dependencies: EditTypePropertiesDependencies
        ): EditTypePropertiesComponent
    }

    fun inject(fragment: EditTypePropertiesFragment)
}

@Module
object EditTypePropertiesModule {

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
    ) = CreateRelation(repo, storeOfRelations)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetObjectDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDataViewProperties(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDataViewProperties = SetDataViewProperties(repo, dispatchers)

    @Module
    interface Declarations {
        @PerModal
        @Binds
        fun bindViewModelFactory(
            factory: EditTypePropertiesViewModelFactory
        ): ViewModelProvider.Factory
    }
}

interface EditTypePropertiesDependencies : ComponentDependencies {
    fun provideStringResourceProvider(): StringResourceProvider
    fun provideStoreOfRelations(): StoreOfRelations
    fun provideStoreOfObjectTypes(): StoreOfObjectTypes
    fun provideBlockRepository(): BlockRepository
    fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun dispatcher(): Dispatcher<Payload>
}
//endregion