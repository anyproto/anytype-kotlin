package com.anytypeio.anytype.di.feature.cover

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.unsplash.SearchUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.UnsplashViewModel
import com.anytypeio.anytype.ui.editor.cover.UnsplashBaseFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [UnsplashModule::class])
@PerDialog
interface UnsplashSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: UnsplashModule): Builder
        fun build(): UnsplashSubComponent
    }

    fun inject(fragment: UnsplashBaseFragment)
}

@Module
object UnsplashModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        search: SearchUnsplashImage,
        delegator: Delegator<Action>
    ): UnsplashViewModel.Factory {
        return UnsplashViewModel.Factory(
            search = search,
            delegator = delegator
        )
    }

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSearch(repo: UnsplashRepository): SearchUnsplashImage = SearchUnsplashImage(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSetDocCoverImageUseCase(
        repo: BlockRepository
    ): SetDocCoverImage = SetDocCoverImage(repo)
}