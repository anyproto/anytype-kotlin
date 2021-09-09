package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.objects.ObjectSetMenuViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.sheets.ObjectMenuFragment
import com.anytypeio.anytype.ui.sets.ObjectSetMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow


@Subcomponent(modules = [ObjectMenuModuleBase::class, ObjectMenuModule::class])
@PerDialog
interface ObjectMenuComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectMenuModuleBase) : Builder
        fun module(module: ObjectMenuModule): Builder
        fun build(): ObjectMenuComponent
    }

    fun inject(fragment: ObjectMenuFragment)
}

@Subcomponent(modules = [ObjectMenuModuleBase::class, ObjectSetMenuModule::class])
@PerDialog
interface ObjectSetMenuComponent {
    @Subcomponent.Builder
    interface Builder {
        fun base(module: ObjectMenuModuleBase) : Builder
        fun module(module: ObjectSetMenuModule): Builder
        fun build(): ObjectSetMenuComponent
    }

    fun inject(fragment: ObjectSetMenuFragment)
}

@Module
object ObjectMenuModuleBase {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddToFavoriteUseCase(
        repo: BlockRepository
    ) : AddToFavorite = AddToFavorite(repo = repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun provideRemoveFromFavoriteUseCase(
        repo: BlockRepository
    ) : RemoveFromFavorite = RemoveFromFavorite(repo = repo)
}

@Module
object ObjectMenuModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        archiveDocument: ArchiveDocument,
        addToFavorite: AddToFavorite,
        removeFromFavorite: RemoveFromFavorite,
        storage: Editor.Storage,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>
    ): ObjectMenuViewModel.Factory = ObjectMenuViewModel.Factory(
        archiveDocument = archiveDocument,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        storage = storage,
        analytics = analytics,
        dispatcher = dispatcher
    )
}

@Module
object ObjectSetMenuModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        archiveDocument: ArchiveDocument,
        addToFavorite: AddToFavorite,
        removeFromFavorite: RemoveFromFavorite,
        analytics: Analytics,
        state: StateFlow<ObjectSet>,
        dispatcher: Dispatcher<Payload>
    ): ObjectSetMenuViewModel.Factory = ObjectSetMenuViewModel.Factory(
        archiveDocument = archiveDocument,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        analytics = analytics,
        state = state,
        dispatcher = dispatcher
    )
}