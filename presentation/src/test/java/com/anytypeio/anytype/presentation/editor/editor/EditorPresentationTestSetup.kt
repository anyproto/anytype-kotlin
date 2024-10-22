package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.ClearBlockContent
import com.anytypeio.anytype.domain.block.interactor.ClearBlockStyle
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.MoveOld
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
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
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.ConvertObjectToSet
import com.anytypeio.anytype.domain.`object`.SetObjectInternalFlags
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.CreateObjectAsMentionOrLink
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmarkBlock
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.SetRelationKey
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
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
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.table.DefaultEditorTableDelegate
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableDelegate
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.home.UserPermissionProviderStub
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.dispatchers
import com.anytypeio.anytype.presentation.util.downloader.DocumentFileShareDownloader
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

open class EditorPresentationTestSetup {

    val root: Id = MockDataFactory.randomString()
    val defaultSpace: Id = MockDataFactory.randomUuid()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: CloseBlock

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var updateCheckbox: UpdateCheckbox

    @Mock
    lateinit var duplicateBlock: DuplicateBlock

    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var clearLastOpenedObject: ClearLastOpenedObject

    @Mock
    lateinit var updateBlocksMark: UpdateBlocksMark

    @Mock
    lateinit var updateDivider: UpdateDivider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var updateLinkMark: UpdateLinkMarks

    @Mock
    lateinit var removeLinkMark: RemoveLinkMark

    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var splitBlock: SplitBlock

    @Mock
    lateinit var createBlockLinkWithObject: CreateBlockLinkWithObject

    @Mock
    lateinit var updateAlignment: UpdateAlignment

    @Mock
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var downloadFile: DownloadFile

    @Mock
    lateinit var documentFileShareDownloader: DocumentFileShareDownloader

    @Mock
    lateinit var uploadBlock: UploadBlock

    @Mock
    lateinit var updateFields: UpdateFields

    @Mock
    lateinit var paste: Paste

    @Mock
    lateinit var copy: Copy

    @Mock
    lateinit var undo: Undo

    @Mock
    lateinit var redo: Redo

    @Mock
    lateinit var setupBookmark: SetupBookmark

    @Mock
    lateinit var createBookmarkBlock: CreateBookmarkBlock

    @Mock
    lateinit var setRelationKey: SetRelationKey

    @Mock
    lateinit var createObjectAsMentionOrLink: CreateObjectAsMentionOrLink

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var move: MoveOld

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var turnIntoStyle: TurnIntoStyle

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var setObjectType: SetObjectType

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var applyTemplate: ApplyTemplate

    @Mock
    lateinit var getDefaultObjectType: GetDefaultObjectType

    @Mock
    lateinit var findObjectSetForType: FindObjectSetForType

    @Mock
    lateinit var createObjectSet: CreateObjectSet

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var getTemplates: GetTemplates

    @Mock
    lateinit var createTable: CreateTable

    @Mock
    lateinit var fillTableRow: FillTableRow

    @Mock
    lateinit var convertObjectToCollection: ConvertObjectToCollection

    lateinit var tableDelegate: EditorTableDelegate

    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var templatesContainer: ObjectTypeTemplatesContainer

    protected val builder: UrlBuilder get() = UrlBuilder(gateway)

    private lateinit var updateDetail: UpdateDetail
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var setDocImageIcon: SetDocumentImageIcon
    private lateinit var objectToSet: ConvertObjectToSet
    private lateinit var clearBlockContent: ClearBlockContent
    private lateinit var clearBlockStyle: ClearBlockStyle

    @Mock
    lateinit var createTableColumn: CreateTableColumn

    @Mock
    lateinit var createTableRow: CreateTableRow

    @Mock
    lateinit var deleteTableColumn: DeleteTableColumn

    @Mock
    lateinit var deleteTableRow: DeleteTableRow

    @Mock
    lateinit var duplicateTableRow: DuplicateTableRow

    @Mock
    lateinit var duplicateTableColumn: DuplicateTableColumn

    @Mock
    lateinit var fillTableColumn: FillTableColumn

    @Mock
    lateinit var moveTableRow: MoveTableRow

    @Mock
    lateinit var moveTableColumn: MoveTableColumn

    @Mock
    lateinit var addRelationToObject: AddRelationToObject

    @Mock
    lateinit var setTableRowHeader: SetTableRowHeader

    open lateinit var orchestrator: Orchestrator

    private val delegator = Delegator.Default<Action>()

    protected val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    protected val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    @Mock
    lateinit var getObjectTypes: GetObjectTypes

    @Mock
    lateinit var interceptFileLimitEvents: InterceptFileLimitEvents

    @Mock
    lateinit var setObjectInternalFlags: SetObjectInternalFlags

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var getNetworkMode: GetNetworkMode

    @Mock
    lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider

    var permissions: UserPermissionProvider = UserPermissionProviderStub()

