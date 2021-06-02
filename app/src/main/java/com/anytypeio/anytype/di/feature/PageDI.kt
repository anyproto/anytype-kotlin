package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectSubComponent
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.*
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.download.Downloader
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
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
import com.anytypeio.anytype.presentation.page.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.page.editor.Interactor
import com.anytypeio.anytype.presentation.page.editor.InternalDetailModificationManager
import com.anytypeio.anytype.presentation.page.editor.Orchestrator
import com.anytypeio.anytype.presentation.page.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.relations.providers.*
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
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

    fun documentEmojiIconPickerComponentBuilder(): DocumentEmojiIconPickerSubComponent.Builder
    fun documentActionMenuComponentBuilder(): DocumentActionMenuSubComponent.Builder

    // Relations

    fun documentRelationSubComponent(): DocumentRelationSubComponent.Builder
    fun relationAddToObjectComponent() : RelationAddToObjectSubComponent.Builder
    fun relationCreateFromScratchForObjectComponent() : RelationCreateFromScratchForObjectSubComponent.Builder
    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun editDocRelationComponent() : ObjectObjectRelationValueSubComponent.Builder
    fun editRelationDateComponent(): RelationDataValueSubComponent.Builder

    fun docCoverGalleryComponentBuilder(): SelectDocCoverSubComponent.Builder
    fun uploadDocCoverImageComponentBuilder(): UploadDocCoverImageSubComponent.Builder

    fun documentAddNewBlockComponentBuilder(): DocumentAddNewBlockSubComponent.Builder

    fun objectLayoutComponent() : ObjectLayoutSubComponent.Builder
}


/**
 * Session-related dependencies, session being defined as active work with a document visible to our user.
 * Hence, these dependencies are stateful and therefore should not be shared between different sessions of the same document.
 * Consider the following navigation scenario: Document A > Document B > Document A'.
 * In this case, stateful dependencies should not be shared between A and A'.
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
    @PerScreen
    fun provideStorage(): Editor.Storage = Editor.Storage()

    @JvmStatic
    @Provides
    fun providePageViewModelFactory(
        openPage: OpenPage,
        closePage: CloseBlock,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
        updateLinkMarks: UpdateLinkMarks,
        removeLinkMark: RemoveLinkMark,
        createPage: CreatePage,
        createDocument: CreateDocument,
        createObject: CreateObject,
        createNewDocument: CreateNewDocument,
        documentExternalEventReducer: DocumentExternalEventReducer,
        setDocCoverImage: SetDocCoverImage,
        removeDocCover: RemoveDocCover,
        urlBuilder: UrlBuilder,
        renderer: DefaultBlockViewRenderer,
        archiveDocument: ArchiveDocument,
        orchestrator: Orchestrator,
        getListPages: GetListPages,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>,
        detailModificationManager: DetailModificationManager,
        updateDetail: UpdateDetail,
        getObjectTypes: GetObjectTypes
    ): PageViewModelFactory = PageViewModelFactory(
        openPage = openPage,
        closePage = closePage,
        createPage = createPage,
        createDocument = createDocument,
        createObject = createObject,
        createNewDocument = createNewDocument,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        updateLinkMarks = updateLinkMarks,
        removeLinkMark = removeLinkMark,
        documentEventReducer = documentExternalEventReducer,
        setDocCoverImage = setDocCoverImage,
        removeDocCover = removeDocCover,
        urlBuilder = urlBuilder,
        renderer = renderer,
        archiveDocument = archiveDocument,
        orchestrator = orchestrator,
        getListPages = getListPages,
        analytics = analytics,
        dispatcher = dispatcher,
        detailModificationManager = detailModificationManager,
        updateDetail = updateDetail,
        getObjectTypes = getObjectTypes
    )

    @JvmStatic
    @Provides
    fun provideDefaultBlockViewRenderer(
        urlBuilder: UrlBuilder,
        toggleStateHolder: ToggleStateHolder,
        counter: Counter,
        coverImageHashProvider: CoverImageHashProvider
    ): DefaultBlockViewRenderer = DefaultBlockViewRenderer(
        urlBuilder = urlBuilder,
        toggleStateHolder = toggleStateHolder,
        counter = counter,
        coverImageHashProvider = coverImageHashProvider
    )

    @JvmStatic
    @Provides
    fun provideCoverImageHashProvider(): CoverImageHashProvider = DefaultCoverImageHashProvider()

    @JvmStatic
    @Provides
    fun provideDocumentExternalEventReducer(): DocumentExternalEventReducer = DocumentExternalEventReducer()

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
        turnInto: TurnIntoStyle,
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
        setRelationKey: SetRelationKey,
        analytics: Analytics,
        updateBlocksMark: UpdateBlocksMark
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
        setRelationKey = setRelationKey,
        analytics = analytics,
        updateFields = updateFields,
        turnIntoStyle = turnInto,
        updateBlocksMark = updateBlocksMark
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
    ): CloseBlock = CloseBlock(
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
    fun provideSetRelationKeyUseCase(
        repo: BlockRepository
    ): SetRelationKey = SetRelationKey(
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
    fun provideUpdateBlocksMark(
        repo: BlockRepository
    ): UpdateBlocksMark = UpdateBlocksMark(
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
    fun provideCreateObjectUseCase(
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreateObject = CreateObject(
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

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDefaultObjectRelationProvider(
        storage: Editor.Storage
    ) : ObjectRelationProvider = DefaultObjectRelationProvider(storage.relations)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDefaultObjectValueProvider(
        storage: Editor.Storage
    ) : ObjectValueProvider = DefaultObjectValueProvider(storage.details)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeProvider(
        storage: Editor.Storage
    ) : ObjectTypeProvider = object : ObjectTypeProvider {
        override fun provide(): List<ObjectType> = storage.objectTypes.current()
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        storage: Editor.Storage
    ) : ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> = storage.details.current().details
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun providePayloadDispatcher() : Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDetailManager(
        storage: Editor.Storage
    ) : DetailModificationManager = InternalDetailModificationManager(
        store = storage.details
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocCoverImageUseCase(
        repo: BlockRepository
    ): SetDocCoverImage = SetDocCoverImage(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRemoveDocCoverUseCase(
        repo: BlockRepository
    ): RemoveDocCover = RemoveDocCover(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTurnIntoUseCase(repo: BlockRepository): TurnIntoStyle = TurnIntoStyle(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository
    ) : UpdateDetail = UpdateDetail(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository
    ) : GetObjectTypes = GetObjectTypes(repository)
}