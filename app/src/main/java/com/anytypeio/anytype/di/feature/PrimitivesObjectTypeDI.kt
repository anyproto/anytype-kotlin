package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.CreateFromScratch
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.GetObjectTypeConflictingFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeHeaderRecommendedFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.types.CreateObjectType
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateTypeVmParams
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesViewModel
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesVmFactory
import com.anytypeio.anytype.presentation.types.SpaceTypesViewModel
import com.anytypeio.anytype.presentation.types.SpaceTypesVmFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.ui.primitives.CreateTypeFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFieldsFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.primitives.SpacePropertiesFragment
import com.anytypeio.anytype.ui.primitives.SpaceTypesFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [ObjectTypeDependencies::class],
    modules = [
        ObjectTypeModule::class,
        ObjectTypeModule.Declarations::class
    ]
)
@PerScreen
interface ObjectTypeComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: ObjectTypeVmParams,
            dependencies: ObjectTypeDependencies
        ): ObjectTypeComponent
    }

    fun inject(fragment: ObjectTypeFragment)
    fun inject(fragment: ObjectTypeFieldsFragment)
}

@Module
object ObjectTypeModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDataViewProperties(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDataViewProperties = SetDataViewProperties(repo, dispatchers)

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

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(repository, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun coverHashProvider(): CoverImageHashProvider = DefaultCoverImageHashProvider()

    @JvmStatic
    @PerScreen
    @Provides
    fun getDeleteObjects(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DeleteObjects = DeleteObjects(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun getObjectTypeConflictingFields(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypeConflictingFields = GetObjectTypeConflictingFields(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDuplicateObjectsListUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DuplicateObjects = DuplicateObjects(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTypeSetRecommendedFields(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectTypeRecommendedFields = SetObjectTypeRecommendedFields(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTypeSetHeaderRecommendedFields(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectTypeHeaderRecommendedFields =
        SetObjectTypeHeaderRecommendedFields(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddToFeaturedRelations(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddToFeaturedRelations = AddToFeaturedRelations(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRemoveFromFeaturedRelations(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): RemoveFromFeaturedRelations = RemoveFromFeaturedRelations(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateBlockUseCase(
        repo: BlockRepository
    ): UpdateText = UpdateText(
        repo = repo
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ObjectTypeVMFactory
        ): ViewModelProvider.Factory
    }
}

interface ObjectTypeDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun subEventChannel(): SubscriptionEventChannel
    fun logger(): Logger
    fun localeProvider(): LocaleProvider
    fun config(): ConfigStorage
    fun userPermissionProvider(): UserPermissionProvider
    fun provideStoreOfRelations(): StoreOfRelations
    fun provideSpaceSyncAndP2PStatusProvider(): SpaceSyncAndP2PStatusProvider
    fun provideUserSettingsRepository(): UserSettingsRepository
    fun fieldParser(): FieldParser
    fun provideEventChannel(): EventChannel
    fun provideStringResourceProvider(): StringResourceProvider
    fun dispatcher(): Dispatcher<Payload>
    fun spaceManager(): SpaceManager
}

//region Space Types Screen
@Component(
    dependencies = [SpaceTypesDependencies::class],
    modules = [
        SpaceTypesModule::class,
        SpaceTypesModule.Declarations::class
    ]
)
@PerScreen
interface SpaceTypesComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: SpaceTypesViewModel.VmParams,
            dependencies: SpaceTypesDependencies
        ): SpaceTypesComponent
    }

    fun inject(fragment: SpaceTypesFragment)
}

@Module
object SpaceTypesModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: SpaceTypesVmFactory
        ): ViewModelProvider.Factory
    }
}

interface SpaceTypesDependencies : ComponentDependencies {
    fun stringResourceProvider(): StringResourceProvider
    fun blockRepository(): BlockRepository
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun userPermissionProvider(): UserPermissionProvider
    fun fieldParser(): FieldParser
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
}
//endregion

//region Space Properties Screen
@Component(
    dependencies = [SpacePropertiesDependencies::class],
    modules = [
        SpacePropertiesModule::class,
        SpacePropertiesModule.Declarations::class
    ]
)
@PerScreen
interface SpacePropertiesComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: SpacePropertiesViewModel.VmParams,
            dependencies: SpacePropertiesDependencies
        ): SpacePropertiesComponent
    }

    fun inject(fragment: SpacePropertiesFragment)
}

@Module
object SpacePropertiesModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun createRelation(
        repo: BlockRepository,
        storeOfRelations: StoreOfRelations
    ) = CreateRelation(
        repo = repo,
        storeOfRelations = storeOfRelations
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: SpacePropertiesVmFactory
        ): ViewModelProvider.Factory
    }
}

interface SpacePropertiesDependencies : ComponentDependencies {
    fun stringResourceProvider(): StringResourceProvider
    fun blockRepository(): BlockRepository
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun storeOfRelations(): StoreOfRelations
    fun userPermissionProvider(): UserPermissionProvider
    fun fieldParser(): FieldParser
}
//endregion

//region Create Type Screen
@Component(
    dependencies = [CreateObjectTypeDependencies::class],
    modules = [
        CreateObjectTypeModule::class,
        CreateObjectTypeModule.Declarations::class
    ]
)
@PerScreen
interface CreateObjectTypeComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: CreateTypeVmParams,
            dependencies: CreateObjectTypeDependencies
        )
                : CreateObjectTypeComponent
    }

    fun inject(fragment: CreateTypeFragment)
}

@Module
object CreateObjectTypeModule {

    @Provides
    @PerScreen
    @JvmStatic
    fun createObjectType(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateObjectType = CreateObjectType(repo, dispatchers)

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: CreateObjectTypeVMFactory
        ): ViewModelProvider.Factory
    }
}

interface CreateObjectTypeDependencies : ComponentDependencies {
    fun stringResourceProvider(): StringResourceProvider
    fun blockRepository(): BlockRepository
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
}
//endregion