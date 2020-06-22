package com.agileburo.anytype.features.editor.base

import com.agileburo.anytype.core_ui.features.page.pattern.DefaultPatternMatcher
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.clipboard.Clipboard
import com.agileburo.anytype.domain.clipboard.Copy
import com.agileburo.anytype.domain.clipboard.Paste
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.domain.page.bookmark.SetupBookmark
import com.agileburo.anytype.mocking.MockDataFactory
import com.agileburo.anytype.presentation.page.DocumentExternalEventReducer
import com.agileburo.anytype.presentation.page.Editor
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.presentation.page.editor.Interactor
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.editor.Proxy
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import org.mockito.Mock
import org.mockito.MockitoAnnotations

open class EditorTestSetup {

    lateinit var archiveDocument: ArchiveDocument
    lateinit var createDocument: CreateDocument
    lateinit var downloadFile: DownloadFile
    lateinit var undo: Undo
    lateinit var redo: Redo
    lateinit var copy: Copy
    lateinit var paste: Paste
    lateinit var updateTitle: UpdateTitle
    lateinit var updateAlignment: UpdateAlignment
    lateinit var replaceBlock: ReplaceBlock
    lateinit var setupBookmark: SetupBookmark
    lateinit var uploadBlock: UploadBlock
    lateinit var splitBlock: SplitBlock
    lateinit var createPage: CreatePage
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var openPage: OpenPage
    @Mock
    lateinit var closePage: ClosePage
    @Mock
    lateinit var updateText: UpdateText
    @Mock
    lateinit var createBlock: CreateBlock
    @Mock
    lateinit var interceptEvents: InterceptEvents
    @Mock
    lateinit var updateCheckbox: UpdateCheckbox
    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks
    @Mock
    lateinit var duplicateBlock: DuplicateBlock
    @Mock
    lateinit var updateTextStyle: UpdateTextStyle
    @Mock
    lateinit var updateTextColor: UpdateTextColor
    @Mock
    lateinit var updateLinkMarks: UpdateLinkMarks
    @Mock
    lateinit var removeLinkMark: RemoveLinkMark
    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var uriMatcher: Clipboard.UriMatcher
    @Mock
    lateinit var repo: BlockRepository
    @Mock
    lateinit var clipboard: Clipboard

    val root: String = "rootId123"

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomString(),
        profile = MockDataFactory.randomUuid()
    )

    private val urlBuilder = UrlBuilder(
        config = config
    )

    private val intents = Proxy.Intents()

    private val stores = Editor.Storage()

    private val proxies = Editor.Proxer(
        intents = intents
    )

    open fun setup() {
        MockitoAnnotations.initMocks(this)

        splitBlock = SplitBlock(repo)
        createPage = CreatePage(repo)
        archiveDocument = ArchiveDocument(repo)
        createDocument = CreateDocument(repo)
        undo = Undo(repo)
        redo = Redo(repo)
        replaceBlock = ReplaceBlock(repo)
        setupBookmark = SetupBookmark(repo)
        updateAlignment = UpdateAlignment(repo)
        updateTitle = UpdateTitle(repo)
        uploadBlock = UploadBlock(repo)
        downloadFile = DownloadFile(
            downloader = mock(),
            context = Dispatchers.Main
        )
        copy = Copy(
            repo = repo,
            clipboard = clipboard
        )

        paste = Paste(
            repo = repo,
            clipboard = clipboard,
            matcher = uriMatcher
        )

        updateBackgroundColor = UpdateBackgroundColor(repo)

        TestPageFragment.testViewModelFactory = PageViewModelFactory(
            openPage = openPage,
            closePage = closePage,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createPage = createPage,
            documentEventReducer = DocumentExternalEventReducer(),
            archiveDocument = archiveDocument,
            createDocument = createDocument,
            uploadUrl = uploadBlock,
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                counter = Counter.Default(),
                toggleStateHolder = ToggleStateHolder.Default()
            ),
            interactor = Orchestrator(
                createBlock = createBlock,
                splitBlock = splitBlock,
                unlinkBlocks = unlinkBlocks,
                updateCheckbox = updateCheckbox,
                updateTextStyle = updateTextStyle,
                updateText = updateText,
                updateBackgroundColor = updateBackgroundColor,
                undo = undo,
                redo = redo,
                copy = copy,
                paste = paste,
                duplicateBlock = duplicateBlock,
                updateAlignment = updateAlignment,
                downloadFile = downloadFile,
                mergeBlocks = mergeBlocks,
                updateTitle = updateTitle,
                updateTextColor = updateTextColor,
                replaceBlock = replaceBlock,
                setupBookmark = setupBookmark,
                memory = Editor.Memory(
                    selections = SelectionStateHolder.Default()
                ),
                stores = stores,
                proxies = proxies,
                textInteractor = Interactor.TextInteractor(
                    proxies = proxies,
                    stores = stores,
                    matcher = DefaultPatternMatcher()
                )
            )
        )
    }

    /**
     * STUBBING
     */

    fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build() } doReturn emptyFlow()
        }
    }

    fun stubOpenDocument(
        document: List<Block>,
        details: Block.Details = Block.Details()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
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
        }
    }

    fun stubSplitBlocks(
        command: Command.Split,
        new: Id,
        events: List<Event.Command>
    ) {
        repo.stub {
            onBlocking {
                split(command = command)
            } doReturn Pair(new, Payload(context = root, events = events))
        }
    }

    fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }
}