package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_ui.features.page.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.*
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.page.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.Bridge
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mock

open class EditorPresentationTestSetup {

    val root: Id = MockDataFactory.randomString()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: ClosePage

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var getListPages: GetListPages

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
    lateinit var createNewDocument: CreateNewDocument

    @Mock
    lateinit var archiveDocument: ArchiveDocument

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var updateTitle: UpdateTitle

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var gateway : Gateway

    private val builder: UrlBuilder get() = UrlBuilder(gateway)

    open fun buildViewModel(urlBuilder: UrlBuilder = builder): PageViewModel {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )

        return PageViewModel(
            getListPages = getListPages,
            openPage = openPage,
            closePage = closePage,
            createPage = createPage,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                counter = Counter.Default()
            ),
            archiveDocument = archiveDocument,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            analytics = analytics,
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
                updateFields = updateFields
            ),
            bridge = Bridge()
        )
    }

    fun stubOpenDocument(
        document: List<Block> = emptyList(),
        details: Block.Details = Block.Details()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowBlock(
                                context = root,
                                root = root,
                                details = details,
                                blocks = document
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

    fun stubObserveEvents(flow: Flow<List<Event>> = flowOf()) {
        interceptEvents.stub { onBlocking { build() } doReturn flow }
    }

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf()
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
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

    fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
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

}