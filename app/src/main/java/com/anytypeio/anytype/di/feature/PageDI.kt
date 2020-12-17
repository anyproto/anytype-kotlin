package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.*
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.download.Downloader
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.presentation.page.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.PageViewModelFactory
import com.anytypeio.anytype.presentation.page.editor.Interactor
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.Bridge
import com.anytypeio.anytype.ui.page.PageFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(modules = [EditorSessionModule::class, EditorUseCaseModule::class])
@PerScreen
interface PageSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun session(module: EditorSessionModule): Builder
        fun usecase(module: EditorUseCaseModule): Builder
        fun build(): PageSubComponent
    }

    fun inject(fragment: PageFragment)
}


/**
 * Sesssion-related dependencies, session being defined as active work with a document visible to our user.
 * Hence, these dependencies are stateful and therefore should not be shared between different sessions of the same document.
 * Consider the following navigation scenario: Document A > Document B > Document A'.
 * In this case, statetul dependencies should not be shared between A and A'.
 */
@Module
object EditorSessionModule {

    @JvmStatic
    @Provides
    fun provideToggler(): ToggleStateHolder = ToggleStateHolder.Default()

    @JvmStatic
    @Provides
    fun provideCounter(): Counter = Counter.Default()

    @JvmStatic
    @Provides
    fun provideProxer(): Editor.Proxer = Editor.Proxer()

    @JvmStatic
    @Provides
    fun provideStorage(): Editor.Storage = Editor.Storage()

    @JvmStatic
    @Provides
    fun providePageViewModelFactory(
        openPage: OpenPage,
        closePage: ClosePage,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
        updateLinkMarks: UpdateLinkMarks,
        removeLinkMark: RemoveLinkMark,
        createPage: CreatePage,
        createDocument: CreateDocument,
        createNewDocument: CreateNewDocument,
        documentExternalEventReducer: DocumentExternalEventReducer,
        urlBuilder: UrlBuilder,
        renderer: DefaultBlockViewRenderer,
        archiveDocument: ArchiveDocument,
        interactor: Orchestrator,
        getListPages: GetListPages,
        analytics: Analytics,
        bridge: Bridge<Payload>
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        createPage = createPage,
        createDocument = createDocument,
        createNewDocument = createNewDocument,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        updateLinkMarks = updateLinkMarks,
        removeLinkMark = removeLinkMark,
        documentEventReducer = documentExternalEventReducer,
        urlBuilder = urlBuilder,
        renderer = renderer,
        archiveDocument = archiveDocument,
        interactor = interactor,
        getListPages = getListPages,
        analytics = analytics,
        bridge = bridge
    )

    @JvmStatic
    @Provides
    fun provideDefaultBlockViewRenderer(
        urlBuilder: UrlBuilder,
        toggleStateHolder: ToggleStateHolder,
        counter: Counter
    ): DefaultBlockViewRenderer = DefaultBlockViewRenderer(
        urlBuilder = urlBuilder,
        toggleStateHolder = toggleStateHolder,
        counter = counter
    )

    @JvmStatic
    @Provides
    fun provideDocumentExternalEventReducer(): DocumentExternalEventReducer =
        DocumentExternalEventReducer()

    @JvmStatic
    @Provides
    fun providePatternMatcher(): DefaultPatternMatcher = DefaultPatternMatcher()

    @JvmStatic
    @Provides
    fun provideSelectionStateHolder(): SelectionStateHolder = SelectionStateHolder.Default()

    @JvmStatic
    @Provides
    fun provideMemory(
        selectionStateHolder: SelectionStateHolder
    ): Editor.Memory = Editor.Memory(
        selections = selectionStateHolder
    )

    @JvmStatic
    @Provides
    fun provideInteractor(
        storage: Editor.Storage,
        proxer: Editor.Proxer,
        memory: Editor.Memory,
        createBlock: CreateBlock,
        replaceBlock: ReplaceBlock,
        duplicateBlock: DuplicateBlock,
        updateTextColor: UpdateTextColor,
        updateBackgroundColor: UpdateBackgroundColor,
        splitBlock: SplitBlock,
        mergeBlocks: MergeBlocks,
        unlinkBlocks: UnlinkBlocks,
        updateDivider: UpdateDivider,
        updateTextStyle: UpdateTextStyle,
        updateCheckbox: UpdateCheckbox,
        downloadFile: DownloadFile,
        updateTitle: UpdateTitle,
        updateText: UpdateText,
        uploadBlock: UploadBlock,
        updateFields: UpdateFields,
        updateAlignment: UpdateAlignment,
        setupBookmark: SetupBookmark,
        turnIntoDocument: TurnIntoDocument,
        matcher: DefaultPatternMatcher,
        move: Move,
        copy: Copy,
        paste: Paste,
        undo: Undo,
        redo: Redo,
        analytics: Analytics
    ): Orchestrator = Orchestrator(
        stores = storage,
        createBlock = createBlock,
        replaceBlock = replaceBlock,
        proxies = proxer,
        duplicateBlock = duplicateBlock,
        updateBackgroundColor = updateBackgroundColor,
        updateTextColor = updateTextColor,
        uploadBlock = uploadBlock,
        splitBlock = splitBlock,
        mergeBlocks = mergeBlocks,
        unlinkBlocks = unlinkBlocks,
        undo = undo,
        redo = redo,
        updateTextStyle = updateTextStyle,
        updateCheckbox = updateCheckbox,
        updateDivider = updateDivider,
        memory = memory,
        downloadFile = downloadFile,
        updateTitle = updateTitle,
        turnIntoDocument = turnIntoDocument,
        textInteractor = Interactor.TextInteractor(
            proxies = proxer,
            stores = storage,
            matcher = matcher
        ),
        updateText = updateText,
        updateAlignment = updateAlignment,
        setupBookmark = setupBookmark,
        move = move,
        paste = paste,
        copy = copy,
        analytics = analytics,
        updateFields = updateFields
    )
}

