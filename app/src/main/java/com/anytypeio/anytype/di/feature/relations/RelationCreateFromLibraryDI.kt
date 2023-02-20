package com.anytypeio.anytype.di.feature.relations

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_utils.di.scope.CreateFromScratch
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.relations.RelationCreateFromLibraryViewModel
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import com.anytypeio.anytype.ui.relations.RelationCreateFromLibraryFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [RelationCreateFromLibraryDependencies::class],
    modules = [
        RelationCreateFromLibraryModule::class,
        RelationCreateFromLibraryModule.Declarations::class
    ]
)
@PerScreen
@CreateFromScratch
interface RelationCreateFromLibraryComponent {

    @Component.Factory
    interface Factory {
        fun create(
            dependencies: RelationCreateFromLibraryDependencies
        ): RelationCreateFromLibraryComponent
    }

    fun relationFormatPickerComponent(): RelationFormatPickerSubcomponent.Builder
    fun limitObjectTypeComponent(): LimitObjectTypeSubComponent.Builder

    fun inject(fragment: RelationCreateFromLibraryFragment)
}

@Module
object RelationCreateFromLibraryModule {

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideState(): StateHolder<CreateFromScratchState> = StateHolder(
        initial = CreateFromScratchState(
            format = RelationFormat.OBJECT,
            limitObjectTypes = emptyList()
        )
    )

    @JvmStatic
    @Provides
    fun createRelation(
        repo: BlockRepository,
        storeOfRelations: StoreOfRelations
    ) = CreateRelation(
        repo = repo,
        storeOfRelations = storeOfRelations
    )

    @JvmStatic
    @Provides
    fun provideSearchObjects(
        repo: BlockRepository
    ) = SearchObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    fun provideStoreOfRelations(): StoreOfRelations = DefaultStoreOfRelations()

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: RelationCreateFromLibraryViewModel.Factory): ViewModelProvider.Factory
    }

}

interface RelationCreateFromLibraryDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun urlBuilder(): UrlBuilder
}