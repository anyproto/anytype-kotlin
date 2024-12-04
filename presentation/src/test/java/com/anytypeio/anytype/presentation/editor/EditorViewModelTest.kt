package com.anytypeio.anytype.presentation.editor


import android.net.Uri
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.StubNumbered
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.parseThemeTextColor
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.Mimetype
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
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.ConvertObjectToSet
import com.anytypeio.anytype.domain.`object`.SetObjectInternalFlags
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.CreateObjectAsMentionOrLink
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.UpdateTitle
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmarkBlock
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.SetRelationKey
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.table.CreateTable
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.Interactor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableDelegate
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.presentation.util.dispatchers
import com.anytypeio.anytype.presentation.util.downloader.DocumentFileShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
open class EditorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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
    lateinit var updateDivider: UpdateDivider

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var updateLinkMark: UpdateLinkMarks

    @Mock
    lateinit var setRelationKey: SetRelationKey

    @Mock
    lateinit var updateBlocksMark: UpdateBlocksMark

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
    lateinit var createObjectAsMentionOrLink: CreateObjectAsMentionOrLink

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var updateTitle: UpdateTitle

    @Mock
    lateinit var move: MoveOld

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var turnIntoStyle: TurnIntoStyle

    @Mock
    lateinit var updateFields: UpdateFields

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var setObjectType: SetObjectType

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var getDefaultObjectType: GetDefaultObjectType

    @Mock
    lateinit var findObjectSetForType: FindObjectSetForType

    @Mock
    lateinit var createObjectSet: CreateObjectSet

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    @Mock
    lateinit var applyTemplate: ApplyTemplate

    @Mock
    lateinit var fillTableRow: FillTableRow

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var templatesContainer: ObjectTypeTemplatesContainer

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var createTable: CreateTable

    @Mock
    lateinit var tableDelegate: EditorTableDelegate

    @Mock
    lateinit var convertObjectToCollection: ConvertObjectToCollection

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var getNetworkMode: GetNetworkMode

    private lateinit var updateDetail: UpdateDetail

    @Mock
    lateinit var interceptFileLimitEvents: InterceptFileLimitEvents

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    @Mock
    lateinit var permissions: UserPermissionProvider

    @Mock
    lateinit var clearLastOpenedObject: ClearLastOpenedObject

    @Mock
    lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider

    @Mock
    lateinit var fieldParser: FieldParser

    lateinit var vm: EditorViewModel

    lateinit var orchestrator: Orchestrator

    private lateinit var builder: UrlBuilder
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var setDocImageIcon: SetDocumentImageIcon
    private lateinit var objectToSet: ConvertObjectToSet
    private lateinit var clearBlockContent: ClearBlockContent
    private lateinit var clearBlockStyle: ClearBlockStyle
    private lateinit var setObjectInternalFlags: SetObjectInternalFlags

    val root = MockDataFactory.randomUuid()

    val delegator = Delegator.Default<Action>()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    val defaultSpace = MockDataFactory.randomString()
    val spaceId = SpaceId(defaultSpace)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        builder = UrlBuilder(gateway)
        stubNetworkMode()
        stubObserveEvents()
        stubInterceptEvents()
        stubUserPermission()
        spaceManager.stub {
            onBlocking {
                get()
            } doReturn defaultSpace
        }
        stubAnalyticSpaceHelperDelegate()
        stubFileLimitEvents()
        stubUpdateBlocksMark()
        stubOpenPage(root, emptyList())
        stubUpdateText()
        openPage.stub {
            onBlocking {
                async(any())
            } doReturn Resultat.success(
                Result.Success(
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    @Test
    fun `should not start observing events when view model is initialized`() {
        givenViewModel()
        verifyNoInteractions(interceptEvents)
    }

    fun stubNetworkMode() {
        getNetworkMode.stub {
            onBlocking { run(Unit) } doReturn NetworkModeConfig()
        }
    }

    @Test
    fun `should start opening page when requested`() {
        val param = OpenPage.Params(
            obj = root,
            saveAsLastOpened = true,
            space = spaceId
        )

        stubInterceptEvents()
        givenViewModel()
        stubOpenPage(context = root)

        vm.onStart(id = root, space = defaultSpace)

        runBlockingTest { verify(openPage, times(1)).async(param) }
    }

    @Test
    fun `should dispatch a page to UI when this view model receives an appropriate command`() {

        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(id = child)

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )

        stubInterceptEvents()

        givenViewModel(builder)

        vm.onStart(id = root, space = defaultSpace)

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content<Block.Content.Text>().text,
                    background = paragraph.parseThemeBackgroundColor(),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should close page when the system back button is pressed`() {
        runTest {
            val root = MockDataFactory.randomUuid()

            stubOpenPage(root)
            stubInterceptEvents()
            stubClosePage(null, null)

            givenViewModel()

            vm.onStart(id = root, space = defaultSpace)

            verifyNoInteractions(closePage)

            vm.onSystemBackPressed(editorHasChildrenScreens = false)

            verify(closePage, times(1)).async(any())
        }
    }

    @Test
    fun `should emit an appropriate navigation command when the page is closed`() {
        stubInterceptEvents()
        stubClosePage()
        givenViewModel()

        val testObserver = vm.navigation.test()

        verifyNoInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should update block when its text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = root
        val text = MockDataFactory.randomString()

        stubInterceptEvents()
        givenViewModel()
        stubOpenPage(context = pageId)
        stubUpdateText()

        vm.onStart(id = pageId, space = defaultSpace)

        val blockView = BlockView.Text.Paragraph(id = blockId, text = text)
        vm.onTextBlockTextChanged(blockView)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should not debounce values when dispatching text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        stubUpdateText()
        stubOpenPage(context = pageId)
        givenViewModel()

        vm.onStart(id = pageId, space = defaultSpace)

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = blockId,
                text = text
            )
        )
        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = blockId,
                text = text
            )
        )
        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = blockId,
                text = text
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = blockId,
                text = text
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(4)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should add a new block to the already existing one when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(
            id = child
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val added = StubParagraph()

        stubObserveEvents(
            flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = root,
                            children = listOf(header.id, child, added.id)
                        )
                    )
                )
                emit(
                    listOf(
                        Event.Command.AddBlock(
                            blocks = listOf(added),
                            context = root
                        )
                    )
                )
            }
        )

        stubOpenPage()
        givenViewModel(builder)
        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(200)

        val expected =
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content.asText().text,
                        background = paragraph.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = paragraph.parseThemeBackgroundColor()
                            )
                        )
                    ),
                    BlockView.Text.Paragraph(
                        id = added.id,
                        text = added.content.asText().text,
                        background = added.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = added.parseThemeBackgroundColor()
                            )
                        )
                    )
                )
            )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start creating a new block if user clicked create-text-block-button`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomString()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowObject(
                    context = root,
                    blocks = listOf(smart, header, title, paragraph),
                    root = root
                )
            )
        )

        stubCreateBlock(root)

        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = true)

        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)

        runBlockingTest {
            verify(createBlock, times(1)).async(any())
        }
    }

    @Test
    fun `should update block text without dispatching it to UI when we receive an appropriate event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(id = child)

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val text = MockDataFactory.randomString()

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateBlockText(
                            text = text,
                            id = child,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()
        givenViewModel(builder)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val beforeUpdate = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    background = paragraph.parseThemeBackgroundColor(),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        vm.state.test().assertValue(beforeUpdate)

        coroutineTestRule.advanceTime(200)

        val afterUpdate = beforeUpdate.copy()

        vm.state.test().assertValue(afterUpdate)
    }

    @Test
    fun `should emit loading state when starting opening a page`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage()
        stubObserveEvents()
        givenViewModel()

        val testObserver = vm.state.test()

        vm.onStart(id = root, space = defaultSpace)

        testObserver.assertValue(ViewState.Loading)
    }

    @Test
    fun `should apply two different markup actions`() {

        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = ThemeColor.RED.code
            ),
            children = emptyList(),
            backgroundColor = ThemeColor.YELLOW.code
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        givenViewModel(builder)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content.asText().parseThemeTextColor(),
                    background = paragraph.parseThemeBackgroundColor(),
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    ),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        val secondTimeRange = 0..5

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.ITALIC)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content.asText().parseThemeTextColor(),
                    background = paragraph.parseThemeBackgroundColor(),
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        ),
                        Markup.Mark.Italic(
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    ),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        coroutineTestRule.advanceUntilIdle()

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should apply two markup actions of the same markup type`() {

        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = ThemeColor.RED.code
            ),
            backgroundColor = "yellow",
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        stubUserPermission()
        stubAnalyticSpaceHelperDelegate()
        givenViewModel(builder)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content.asText().parseThemeTextColor(),
                    background = paragraph.parseThemeBackgroundColor(),
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    ),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        assertEquals(firstTimeExpected, vm.state.value)

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 3..3
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 0..0
        )

        val secondTimeRange = 0..5

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content.asText().parseThemeTextColor(),
                    background = paragraph.parseThemeBackgroundColor(),
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    ),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        coroutineTestRule.advanceUntilIdle()

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should dispatch texts changes and markup even if only markup is changed`() = runTest {

        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(
            id = child,
            textColor = ThemeColor.RED.code,
            backgroundColor = ThemeColor.YELLOW.code
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubUpdateText()
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val range = 0..3

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = range
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                range = range
            )
        )

        verify(updateText, times(1)).invoke(
            params = eq(
                UpdateText.Params(
                    target = paragraph.id,
                    marks = marks,
                    context = page.id,
                    text = paragraph.content.asText().text
                )
            )
        )
    }

    @Test
    fun `test changes from UI do not trigger re-rendering`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(id = child)

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        givenViewModel(builder)
        stubUpdateText()

        val testObserver = vm.state.test()

        vm.onStart(id = root, space = defaultSpace)

        testObserver.assertValue(ViewState.Loading)

        coroutineTestRule.advanceTime(100)

        val state = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    background = paragraph.parseThemeBackgroundColor(),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        testObserver.assertValue(state).assertHistorySize(2)

        val userInput = MockDataFactory.randomString()

        val range = 0..3

        val blockView = BlockView.Text.Paragraph(
            id = paragraph.id,
            text = userInput,
            marks = listOf(Markup.Mark.Bold(from = range.first, to = range.last))
        )

        vm.onTextBlockTextChanged(blockView)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        testObserver.assertValue(state).assertHistorySize(2)
    }

    @Test
    fun `should update text inside view state when user changed text`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()

        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()

        val blockView = BlockView.Text.Paragraph(
            id = paragraph.id,
            text = userInput
        )

        vm.onTextBlockTextChanged(blockView)

        val contentAfterChange = Block.Content.Text(
            text = userInput,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraphAfterChange = paragraph.copy(
            content = contentAfterChange
        )

        val expected = listOf(
            page,
            header,
            title,
            paragraphAfterChange
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        assertEquals(
            expected = expected,
            actual = vm.blocks
        )
    }

    @Test
    fun `should dispatch text changes including markup to the middleware`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()
        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        val blockView = BlockView.Text.Paragraph(
            id = paragraph.id,
            text = userInput,
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 5)
            )
        )

        vm.onTextBlockTextChanged(blockView)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        target = paragraph.id,
                        text = userInput,
                        marks = marks,
                        context = page.id
                    )
                )
            )
        }
    }

    @Test
    fun `should receive initial control panel state when view model is initialized`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(1001)

        val expected = ControlPanelState.init()

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should add a header-one block on add-header-one event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = StubParagraph(id = child)

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child)
        )

        val style = Block.Content.Text.Style.H1

        val new = Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val flow: Flow<List<Event>> = flow {
            delay(500)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = listOf(page, header, title, paragraph),
                        context = root
                    )
                )
            )
            delay(500)
            emit(
                listOf(
                    Event.Command.UpdateStructure(
                        context = root,
                        id = root,
                        children = listOf(header.id, child, new.id)
                    )
                )
            )
            emit(
                listOf(
                    Event.Command.AddBlock(
                        blocks = listOf(new),
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()

        givenViewModel(builder)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(500)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        background = paragraph.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = paragraph.parseThemeBackgroundColor()
                            )
                        )
                    )
                )
            )
        )

        coroutineTestRule.advanceTime(500)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        background = paragraph.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = paragraph.parseThemeBackgroundColor()
                            )
                        )
                    ),
                    BlockView.Text.Header.One(
                        id = new.id,
                        text = new.content<Block.Content.Text>().text,
                        background = new.parseThemeBackgroundColor(),
                        indent = 0,
                        decorations = listOf(
                            BlockView.Decoration(
                                background = new.parseThemeBackgroundColor(),
                                style = BlockView.Decoration.Style.Header.H1
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `should start duplicating focused block when requested`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        givenViewModel()
        stubDuplicateBlock(
            newBlockId = MockDataFactory.randomString(),
            root = root
        )


        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child, hasFocus = true)

        vm.onBlockToolbarBlockActionsClicked()

        vm.onBlockFocusChanged(id = child, hasFocus = false)

        vm.onMultiSelectAction(ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        target = child,
                        context = root,
                        blocks = listOf(child)
                    )
                )
            )
        }
    }

    @Test
    fun `should start deleting focused block when requested`() {

        // SETUP

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubUnlinkBlocks(root = root)
        stubObserveEvents(events)
        givenViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onSelectionChanged(id = child.id, selection = IntRange(0, 0))
        vm.onBlockFocusChanged(id = child.id, hasFocus = true)
        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = child.id, hasFocus = false)
        vm.onMultiSelectModeDeleteClicked()
        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        runBlockingTest {
            verify(unlinkBlocks, times(1)).invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should delete the first block when the delete-block event received for the first block, then rerender the page`() {

        val pageOpenedDelay = 100L
        val blockDeletedEventDelay = 100L

        val root = MockDataFactory.randomUuid()

        val firstChild = StubParagraph(
            id = "FIRST CHILD"
        )

        val secondChild = StubParagraph(
            id = "SECOND CHILD"
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, firstChild.id, secondChild.id)
        )

        val doc = listOf(page, header, title, firstChild, secondChild)

        val events: Flow<List<Event.Command>> = flow {
            delay(pageOpenedDelay)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
            delay(blockDeletedEventDelay)
            emit(
                listOf(
                    Event.Command.DeleteBlock(
                        targets = listOf(firstChild.id),
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        givenViewModel(builder)
        stubUnlinkBlocks(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(pageOpenedDelay)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = firstChild.id,
                        text = firstChild.content<Block.Content.Text>().text,
                        background = firstChild.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = firstChild.parseThemeBackgroundColor()
                            )
                        )
                    ),
                    BlockView.Text.Paragraph(
                        id = secondChild.id,
                        text = secondChild.content<Block.Content.Text>().text,
                        background = secondChild.parseThemeBackgroundColor(),
                        decorations = listOf(
                            BlockView.Decoration(
                                background = secondChild.parseThemeBackgroundColor()
                            )
                        )
                    )
                )
            )
        )

        vm.onBlockFocusChanged(id = firstChild.id, hasFocus = true)
        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = firstChild.id, hasFocus = false)
        vm.onMultiSelectModeDeleteClicked()
        vm.onExitMultiSelectModeClicked()

        assertEquals(expected = 5, actual = vm.blocks.size)

        coroutineTestRule.advanceTime(blockDeletedEventDelay)

        assertEquals(expected = 4, actual = vm.blocks.size)

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text,
                    cursor = null
                ),
                BlockView.Text.Paragraph(
                    id = secondChild.id,
                    text = secondChild.content<Block.Content.Text>().text,
                    background = secondChild.parseThemeBackgroundColor(),
                    decorations = listOf(
                        BlockView.Decoration(
                            background = secondChild.parseThemeBackgroundColor()
                        )
                    )
                )
            )
        )

        assertEquals(expected, testObserver.value())

        coroutineTestRule.advanceTime(300L)
    }

    @Test
    fun `should start deleting the target block on empty-block-backspace-click event`() {

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )


        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        givenViewModel()
        stubUnlinkBlocks(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(child.id, true)
        vm.onEmptyBlockBackspaceClicked(child.id)

        verifyBlocking(unlinkBlocks, times(1)) {
            invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    fun stubUnlinkBlocks(root: String) {
        unlinkBlocks.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    @Test
    fun `should not proceed with deleting the title block on empty-block-backspace-click event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new block on end-line-enter-press event`() = runTest {

        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        stubCreateBlock(root)
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        verify(createBlock, times(1)).async(
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    position = Position.BOTTOM,
                    prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
                )
            )
        )
    }

    @Test
    fun `should start updating the target block's color on color-toolbar-option-selected event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        stubUpdateTextColor(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val color = MockDataFactory.randomString()

        vm.onToolbarTextColorAction(color = color, targets = listOf(child))

        runBlockingTest {
            verify(updateTextColor, times(1)).invoke(
                params = eq(
                    UpdateTextColor.Params(
                        context = root,
                        targets = listOf(child),
                        color = color
                    )
                )
            )
        }
    }

    @Test
    fun `should start creating a new paragraph on endline-enter-pressed event inside a quote block`() = runTest {

        val style = Block.Content.Text.Style.QUOTE
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = style
                ),
                children = emptyList()
            )
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage(context = root)
        stubCreateBlock(root)
        stubUnlinkBlocks(root)
        stubInterceptThreadStatus()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        advanceUntilIdle()

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        advanceUntilIdle()

        verify(createBlock, times(1)).async(
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    prototype = Block.Prototype.Text(
                        style = Block.Content.Text.Style.P
                    ),
                    position = Position.BOTTOM
                )
            )
        )
    }

    @Test
    fun `should turn a list item with empty text into a paragraph on endline-enter-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.BULLET,
            secondChildText = ""
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubUpdateTextStyle()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = secondChild,
            text = "",
            marks = emptyList()
        )

        runBlockingTest {

            verify(createBlock, never()).execute(params = any())

            verify(updateTextStyle, times(1)).invoke(
                params = eq(
                    UpdateTextStyle.Params(
                        targets = listOf(secondChild),
                        style = Block.Content.Text.Style.P,
                        context = root
                    )
                )
            )
        }
    }

    @Test
    fun `should send update block text and split block commands on enter key 0 cursor position`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraph = StubParagraph()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, paragraph.id)
            ),
            header,
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = IntRange(0, 0)
        )

        val newText = MockDataFactory.randomString()

        val blockView = BlockView.Text.Paragraph(
            id = paragraph.id,
            text = newText
        )

        vm.onTextBlockTextChanged(blockView)

        vm.onEnterKeyClicked(
            target = paragraph.id,
            text = newText,
            marks = emptyList(),
            range = 0..0
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(2)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = newText,
                        target = paragraph.id,
                        marks = emptyList()
                    )
                )
            )
        }

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = paragraph.copy(
                            content = paragraph.content.asText().copy(text = newText)
                        ),
                        range = 0..0,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should send update block text and split block commands on enter key non 0 cursor position`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraph = StubParagraph()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, paragraph.id)
            ),
            header,
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = IntRange(1, 1)
        )

        val newText = MockDataFactory.randomString()

        val blockView = BlockView.Text.Paragraph(
            id = paragraph.id,
            text = newText
        )

        vm.onTextBlockTextChanged(blockView)

        vm.onEnterKeyClicked(
            target = paragraph.id,
            text = newText,
            marks = emptyList(),
            range = 0..0
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(2)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = newText,
                        target = paragraph.id,
                        marks = emptyList()
                    )
                )
            )
        }

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = paragraph.copy(
                            content = paragraph.content.asText().copy(text = newText)
                        ),
                        range = 0..0,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should start sharing a file`() {

        val root = MockDataFactory.randomUuid()
        val file = MockBlockFactory.makeFileBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(title.id, file.id)
            ),
            title,
            file
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel(builder)

        givenSharedFile()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.startSharingFile(id = file.id)

        runTest {
            verify(documentFileShareDownloader, times(1)).async(
                params = eq(
                    MiddlewareShareDownloader.Params(
                        name = file.content<Block.Content.File>().name.orEmpty(),
                        objectId = file.content<Block.Content.File>().targetObjectId.orEmpty(),
                    )
                )
            )
        }
    }

    @Test
    fun `should start downloading file`() {

        val root = MockDataFactory.randomUuid()
        val file = MockBlockFactory.makeFileBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(title.id, file.id)
            ),
            title,
            file
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel(builder)

        stubDownloadFile()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.startDownloadingFileFromBlock(blockId = file.id)

        runBlockingTest {
            verify(downloadFile, times(1)).invoke(
                params = eq(
                    DownloadFile.Params(
                        name = file.content<Block.Content.File>().name.orEmpty(),
                        url = builder.file(
                            path = file.content<Block.Content.File>().targetObjectId!!
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should proceed with undo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.paragraph()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart,
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage(context = root)
        givenViewModel()

        undo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Undo.Result.Success(
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionUndoClicked()

        runBlockingTest {
            verify(undo, times(1)).invoke(
                params = eq(
                    Undo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should proceed with redo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.paragraph()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart,
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        redo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Redo.Result.Success(
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionRedoClicked()

        runBlockingTest {
            verify(redo, times(1)).invoke(
                params = eq(
                    Redo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should start closing page after successful archive operation`() = runTest {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart,
                children = listOf(title.id)
            ),
            title
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubClosePage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(300)

        // TESTING

        vm.proceedWithExitingBack()

        verify(closePage, times(1)).async(
            params = eq(CloseBlock.Params(root, SpaceId(defaultSpace)))
        )
    }

    @Test
    fun `should convert paragraph to numbered list without any delay when regex matches`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.paragraph()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubReplaceBlock(root = root)
        givenViewModel()
        stubReplaceBlock(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        runBlockingTest {
            verify(replaceBlock, times(1)).invoke(
                params = eq(
                    ReplaceBlock.Params(
                        context = root,
                        target = paragraph.id,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.NUMBERED
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should not ignore create-numbered-list-item pattern and replace block immediately`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val numbered = StubNumbered()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(title.id, numbered.id)
            ),
            title,
            numbered
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubReplaceBlock(root)
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        val blockView = BlockView.Text.Paragraph(
            id = numbered.id,
            text = update
        )

        vm.onTextBlockTextChanged(blockView)

        runBlockingTest {
            verifyNoInteractions(updateText)

            verify(replaceBlock, times(1)).invoke(
                params = ReplaceBlock.Params(
                    context = root,
                    target = numbered.id,
                    prototype = Block.Prototype.Text(
                        style = Block.Content.Text.Style.NUMBERED
                    )
                )
            )

            coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

            verifyNoInteractions(updateText)
            verifyNoMoreInteractions(replaceBlock)
        }
    }

    @Test
    fun `should not update text while processing paragraph-to-numbered-list editor pattern`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.paragraph()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()
        stubUpdateText()
        stubReplaceBlock(root)

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateText, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should update focus after block duplication`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.paragraph()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val newBlockId = MockDataFactory.randomUuid()

        stubDuplicateBlock(newBlockId, root)

        assertTrue { orchestrator.stores.focus.current().isEmpty }

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        assertEquals(
            paragraph.id,
            orchestrator.stores.focus.current().requireTarget()
        )

        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = false)
        vm.onMultiSelectAction(ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        context = root,
                        target = paragraph.id,
                        blocks = listOf(paragraph.id)
                    )
                )
            )
        }

        verifyNoMoreInteractions(duplicateBlock)

        assertEquals(
            newBlockId,
            orchestrator.stores.focus.current().requireTarget()
        )

        coroutineTestRule.advanceTime(200)
    }

    fun stubDuplicateBlock(newBlockId: String, root: String) {
        duplicateBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    listOf(newBlockId),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    @Test
    fun `should enter multi-select mode and select blocks, and exit into edit mode when all blocks are unselected`() {

        // SETUP

        val paragraphs = listOf(
            StubParagraph(),
            StubParagraph(),
            StubParagraph()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[0].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        // At this moment, we expect that all blocks are unselected, therefore we should exit to read mode.

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    @Test
    fun `should exit multi-select mode and unselect blocks`() {

        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(StubParagraph())

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.map { view ->
                    view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    @Test
    fun `open select picture - when error in edit mode`() {

        val picture = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                targetObjectId = MockDataFactory.randomString(),
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.ERROR
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to false)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, picture.id)
            ),
            header,
            title,
            picture
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        givenOpenDocument(
            document = page
        )

        givenViewModel()


        vm.onStart(id = root, space = defaultSpace)


        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Error.Picture(
                id = picture.id,
                mode = BlockView.Mode.EDIT,
                indent = 0,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT,
                        style = BlockView.Decoration.Style.Card
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(ListenerType.Picture.Error(picture.id))

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenGallery(
                mimeType = Mimetype.MIME_IMAGE_ALL
            )
        }
    }

    @Test
    fun `open select video - when error in edit mode`() {

        val video = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                targetObjectId = MockDataFactory.randomString(),
                type = Block.Content.File.Type.VIDEO,
                state = Block.Content.File.State.ERROR
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to false)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, video.id)
            ),
            header,
            title,
            video
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        givenOpenDocument(
            document = page
        )

        givenViewModel()


        vm.onStart(id = root, space = defaultSpace)


        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Error.Video(
                id = video.id,
                mode = BlockView.Mode.EDIT,
                indent = 0,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT,
                        style = BlockView.Decoration.Style.Card
                    )
                )
            )
        )


        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(ListenerType.Video.Error(video.id))

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenGallery(
                mimeType = Mimetype.MIME_VIDEO_ALL
            )
        }
    }

    @Test
    fun `open select file - when error in edit mode`() {

        val file = StubFile(
            state = Block.Content.File.State.ERROR,
            type = Block.Content.File.Type.FILE,
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to false)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, file.id)
            ),
            header,
            title,
            file
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        givenOpenDocument(
            document = page
        )

        givenViewModel()


        vm.onStart(id = root, space = defaultSpace)


        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Error.File(
                id = file.id,
                mode = BlockView.Mode.EDIT,
                indent = 0,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT,
                        style = BlockView.Decoration.Style.Card
                    )
                ),
                name = file.content<Block.Content.File>().name.orEmpty()
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(ListenerType.File.Error(file.id))

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenGallery(
                mimeType = Mimetype.MIME_FILE_ALL
            )
        }
    }

    private fun givenOpenDocument(
        document: List<Block> = emptyList(),
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList(),
        objectRestrictions: List<ObjectRestriction> = emptyList()
    ) {
        openPage.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
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

    private fun stubClosePage(
        exception: Exception? = null,
        context: Id? = root,
    ) {

        closePage.stub {
            onBlocking { if (context == null) async(any()) else async(CloseBlock.Params(root, SpaceId(defaultSpace))) } doReturn Resultat.success(
                Unit
            )
        }

        exception?.let {
            closePage.stub {
                onBlocking { async(any()) } doAnswer { invocationOnMock -> throw exception }
            }
        }
    }

    private fun stubSplitBlocks(root: String) {
        splitBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    fun stubFileLimitEvents() {
        interceptFileLimitEvents.stub {
            onBlocking { run(Unit) } doReturn emptyFlow()
        }
    }

    fun stubOpenPage(
        context: Id = root,
        events: List<Event> = emptyList()
    ) {
        openPage.stub {
            onBlocking {
                async(
                    OpenPage.Params(
                        obj = context,
                        space = spaceId,
                        saveAsLastOpened = true
                    )
                )
            } doReturn Resultat.success(
                Result.Success(
                    Payload(
                        context = context,
                        events = events
                    )
                )
            )
        }
    }

    private fun stubUpdateBlocksMark() {
        updateBlocksMark.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(Payload("", emptyList()))
        }
    }

    private fun stubUpdateText() {
        updateText.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(Unit)
        }
    }

    fun stubObserveEvents(
        flow: Flow<List<Event>> = flowOf(),
        stubInterceptThreadStatus: Boolean = true
    ) {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow
        }
        if (stubInterceptThreadStatus) stubInterceptThreadStatus()
    }

    fun stubInterceptThreadStatus() {
        spaceSyncAndP2PStatusProvider.stub {
            onBlocking { observe() } doReturn emptyFlow()
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

    private fun stubReplaceBlock(root: String) {
        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    private fun stubCreateBlock(root: String) {
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

    private fun stubUpdateTitle() {
        updateTitle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubDownloadFile() {
        downloadFile.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun givenSharedFile() {
        documentFileShareDownloader.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                MiddlewareShareDownloader.Response(
                    Uri.EMPTY,
                    ""
                )
            )
        }
    }

    private fun stubUpdateTextColor(root: String) {
        updateTextColor.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    private fun stubUpdateTextStyle(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }

    private fun stubTurnIntoStyle(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        turnIntoStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }

    lateinit var getObjectTypes: GetObjectTypes

    lateinit var addRelationToObject: AddRelationToObject

    @Mock
    lateinit var dateProvider: DateProvider

    fun givenViewModel(urlBuilder: UrlBuilder = builder) {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )
        addRelationToObject = AddRelationToObject(repo)
        objectToSet = ConvertObjectToSet(repo, dispatchers)
        updateDetail = UpdateDetail(repo)
        setDocCoverImage = SetDocCoverImage(repo)
        setDocImageIcon = SetDocumentImageIcon(repo)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)
        clearBlockContent = ClearBlockContent(repo)
        clearBlockStyle = ClearBlockStyle(repo)
        interceptFileLimitEvents = interceptFileLimitEvents
        setObjectInternalFlags = SetObjectInternalFlags(repo, dispatchers)

        getObjectTypes = GetObjectTypes(repo, dispatchers)

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
            spaceManager = spaceManager,
        )

        vm = EditorViewModel(
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
                storeOfObjectTypes = storeOfObjectTypes,
                fieldParser = fieldParser
            ),
            orchestrator = orchestrator,
            analytics = analytics,
            dispatcher = Dispatcher.Default(),
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
                space = spaceId
            ),
            permissions = permissions,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            clearLastOpenedObject = clearLastOpenedObject,
            getNetworkMode = getNetworkMode,
            fieldParser = fieldParser,
            dateProvider = dateProvider,
            getDateObjectByTimestamp = getDateObjectByTimestamp
        )
    }

    @Test
    fun `should enter multi select mode and selections should be empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            StubParagraph(),
            StubParagraph(),
            StubParagraph()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be two selected blocks in multi select mode`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            StubParagraph(),
            StubParagraph(),
            StubParagraph()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be zero selected blocks after done click`() {
        // SETUP

        val paragraphs = listOf(StubParagraph(), StubParagraph(), StubParagraph())

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text,
                    decorations = listOf(
                        BlockView.Decoration(
                            background = p.parseThemeBackgroundColor()
                        )
                    )
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should not update text style in multi select mode`() {

        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(
                        Block.Content.Text.Mark(
                            range = 0..7, type = Block.Content.Text.Mark.Type.BOLD
                        )
                    ),
                    text = "Foo Bar",
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignCenter
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = id2,
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        givenViewModel()

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.styleTextToolbar.isVisible)

        vm.onClickListener(ListenerType.LongClick(target = blocks[0].id))
        vm.onMultiSelectStyleButtonClicked()

        val actual = vm.controlPanelViewState.test().value()
        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(Block.Content.Text.Style.P)
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isSelectAllVisible = true,
                isVisible = true,
                count = 1
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )

        assertEquals(expected, actual)

        vm.onStylingToolbarEvent(event = StylingEvent.Markup.Italic)

        verifyNoMoreInteractions(updateText)

        coroutineTestRule.advanceTime(200)
    }

    fun stubUserPermission(
        space: SpaceId = spaceId,
        permission: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) {
        permissions.stub {
            on {
                observe(space = space)
            } doReturn flowOf(permission)
        }
    }

    fun stubAnalyticSpaceHelperDelegate() {
        analyticSpaceHelperDelegate.stub {
            on { provideParams(defaultSpace) } doReturn AnalyticSpaceHelperDelegate.Params.EMPTY
        }
    }
}