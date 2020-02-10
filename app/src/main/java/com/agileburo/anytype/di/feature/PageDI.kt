package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.interactor.EventChannel
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.page.ClosePage
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
        interceptEvents: InterceptEvents,
        updateCheckbox: UpdateCheckbox,
        unlinkBlocks: UnlinkBlocks,
        updateLinkMarks: UpdateLinkMarks,
        removeLinkMark: RemoveLinkMark,
        duplicateBlock: DuplicateBlock,
        updateTextStyle: UpdateTextStyle,
        updateTextColor: UpdateTextColor,
        mergeBlocks: MergeBlocks
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        updateBlock = updateBlock,
        createBlock = createBlock,
        interceptEvents = interceptEvents,
        updateCheckbox = updateCheckbox,
        unlinkBlocks = unlinkBlocks,
        duplicateBlock = duplicateBlock,
        updateTextStyle = updateTextStyle,
        updateTextColor = updateTextColor,
        updateLinkMarks = updateLinkMarks,
        removeLinkMark = removeLinkMark,
        mergeBlocks = mergeBlocks
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
    fun provideInterceptEventsUseCase(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        channel = channel,
        context = Dispatchers.IO
    )

    @Provides
    @PerScreen
    fun provideUpdateCheckboxUseCase(
        repo: BlockRepository
    ): UpdateCheckbox = UpdateCheckbox(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideUnlinkBlocksUseCase(
        repo: BlockRepository
    ): UnlinkBlocks = UnlinkBlocks(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideDuplicateBlockUseCase(
        repo: BlockRepository
    ): DuplicateBlock = DuplicateBlock(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideMergeBlocksUseCase(
        repo: BlockRepository
    ): MergeBlocks = MergeBlocks(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideUpdateLinkMarks(): UpdateLinkMarks = UpdateLinkMarks()

    @Provides
    @PerScreen
    fun provideRemoveLinkMark(): RemoveLinkMark = RemoveLinkMark()

    @Provides
    @PerScreen
    fun provideUpdateTextStyleUseCase(
        repo: BlockRepository
    ): UpdateTextStyle = UpdateTextStyle(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideUpdateTextColorUseCase(
        repo: BlockRepository
    ): UpdateTextColor = UpdateTextColor(
        repo = repo
    )
}