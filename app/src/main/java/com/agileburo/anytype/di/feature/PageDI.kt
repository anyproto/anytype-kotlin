package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.ObservePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.page.PageFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [PageModule::class])
@PerScreen
interface PageSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun pageModule(module: PageModule): Builder
        fun build(): PageSubComponent
    }

    fun inject(fragment: PageFragment)
}

@Module
class PageModule {

    @Provides
    @PerScreen
    fun providePageViewModelFactory(
        openPage: OpenPage,
        closePage: ClosePage,
        observePage: ObservePage
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        observePage = observePage,
        closePage = closePage
    )

    @Provides
    @PerScreen
    fun provideOpenPageUseCase(
        repo: BlockRepository
    ): OpenPage = OpenPage(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideObservePageUseCase(
        repo: BlockRepository
    ): ObservePage = ObservePage(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideClosePageUseCase(
        repo: BlockRepository
    ): ClosePage = ClosePage(
        repo = repo
    )
}