package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.page.bookmark.SetupBookmark
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel
import com.agileburo.anytype.ui.page.modals.CreateBookmarkFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [CreateBookmarkModule::class])
@PerScreen
interface CreateBookmarkSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun createBookmarkModule(module: CreateBookmarkModule): Builder
        fun build(): CreateBookmarkSubComponent
    }

    fun inject(fragment: CreateBookmarkFragment)
}

@Module
class CreateBookmarkModule {

    @Provides
    @PerScreen
    fun provideCreateBookmarkViewModelFactory(
        setupBookmark: SetupBookmark
    ): CreateBookmarkViewModel.Factory {
        return CreateBookmarkViewModel.Factory(
            setupBookmark = setupBookmark
        )
    }

    @Provides
    @PerScreen
    fun provideSetBookmarkUrlUseCase(
        repo: BlockRepository
    ): SetupBookmark = SetupBookmark(
        repo = repo
    )
}