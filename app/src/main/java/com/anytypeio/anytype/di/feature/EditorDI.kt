package com.anytypeio.anytype.di.feature

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.feature.cover.UnsplashSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectBlockSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectSubComponent
import com.anytypeio.anytype.domain.`object`.ConvertObjectToSet
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.ClearBlockContent
import com.anytypeio.anytype.domain.block.interactor.ClearBlockStyle
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.domain.block.interactor.SetObjectType
import com.anytypeio.anytype.domain.block.interactor.SplitBlock
import com.anytypeio.anytype.domain.block.interactor.TurnIntoDocument
import com.anytypeio.anytype.domain.block.interactor.TurnIntoStyle
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateAlignment
import com.anytypeio.anytype.domain.block.interactor.UpdateBackgroundColor
import com.anytypeio.anytype.domain.block.interactor.UpdateBlocksMark
import com.anytypeio.anytype.domain.block.interactor.UpdateCheckbox
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.UpdateTextColor
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.domain.block.interactor.UploadBlock
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.download.Downloader
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObjectAsMentionOrLink
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.UpdateTitle
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmarkBlock
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.SetRelationKey
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.table.CreateTable
import com.anytypeio.anytype.domain.table.CreateTableColumn
import com.anytypeio.anytype.domain.table.CreateTableRow
import com.anytypeio.anytype.domain.table.DeleteTableColumn
import com.anytypeio.anytype.domain.table.DeleteTableRow
import com.anytypeio.anytype.domain.table.DuplicateTableColumn
import com.anytypeio.anytype.domain.table.DuplicateTableRow
import com.anytypeio.anytype.domain.table.FillTableColumn
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.domain.table.MoveTableColumn
import com.anytypeio.anytype.domain.table.MoveTableRow
import com.anytypeio.anytype.domain.table.SetTableRowHeader
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Interactor
import com.anytypeio.anytype.presentation.editor.editor.InternalDetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.table.DefaultEditorTableDelegate
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableDelegate
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.template.DefaultEditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.relations.providers.DefaultObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DefaultObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.DefaultCopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.DocumentFileShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
import com.anytypeio.anytype.ui.editor.EditorFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [
        EditorSessionModule::class,
        EditorUseCaseModule::class,
        EditorUseCaseModule.Bindings::class
    ]
)
@PerScreen
interface EditorSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun session(module: EditorSessionModule): Builder
        fun usecase(module: EditorUseCaseModule): Builder
        fun build(): EditorSubComponent
    }

    fun inject(fragment: EditorFragment)

    fun objectIconPickerComponent(): ObjectIconPickerComponent.Builder
    fun textBlockIconPickerComponent(): TextBlockIconPickerComponent.Builder

    // Relations

    fun documentRelationSubComponent(): DocumentRelationSubComponent.Builder
    fun relationAddToObjectComponent(): RelationAddToObjectSubComponent.Builder
    fun relationCreateFromScratchForObjectComponent(): RelationCreateFromScratchForObjectSubComponent.Builder
    fun relationCreateFromScratchForObjectBlockComponent(): RelationCreateFromScratchForObjectBlockSubComponent.Builder
    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun editDocRelationComponent(): ObjectObjectRelationValueSubComponent.Builder
    fun editRelationDateComponent(): RelationDataValueSubComponent.Builder

    fun objectCoverComponent(): SelectCoverObjectSubComponent.Builder
    fun objectUnsplashComponent(): UnsplashSubComponent.Builder
    fun objectMenuComponent(): ObjectMenuComponent.Builder

    fun objectLayoutComponent(): ObjectLayoutSubComponent.Builder
    fun objectAppearanceSettingComponent(): ObjectAppearanceSettingSubComponent.Builder
    fun objectAppearanceIconComponent(): ObjectAppearanceIconSubComponent.Builder
    fun objectAppearancePreviewLayoutComponent(): ObjectAppearancePreviewLayoutSubComponent.Builder
    fun objectAppearanceCoverComponent(): ObjectAppearanceCoverSubComponent.Builder
    fun objectAppearanceChooseDescription(): ObjectAppearanceChooseDescriptionSubComponent.Builder

    fun setBlockTextValueComponent(): SetBlockTextValueSubComponent.Builder

    fun linkToObjectOrWebBuilder(): LinkToObjectOrWebSubComponent.Builder
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
        createObjectSet: CreateObjectSet,
        createBlockLinkWithObject: CreateBlockLinkWithObject,
        createObjectAsMentionOrLink: CreateObjectAsMentionOrLink,
        documentExternalEventReducer: DocumentExternalEventReducer,
        urlBuilder: UrlBuilder,
        renderer: DefaultBlockViewRenderer,
        orchestrator: Orchestrator,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>,
        delegator: Delegator<Action>,
        detailModificationManager: DetailModificationManager,
        updateDetail: UpdateDetail,
        searchObjects: SearchObjects,
        getDefaultEditorType: GetDefaultEditorType,
        findObjectSetForType: FindObjectSetForType,
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        downloadUnsplashImage: DownloadUnsplashImage,
        setDocCoverImage: SetDocCoverImage,
        setDocImageIcon: SetDocumentImageIcon,
        editorTemplateDelegate: EditorTemplateDelegate,
        createObject: CreateObject,
        storeOfRelations: StoreOfRelations,
        storeOfObjectTypes: StoreOfObjectTypes,
        objectToSet: ConvertObjectToSet,
        featureToggles: FeatureToggles,
        tableDelegate: EditorTableDelegate
    ): EditorViewModelFactory = EditorViewModelFactory(
        openPage = openPage,
        closeObject = closePage,
        createBlockLinkWithObject = createBlockLinkWithObject,
        createObjectAsMentionOrLink = createObjectAsMentionOrLink,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        updateLinkMarks = updateLinkMarks,
        removeLinkMark = removeLinkMark,
        documentEventReducer = documentExternalEventReducer,
        urlBuilder = urlBuilder,
        renderer = renderer,
        orchestrator = orchestrator,
        analytics = analytics,
        dispatcher = dispatcher,
        delegator = delegator,
        detailModificationManager = detailModificationManager,
        updateDetail = updateDetail,
        searchObjects = searchObjects,
        getDefaultEditorType = getDefaultEditorType,
        findObjectSetForType = findObjectSetForType,
        createObjectSet = createObjectSet,
        copyFileToCacheDirectory = copyFileToCacheDirectory,
        downloadUnsplashImage = downloadUnsplashImage,
        setDocCoverImage = setDocCoverImage,
        setDocImageIcon = setDocImageIcon,
        editorTemplateDelegate = editorTemplateDelegate,
        createObject = createObject,
        storeOfRelations = storeOfRelations,
        storeOfObjectTypes = storeOfObjectTypes,
        objectToSet = objectToSet,
        featureToggles = featureToggles,
        tableDelegate = tableDelegate
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTemplateDelegate(
        getTemplates: GetTemplates,
        applyTemplate: ApplyTemplate
    ): EditorTemplateDelegate = DefaultEditorTemplateDelegate(
        getTemplates = getTemplates,
        applyTemplate = applyTemplate
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTableDelegate(
        dispatcher: Dispatcher<Payload>,
        createTableColumn: CreateTableColumn,
        createTableRow: CreateTableRow,
        deleteTableColumn: DeleteTableColumn,
        deleteTableRow: DeleteTableRow,
        duplicateTableRow: DuplicateTableRow,
        duplicateTableColumn: DuplicateTableColumn,
        fillTableRow: FillTableRow,
        fillTableColumn: FillTableColumn,
        moveTableRow: MoveTableRow,
        moveTableColumn: MoveTableColumn,
        setTableRowHeader: SetTableRowHeader
    ): EditorTableDelegate = DefaultEditorTableDelegate(
        dispatcher = dispatcher,
        createTableColumn = createTableColumn,
        createTableRow = createTableRow,
        deleteTableColumn = deleteTableColumn,
        deleteTableRow = deleteTableRow,
        duplicateTableColumn = duplicateTableColumn,
        duplicateTableRow = duplicateTableRow,
        fillTableColumn = fillTableColumn,
        fillTableRow = fillTableRow,
        moveTableColumn = moveTableColumn,
        moveTableRow = moveTableRow,
        setTableRowHeader = setTableRowHeader
    )

    @JvmStatic
    @Provides
    fun provideDefaultBlockViewRenderer(
        urlBuilder: UrlBuilder,
        toggleStateHolder: ToggleStateHolder,
        coverImageHashProvider: CoverImageHashProvider,
        storeOfRelations: StoreOfRelations,
        storeOfObjectTypes: StoreOfObjectTypes
    ): DefaultBlockViewRenderer = DefaultBlockViewRenderer(
        urlBuilder = urlBuilder,
        toggleStateHolder = toggleStateHolder,
        coverImageHashProvider = coverImageHashProvider,
        storeOfRelations = storeOfRelations,
        storeOfObjectTypes = storeOfObjectTypes
    )

    @JvmStatic
    @Provides
    fun provideCoverImageHashProvider(): CoverImageHashProvider = DefaultCoverImageHashProvider()

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
        turnInto: TurnIntoStyle,
        updateCheckbox: UpdateCheckbox,
        downloadFile: DownloadFile,
        updateText: UpdateText,
        uploadBlock: UploadBlock,
        updateFields: UpdateFields,
        updateAlignment: UpdateAlignment,
        setupBookmark: SetupBookmark,
        createBookmarkBlock: CreateBookmarkBlock,
        turnIntoDocument: TurnIntoDocument,
        createTable: CreateTable,
        fillTableRow: FillTableRow,
        setObjectType: SetObjectType,
        matcher: DefaultPatternMatcher,
        move: Move,
        copy: Copy,
        paste: Paste,
        undo: Undo,
        redo: Redo,
        setRelationKey: SetRelationKey,
        analytics: Analytics,
        updateBlocksMark: UpdateBlocksMark,
        documentFileShareDownloader: DocumentFileShareDownloader,
        clearBlockContent: ClearBlockContent,
        clearBlockStyle: ClearBlockStyle
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
        documentFileShareDownloader = documentFileShareDownloader,
        turnIntoDocument = turnIntoDocument,
        textInteractor = Interactor.TextInteractor(
            proxies = proxer,
            stores = storage,
            matcher = matcher
        ),
        updateText = updateText,
        updateAlignment = updateAlignment,
        setupBookmark = setupBookmark,
        createBookmarkBlock = createBookmarkBlock,
        move = move,
        paste = paste,
        copy = copy,
        setRelationKey = setRelationKey,
        analytics = analytics,
        updateFields = updateFields,
        turnIntoStyle = turnInto,
        updateBlocksMark = updateBlocksMark,
        setObjectType = setObjectType,
        createTable = createTable,
        fillTableRow = fillTableRow,
        clearBlockContent = clearBlockContent,
        clearBlockStyle = clearBlockStyle
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
    @Provides
    @PerScreen
    fun provideOpenPageUseCase(
        repo: BlockRepository,
        auth: AuthRepository
    ): OpenPage = OpenPage(
        repo = repo,
        auth = auth
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
    fun provideCreateObjectUseCase(
        repo: BlockRepository,
        getTemplates: GetTemplates
    ): CreateBlockLinkWithObject =
        CreateBlockLinkWithObject(repo = repo, getTemplates = getTemplates)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectAsMentionOrLink(
        repo: BlockRepository,
        getDefaultEditorType: GetDefaultEditorType,
        getTemplates: GetTemplates
    ): CreateObjectAsMentionOrLink = CreateObjectAsMentionOrLink(
        repo = repo,
        getDefaultEditorType = getDefaultEditorType,
        getTemplates = getTemplates
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDuplicateObject(
        repo: BlockRepository
    ): DuplicateObject = DuplicateObject(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideArchiveDocumentUseCase(
        repo: BlockRepository
    ): SetObjectIsArchived = SetObjectIsArchived(
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
    fun provideCreateBookmarkUseCase(
        repo: BlockRepository
    ): CreateBookmarkBlock = CreateBookmarkBlock(
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
    fun provideSetLinkAppearance(
        repo: BlockRepository
    ): SetLinkAppearance = SetLinkAppearance(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateTableUseCase(
        repo: BlockRepository
    ): CreateTable = CreateTable(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTableRowFill(
        repo: BlockRepository
    ): FillTableRow = FillTableRow(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectToSet(
        repo: BlockRepository
    ): ConvertObjectToSet = ConvertObjectToSet(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDefaultObjectRelationProvider(
        storage: Editor.Storage,
        storeOfRelations: StoreOfRelations
    ): ObjectRelationProvider = DefaultObjectRelationProvider(
        storage = storage,
        storeOfRelations = storeOfRelations
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDefaultObjectValueProvider(
        storage: Editor.Storage
    ): ObjectValueProvider = DefaultObjectValueProvider(storage.details)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        storage: Editor.Storage
    ): ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> = storage.details.current().details
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun providePayloadDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDelegator(): Delegator<Action> = Delegator.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDetailManager(
        storage: Editor.Storage
    ): DetailModificationManager = InternalDetailModificationManager(
        store = storage.details
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideTurnIntoUseCase(repo: BlockRepository): TurnIntoStyle = TurnIntoStyle(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository
    ): UpdateDetail = UpdateDetail(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository
    ): GetObjectTypes = GetObjectTypes(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddFileToObjectUseCase(
        repo: BlockRepository
    ): AddFileToObject = AddFileToObject(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetObjectType(
        repo: BlockRepository
    ): SetObjectType = SetObjectType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun searchObjects(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun findObjectSetForType(
        repo: BlockRepository
    ): FindObjectSetForType = FindObjectSetForType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectSetUseCase(
        repo: BlockRepository
    ): CreateObjectSet = CreateObjectSet(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = DefaultCopyFileToCacheDirectory(context)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDownload(repo: UnsplashRepository): DownloadUnsplashImage = DownloadUnsplashImage(
        repo = repo
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
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun getTemplates(repo: BlockRepository): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun applyTemplates(repo: BlockRepository): ApplyTemplate = ApplyTemplate(
        repo = repo,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddRelationToObject(repo: BlockRepository) = AddRelationToObject(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun providesDocumentFileShareDownloader(
        repo: BlockRepository,
        context: Context,
        fileProvider: UriFileProvider
    ): DocumentFileShareDownloader = DocumentFileShareDownloader(
        repo = repo,
        context = context.applicationContext,
        uriFileProvider = fileProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun providesDebugTreeShareDownloader(
        repo: BlockRepository,
        context: Context,
        fileProvider: UriFileProvider
    ): DebugTreeShareDownloader = DebugTreeShareDownloader(
        repo = repo,
        context = context.applicationContext,
        uriFileProvider = fileProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockListClearContent(
        repo: BlockRepository
    ): ClearBlockContent = ClearBlockContent(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockListClearStyle(
        repo: BlockRepository
    ): ClearBlockStyle = ClearBlockStyle(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableCreateColumn(
        repo: BlockRepository
    ): CreateTableColumn = CreateTableColumn(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableCreateRow(
        repo: BlockRepository
    ): CreateTableRow = CreateTableRow(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableDeleteRow(
        repo: BlockRepository
    ): DeleteTableRow = DeleteTableRow(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableDeleteColumn(
        repo: BlockRepository
    ): DeleteTableColumn = DeleteTableColumn(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableDuplicateColumn(
        repo: BlockRepository
    ): DuplicateTableColumn = DuplicateTableColumn(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableDuplicateRow(
        repo: BlockRepository
    ): DuplicateTableRow = DuplicateTableRow(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableFillColumn(
        repo: BlockRepository
    ): FillTableColumn = FillTableColumn(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableMoveColumn(
        repo: BlockRepository
    ): MoveTableColumn = MoveTableColumn(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableMoveRow(
        repo: BlockRepository
    ): MoveTableRow = MoveTableRow(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideBlockTableSetHeaderRow(
        repo: BlockRepository
    ): SetTableRowHeader = SetTableRowHeader(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun getOptions(repo: BlockRepository) = GetOptions(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getTemplates: GetTemplates,
        getDefaultEditorType: GetDefaultEditorType
    ): CreateObject = CreateObject(
        repo = repo,
        getTemplates = getTemplates,
        getDefaultEditorType = getDefaultEditorType
    )

    @Module
    interface Bindings {

        @PerScreen
        @Binds
        fun bindUriFileProvider(
            defaultProvider: DefaultUriFileProvider
        ): UriFileProvider
    }
}