    open fun buildViewModel(urlBuilder: UrlBuilder = builder): EditorViewModel {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )
        objectToSet = ConvertObjectToSet(repo, dispatchers)
        updateDetail = UpdateDetail(repo)
        setDocCoverImage = SetDocCoverImage(repo)
        setDocImageIcon = SetDocumentImageIcon(repo)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)
        clearBlockContent = ClearBlockContent(repo)
        clearBlockStyle = ClearBlockStyle(repo)

        orchestrator = Orchestrator(
            createBlock = createBlock,
            replaceBlock = replaceBlock,
            updateTextColor = updateTextColor,
            duplicateBlock = duplicateBlock,
            downloadFile = downloadFile,
            documentFileShareDownloader = documentFileShareDownloader,
            undo = undo,
            redo = redo,
            updateText = updateText,
            updateCheckbox = updateCheckbox,
            updateTextStyle = updateTextStyle,
            updateBackgroundColor = updateBackgroundColor,
            mergeBlocks = mergeBlocks,
            uploadBlock = uploadBlock,
            splitBlock = splitBlock,
            unlinkBlocks = unlinkBlocks,
            updateDivider = updateDivider,
            memory = memory,
            stores = storage,
            proxies = proxies,
            textInteractor = Interactor.TextInteractor(
                proxies = proxies,
                stores = storage,
                matcher = DefaultPatternMatcher()
            ),
            updateAlignment = updateAlignment,
            setupBookmark = setupBookmark,
            createBookmarkBlock = createBookmarkBlock,
            paste = paste,
            copy = copy,
            move = move,
            turnIntoDocument = turnIntoDocument,
            analytics = analytics,
            updateFields = updateFields,
            setRelationKey = setRelationKey,
            turnIntoStyle = turnIntoStyle,
            updateBlocksMark = updateBlocksMark,
            createTable = createTable,
            fillTableRow = fillTableRow,
            clearBlockContent = clearBlockContent,
            clearBlockStyle = clearBlockStyle,
            analyticsSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceManager = spaceManager
        )

        dispatcher = Dispatcher.Default()

        tableDelegate = DefaultEditorTableDelegate(
            dispatcher, createTableColumn, createTableRow,
            deleteTableColumn, deleteTableRow, duplicateTableRow,
            duplicateTableColumn, fillTableRow, fillTableColumn,
            moveTableRow, moveTableColumn, setTableRowHeader
        )

        return EditorViewModel(
            openPage = openPage,
            closePage = closePage,
            createBlockLinkWithObject = createBlockLinkWithObject,
            createObjectAsMentionOrLink = createObjectAsMentionOrLink,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                coverImageHashProvider = coverImageHashProvider,
                storeOfRelations = storeOfRelations,
                storeOfObjectTypes = storeOfObjectTypes
            ),
            orchestrator = orchestrator,
            analytics = analytics,
            dispatcher = dispatcher,
            delegator = delegator,
            updateDetail = updateDetail,
            searchObjects = searchObjects,
            getDefaultObjectType = getDefaultObjectType,
            findObjectSetForType = findObjectSetForType,
            createObjectSet = createObjectSet,
            copyFileToCache = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            createObject = createObject,
            objectToSet = objectToSet,
            objectToCollection = convertObjectToCollection,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            featureToggles = mock(),
            tableDelegate = tableDelegate,
            getObjectTypes = getObjectTypes,
            interceptFileLimitEvents = interceptFileLimitEvents,
            addRelationToObject = addRelationToObject,
            spaceManager = spaceManager,
            applyTemplate = applyTemplate,
            setObjectType = setObjectType,
            templatesContainer = templatesContainer,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            dispatchers = dispatchers,
            vmParams = EditorViewModel.Params(
                ctx = root,
                space = SpaceId(defaultSpace)
            ),
            permissions = permissions,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            clearLastOpenedObject = clearLastOpenedObject,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            getNetworkMode = getNetworkMode
        )
    }

    fun stubGetNetworkMode() {
        getNetworkMode.stub {
            onBlocking { run(Unit) } doReturn NetworkModeConfig()
        }
    }

    fun stubOpenDocument(
        document: List<Block> = emptyList(),
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList(),
        objectRestrictions: List<ObjectRestriction> = emptyList(),
        relationLinks: List<RelationLink> = emptyList(),
        spaceId: SpaceId = SpaceId(defaultSpace)
    ) {
        openPage.stub {
            onBlocking {
                async(
                    OpenPage.Params(
                        obj = root,
                        saveAsLastOpened = true,
                        space = spaceId
                    )
                )
            } doReturn Resultat.success(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = details,
                                relations = relations,
                                relationLinks = relationLinks,
                                blocks = document,
                                objectRestrictions = objectRestrictions
                            )
                        )
                    )
                )
            )
        }
    }

    fun stubMove() {
        move.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf(),
        stubInterceptThreadStatus: Boolean = true
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
        if (stubInterceptThreadStatus) stubInterceptThreadStatus()
    }

    fun stubInterceptThreadStatus() {
        spaceSyncAndP2PStatusProvider.stub {
            onBlocking { observe() } doReturn emptyFlow()
        }
    }

    fun stubUpdateTextStyle(
        params: UpdateTextStyle.Params? = null,
        events: List<Event> = emptyList()
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(params ?: any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubTurnIntoStyle(
        params: TurnIntoStyle.Params? = null,
        events: List<Event> = emptyList()
    ) {
        turnIntoStyle.stub {
            onBlocking { invoke(params ?: any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubUnlinkBlocks(
        params: UnlinkBlocks.Params? = null,
        events: List<Event> = emptyList()
    ) {
        unlinkBlocks.stub {
            onBlocking {
                if (params == null) invoke(any()) else invoke(params)
            } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubCreateBlock(root: String) {
        createBlock.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                Pair(
                    MockDataFactory.randomString(), Payload(
                        context = root,
                        events = listOf()
                    )
                )
            )

        }
    }

    fun stubMergeBlocks(root: String) {
        mergeBlocks.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    fun stubCreateBlockLinkWithObject(root: String, target: String) {
        createBlockLinkWithObject.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(
                CreateBlockLinkWithObject.Result(
                    id = root,
                    objectId = target,
                    payload = Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    fun stubCopy() {
        copy.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    fun stubPaste() {
        paste.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Response.Clipboard.Paste(
                    0,
                    true,
                    emptyList(),
                    Payload("", emptyList())
                )
            )
        }
    }


    fun stubReplaceBlock() {
        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomUuid(),
                    Payload(context = root, events = emptyList())
                )
            )
        }
    }

    fun stubSplitBlock() {
        splitBlock.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomUuid(),
                    Payload(context = root, events = emptyList())
                )
            )
        }
    }

    fun stubClosePage() {
        closePage.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    fun stubUpdateBackground() {
        updateBackgroundColor.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Payload(context = root, events = emptyList())
            )
        }
    }

    fun stubUpdateTextColor() {
        updateTextColor.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Payload(context = root, events = emptyList())
            )
        }
    }

    fun stubUpdateBlocksMark() {
        updateBlocksMark.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Payload(context = root, events = emptyList())
            )
        }
    }

    fun stubDuplicateBlock(
        new: Id,
        events: List<Event>
    ) {
        duplicateBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    listOf(new),
                    Payload(
                        context = root,
                        events = events
                    )
                )
            )
        }
    }

    fun stubSearchObjects(objects: List<ObjectWrapper.Basic> = emptyList()) {
        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(objects)
        }
    }

    fun stubGetDefaultObjectType(
        id: String = MockDataFactory.randomString(),
        type: String = MockDataFactory.randomString(),
        name: String? = null,
        template: String? = null
    ) {
        getDefaultObjectType.stub {
            onBlocking { async(
                params = SpaceId(defaultSpace)
            ) } doReturn Resultat.success(
                GetDefaultObjectType.Response(
                    id = TypeId(id),
                    type = TypeKey(type),
                    name = name,
                    defaultTemplate = template
                )
            )
        }
    }

    fun stubCreateObjectAsMentionOrLink(name: String, id: String) {
        val result = CreateObjectAsMentionOrLink.Result(id, name)
        createObjectAsMentionOrLink.stub {
            onBlocking { async(any()) } doReturn Resultat.success(result)
        }
    }

    protected fun stubGetTemplates(
        templates: List<ObjectWrapper.Basic> = emptyList()
    ) {
        getTemplates.stub {
            onBlocking { run(any()) } doReturn templates
        }
    }

    fun stubFillRow() {
        fillTableRow.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = listOf()
                )
            )
        }
    }

    fun stubGetObjectTypes(types: List<ObjectWrapper.Type>) {
        getObjectTypes.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(types)
        }
    }

    fun stubFileLimitEvents() {
        interceptFileLimitEvents.stub {
            onBlocking { run(Unit) } doReturn emptyFlow()
        }
    }

    fun stubSetInternalFlags() {
        setObjectInternalFlags.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Payload(root, emptyList()))
        }
    }

    fun stubSpaceManager(space: String = defaultSpace) {
        spaceManager.stub {
            onBlocking { get() } doReturn space
        }
    }

    fun stubUserPermission(
        space: SpaceId = SpaceId(defaultSpace),
        permission: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) {
        (permissions as UserPermissionProviderStub).stubObserve(
            space, permission
        )
    }

    fun stubAnalyticSpaceHelperDelegate() {
        Mockito.`when`(analyticSpaceHelperDelegate.provideParams(defaultSpace))
            .thenReturn(AnalyticSpaceHelperDelegate.Params.EMPTY)

        analyticSpaceHelperDelegate.stub {
            on { provideParams(defaultSpace) } doReturn AnalyticSpaceHelperDelegate.Params.EMPTY
        }
    }

    fun proceedWithDefaultBeforeTestStubbing() {
        stubAnalyticSpaceHelperDelegate()
        stubSpaceManager()
        stubUserPermission()
        stubGetNetworkMode()
        stubFileLimitEvents()
    }
}