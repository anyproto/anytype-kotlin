package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.sets.CreateObjectSetViewModel
import com.anytypeio.anytype.ui.sets.CreateObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [CreateSetModule::class])
@PerScreen
interface CreateSetSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateSetModule): Builder
        fun build(): CreateSetSubComponent
    }

    fun inject(fragment: CreateObjectSetFragment)
}

@Module
object CreateSetModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateSetViewModelFactory(
        getObjectTypes: GetObjectTypes,
        createObjectSet: CreateObjectSet,
        createObjectType: CreateObjectType
    ): CreateObjectSetViewModel.Factory {
        return CreateObjectSetViewModel.Factory(
            getObjectTypes = getObjectTypes,
            createObjectSet = createObjectSet,
            createObjectType = createObjectType
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectTypeUseCase(
        repo: BlockRepository,
        documentEmojiProvider: DocumentEmojiIconProvider
    ): CreateObjectType = CreateObjectType(
        repo = repo,
        documentEmojiProvider = documentEmojiProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repo: BlockRepository
    ): GetObjectTypes = GetObjectTypes(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectSetUseCase(
        repo: BlockRepository
    ): CreateObjectSet = CreateObjectSet(repo = repo)
}