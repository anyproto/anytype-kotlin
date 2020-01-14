package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateCheckbox
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.ObservePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.page.PageFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

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
        updateBlock: UpdateBlock,
        createBlock: CreateBlock,
        observeEvents: ObserveEvents,
        updateCheckbox: UpdateCheckbox
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        updateBlock = updateBlock,
        createBlock = createBlock,
        observeEvents = observeEvents,
        updateCheckbox = updateCheckbox
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

    @Provides
    @PerScreen
    fun provideUpdateBlockUseCase(
        repo: BlockRepository
    ): UpdateBlock = UpdateBlock(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideCreateBlockUseCase(
        repo: BlockRepository
    ): CreateBlock = CreateBlock(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideObserveEventsUseCase(
        repo: BlockRepository
    ): ObserveEvents = ObserveEvents(
        repo = repo,
        context = Dispatchers.IO
    )

    @Provides
    @PerScreen
    fun provideUpdateCheckboxUseCase(
        repo: BlockRepository
    ): UpdateCheckbox = UpdateCheckbox(
        repo = repo
    )
}