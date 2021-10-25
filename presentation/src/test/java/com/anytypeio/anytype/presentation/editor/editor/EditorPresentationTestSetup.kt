package com.anytypeio.anytype.presentation.editor.editor

import MockDataFactory
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.*
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.presentation.editor.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

open class EditorPresentationTestSetup {

    val root: Id = MockDataFactory.randomString()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: CloseBlock

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var interceptThreadStatus: InterceptThreadStatus

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
    lateinit var createPage: CreatePage

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var updateAlignment: UpdateAlignment

    @Mock
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var downloadFile: DownloadFile

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
    lateinit var createDocument: CreateDocument

    @Mock
    lateinit var setRelationKey: SetRelationKey

    @Mock
    lateinit var createNewDocument: CreateNewDocument

    @Mock
    lateinit var setObjectIsArchived: SetObjectIsArchived

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var updateTitle: UpdateTitle

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var turnIntoStyle: TurnIntoStyle

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var getCompatibleObjectTypes: GetCompatibleObjectTypes

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var setObjectType: SetObjectType

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    private val builder: UrlBuilder get() = UrlBuilder(gateway)

    private lateinit var updateDetail: UpdateDetail

    open lateinit var orchestrator: Orchestrator

    open fun buildViewModel(urlBuilder: UrlBuilder = builder): EditorViewModel {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )
        updateDetail = UpdateDetail(repo)

        orchestrator = Orchestrator(
            createBlock = createBlock,
            replaceBlock = replaceBlock,
            updateTextColor = updateTextColor,
            duplicateBlock = duplicateBlock,
            downloadFile = downloadFile,
            undo = undo,
            redo = redo,
            updateTitle = updateTitle,
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
            paste = paste,
            copy = copy,
            move = move,
            turnIntoDocument = turnIntoDocument,
            analytics = analytics,
            updateFields = updateFields,
            setRelationKey = setRelationKey,
            turnIntoStyle = turnIntoStyle,
            updateBlocksMark = updateBlocksMark,
            setObjectType = setObjectType
        )

        return EditorViewModel(
            openPage = openPage,
            closePage = closePage,
            createPage = createPage,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            createObject = createObject,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                counter = Counter.Default(),
                coverImageHashProvider = coverImageHashProvider
            ),
            setObjectIsArchived = setObjectIsArchived,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            analytics = analytics,
            orchestrator = orchestrator,
            dispatcher = Dispatcher.Default(),
            detailModificationManager = InternalDetailModificationManager(storage.details),
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider,
            searchObjects = searchObjects,
            getDefaultEditorType = getDefaultEditorType
        )
    }

    fun stubOpenDocument(
        document: List<Block> = emptyList(),
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList(),
        objectRestrictions: List<ObjectRestriction> = emptyList()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = details,
                                relations = relations,
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
        interceptThreadStatus.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
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
        params: UnlinkBlocks.Params = any(),
        events: List<Event> = emptyList()
    ) {
        unlinkBlocks.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubTurnIntoDocument(
        params: TurnIntoDocument.Params = any()
    ) {
        turnIntoDocument.stub {
            onBlocking { invoke(params) } doReturn Either.Right(emptyList())
        }
    }

    fun stubCreateBlock(root: String) {
        createBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
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

    fun stubCreateObject(root: String, target: String) {
        createObject.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                CreateObject.Result(
                    id = root,
                    target = target,
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
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
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

    fun stubGetObjectTypes(objectTypes: List<ObjectType> = listOf()) {
        getCompatibleObjectTypes.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                objectTypes
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

    fun stubSearchObjects() {
        searchObjects.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(listOf())
        }
    }

    fun stubGetDefaultObjectType(type: String?) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn Either.Right(GetDefaultEditorType.Response(type))
        }
    }
}