package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.CheckIsFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.`object`.ObjectMenuViewModel
import com.anytypeio.anytype.presentation.`object`.ObjectSetMenuViewModel
import com.anytypeio.anytype.ui.page.sheets.ObjectMenuFragment
import com.anytypeio.anytype.ui.sets.ObjectSetMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


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

    @JvmStatic
    @Provides
    @PerDialog
    fun provideCheckIsFavoriteUseCase(
        repo: BlockRepository
    ) : CheckIsFavorite = CheckIsFavorite(repo = repo)
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
        checkIsFavorite: CheckIsFavorite,
        getFlavourConfig: GetFlavourConfig
    ): ObjectMenuViewModel.Factory = ObjectMenuViewModel.Factory(
        archiveDocument = archiveDocument,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        checkIsFavorite = checkIsFavorite,
        getFlavourConfig = getFlavourConfig
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
        checkIsFavorite: CheckIsFavorite,
        getFlavourConfig: GetFlavourConfig
    ): ObjectSetMenuViewModel.Factory = ObjectSetMenuViewModel.Factory(
        archiveDocument = archiveDocument,
        addToFavorite = addToFavorite,
        removeFromFavorite = removeFromFavorite,
        checkIsFavorite = checkIsFavorite,
        getFlavourConfig = getFlavourConfig
    )
}