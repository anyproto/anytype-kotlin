package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.download.Downloader
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.event.interactor.EventChannel
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.presentation.page.DocumentExternalEventReducer
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
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
        undo: Undo,
        redo: Redo,
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
        updateBackgroundColor: UpdateBackgroundColor,
        mergeBlocks: MergeBlocks,
        splitBlock: SplitBlock,
        createPage: CreatePage,
        uploadUrl: UploadUrl,
        documentExternalEventReducer: DocumentExternalEventReducer,
        urlBuilder: UrlBuilder,
        downloadFile: DownloadFile,
        renderer: DefaultBlockViewRenderer,
        counter: Counter
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        createPage = createPage,
        updateBlock = updateBlock,
        createBlock = createBlock,
        interceptEvents = interceptEvents,
        updateCheckbox = updateCheckbox,
        unlinkBlocks = unlinkBlocks,
        duplicateBlock = duplicateBlock,
        updateTextStyle = updateTextStyle,
        updateTextColor = updateTextColor,
        updateBackgroundColor = updateBackgroundColor,
        updateLinkMarks = updateLinkMarks,
        removeLinkMark = removeLinkMark,
        mergeBlocks = mergeBlocks,
        uploadUrl = uploadUrl,
        splitBlock = splitBlock,
        documentEventReducer = documentExternalEventReducer,
        urlBuilder = urlBuilder,
        downloadFile = downloadFile,
        renderer = renderer,
        counter = counter,
        undo = undo,
        redo = redo
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
    fun provideSplitBlockUseCase(
        repo: BlockRepository
    ): SplitBlock = SplitBlock(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideUploadUrl(
        repo: BlockRepository
    ): UploadUrl = UploadUrl(
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

    @Provides
    @PerScreen
    fun provideUpdateBackgroundColorUseCase(
        repo: BlockRepository
    ): UpdateBackgroundColor = UpdateBackgroundColor(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideCreatePageUseCase(
        repo: BlockRepository
    ): CreatePage = CreatePage(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideDownloadFileUseCase(
        downloader: Downloader
    ): DownloadFile = DownloadFile(
        downloader = downloader,
        context = Dispatchers.Main
    )

    @Provides
    @PerScreen
    fun provideDefaultBlockViewRenderer(
        emojifier: Emojifier,
        urlBuilder: UrlBuilder,
        toggleStateHolder: ToggleStateHolder
    ): DefaultBlockViewRenderer = DefaultBlockViewRenderer(
        urlBuilder = urlBuilder,
        emojifier = emojifier,
        toggleStateHolder = toggleStateHolder
    )

    @Provides
    @PerScreen
    fun provideToggler(): ToggleStateHolder = ToggleStateHolder.Default()

    @Provides
    @PerScreen
    fun provideCounter(): Counter = Counter.Default()

    @Provides
    @PerScreen
    fun provideDocumentExternalEventReducer(): DocumentExternalEventReducer =
        DocumentExternalEventReducer()

    @Provides
    @PerScreen
    fun provideUndoUseCase(
        repo: BlockRepository
    ): Undo = Undo(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideRedoUseCase(
        repo: BlockRepository
    ): Redo = Redo(
        repo = repo
    )
}