/**
 * These dependencies are stateless and therefore should be shared between different sessions of the same document.
 * In other words, if document A is opened twice (as A and A'), use-cases will be shared between all its sessions.
 * Consider the following navigation scenario: Document A > Document B > Document A'
 * The same documents have the same use-case dependencies, in order to avoid instance-creation overhead.
 */
@Module
object EditorUseCaseModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun getListPages(repo: BlockRepository): GetListPages = GetListPages(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenPageUseCase(
        repo: BlockRepository
    ): OpenPage = OpenPage(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideClosePageUseCase(
        repo: BlockRepository
    ): ClosePage = ClosePage(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateBlockUseCase(
        repo: BlockRepository
    ): UpdateText = UpdateText(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateBlockUseCase(
        repo: BlockRepository
    ): CreateBlock = CreateBlock(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideInterceptEventsUseCase(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        channel = channel,
        context = Dispatchers.IO
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun providesInterceptThreadStatusUseCase(
        channel: ThreadStatusChannel
    ): InterceptThreadStatus = InterceptThreadStatus(
        channel = channel,
        context = Dispatchers.IO
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateCheckboxUseCase(
        repo: BlockRepository
    ): UpdateCheckbox = UpdateCheckbox(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUnlinkBlocksUseCase(
        repo: BlockRepository
    ): UnlinkBlocks = UnlinkBlocks(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDuplicateBlockUseCase(
        repo: BlockRepository
    ): DuplicateBlock = DuplicateBlock(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDivider(
        repo: BlockRepository
    ): UpdateDivider = UpdateDivider(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideMergeBlocksUseCase(
        repo: BlockRepository
    ): MergeBlocks = MergeBlocks(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSplitBlockUseCase(
        repo: BlockRepository
    ): SplitBlock = SplitBlock(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUploadUrl(
        repo: BlockRepository
    ): UploadBlock = UploadBlock(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateLinkMarks(): UpdateLinkMarks = UpdateLinkMarks()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRemoveLinkMark(): RemoveLinkMark = RemoveLinkMark()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateTextStyleUseCase(
        repo: BlockRepository
    ): UpdateTextStyle = UpdateTextStyle(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateTextColorUseCase(
        repo: BlockRepository
    ): UpdateTextColor = UpdateTextColor(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateBackgroundColorUseCase(
        repo: BlockRepository
    ): UpdateBackgroundColor = UpdateBackgroundColor(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreatePageUseCase(
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreatePage = CreatePage(
        repo = repo,
        documentEmojiIconProvider = documentEmojiIconProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDownloadFileUseCase(
        downloader: Downloader
    ): DownloadFile = DownloadFile(
        downloader = downloader,
        context = Dispatchers.Main
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUndoUseCase(
        repo: BlockRepository
    ): Undo = Undo(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRedoUseCase(
        repo: BlockRepository
    ): Redo = Redo(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateDocumentUseCase(
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreateDocument = CreateDocument(
        repo = repo,
        documentEmojiProvider = documentEmojiIconProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateNewDocumentUseCase(
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreateNewDocument = CreateNewDocument(
        repo = repo,
        documentEmojiProvider = documentEmojiIconProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideArchiveDocumentUseCase(
        repo: BlockRepository
    ): ArchiveDocument = ArchiveDocument(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideReplaceBlockUseCase(
        repo: BlockRepository
    ): ReplaceBlock = ReplaceBlock(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateTitleUseCase(
        repo: BlockRepository
    ): UpdateTitle = UpdateTitle(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateAlignmentUseCase(
        repo: BlockRepository
    ): UpdateAlignment = UpdateAlignment(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetupBookmarkUseCase(
        repo: BlockRepository
    ): SetupBookmark = SetupBookmark(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideClipboardPasteUseCase(
        repo: BlockRepository,
        clipboard: Clipboard,
        matcher: Clipboard.UriMatcher
    ): Paste = Paste(
        repo = repo,
        clipboard = clipboard,
        matcher = matcher
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyUseCase(
        repo: BlockRepository,
        clipboard: Clipboard
    ): Copy = Copy(
        repo = repo,
        clipboard = clipboard
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideMoveUseCase(
        repo: BlockRepository
    ): Move = Move(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTurnIntoDocumentUseCase(
        repo: BlockRepository
    ): TurnIntoDocument = TurnIntoDocument(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateFieldsUseCase(
        repo: BlockRepository
    ): UpdateFields = UpdateFields(repo)
}