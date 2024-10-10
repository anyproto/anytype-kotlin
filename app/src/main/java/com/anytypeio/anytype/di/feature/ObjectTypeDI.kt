package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.spaces.AddObjectTypeToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectSelectTypeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectTypeChangeModule::class])
@PerScreen
interface ObjectTypeChangeSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectTypeChangeModule): Builder
        fun build(): ObjectTypeChangeSubComponent
    }

    fun inject(fragment: ObjectSelectTypeFragment)
    fun inject(fragment: DataViewSelectSourceFragment)
    fun inject(fragment: EmptyDataViewSelectSourceFragment)
    fun inject(fragment: AppDefaultObjectTypeFragment)
}

@Module
object ObjectTypeChangeModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeViewModelFactory(
        getObjectTypes: GetObjectTypes,
        addObjectTypeToSpace: AddObjectTypeToSpace,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        getDefaultObjectType: GetDefaultObjectType
    ): ObjectTypeChangeViewModelFactory {
        return ObjectTypeChangeViewModelFactory(
            getObjectTypes = getObjectTypes,
            addObjectTypeToSpace = addObjectTypeToSpace,
            dispatchers = dispatchers,
            spaceManager = spaceManager,
            getDefaultObjectType = getDefaultObjectType
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypes = GetObjectTypes(repository, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddObjectTypeToSpace(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) : AddObjectTypeToSpace = AddObjectTypeToSpace(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers
    )

}