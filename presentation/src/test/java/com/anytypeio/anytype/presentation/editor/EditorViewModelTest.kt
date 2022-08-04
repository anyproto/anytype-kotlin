package com.anytypeio.anytype.presentation.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.Move
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
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.relations.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateDocument
import com.anytypeio.anytype.domain.page.CreateNewDocument
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.UpdateTitle
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmarkBlock
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.table.CreateTable
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.Interactor
import com.anytypeio.anytype.presentation.editor.editor.InternalDetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.parseThemeTextColor
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.editor.editor.table.DefaultSimpleTableDelegate
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableDelegate
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.template.DefaultEditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.ValueClassAnswer
import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
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
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
    lateinit var createDocument: CreateDocument

    @Mock
    lateinit var createNewDocument: CreateNewDocument

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
    lateinit var updateFields: UpdateFields

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var getCompatibleObjectTypes: GetCompatibleObjectTypes

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var setObjectType: SetObjectType

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    @Mock
    lateinit var findObjectSetForType: FindObjectSetForType

    @Mock
    lateinit var createObjectSet: CreateObjectSet

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    @Mock
    lateinit var getTemplates: GetTemplates

    @Mock
    lateinit var applyTemplate: ApplyTemplate

    @Mock
    lateinit var fillTableRow: FillTableRow

    private lateinit var editorTemplateDelegate: EditorTemplateDelegate

    private lateinit var simpleTableDelegate: SimpleTableDelegate

    @Mock
    lateinit var createNewObject: CreateNewObject

    @Mock
    lateinit var createTable: CreateTable

    private lateinit var updateDetail: UpdateDetail

    lateinit var vm: EditorViewModel

    private lateinit var builder: UrlBuilder
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var setDocImageIcon: SetDocumentImageIcon

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        builder = UrlBuilder(gateway)
        editorTemplateDelegate = DefaultEditorTemplateDelegate(
            getTemplates = getTemplates,
            applyTemplate = applyTemplate
        )
        simpleTableDelegate = DefaultSimpleTableDelegate()
    }

    @Test
    fun `should not start observing events when view model is initialized`() {
        givenViewModel()
        verifyZeroInteractions(interceptEvents)
    }

    @Test
    fun `should start opening page when requested`() {
        val param = OpenPage.Params(id = root)

        stubInterceptEvents()
        givenViewModel()
        stubOpenPage(context = root)

        vm.onStart(root)

        runBlockingTest { verify(openPage, times(1)).invoke(param) }
    }

    @Test
    fun `should dispatch a page to UI when this view model receives an appropriate command`() {

        val child = MockDataFactory.randomUuid()

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

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    background = paragraph.parseThemeBackgroundColor()
                )
            )
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should close page when the system back button is pressed`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage(root)
        stubInterceptEvents()

        givenViewModel()

        vm.onStart(root)

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        runBlockingTest {
            verify(closePage, times(1)).invoke(any())
        }
    }

    @Test
    fun `should emit an approprtiate navigation command when the page is closed`() {

        val response = Either.Right(Unit)

        stubInterceptEvents()
        stubClosePage(response)
        givenViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should not emit any navigation command if there is an error while closing the page`() {

        val root = MockDataFactory.randomUuid()

        val error = Exception("Error while closing this page")

        val response = Either.Left(error)

        stubOpenPage(root)
        stubClosePage(response)
        stubInterceptEvents()
        givenViewModel()

        vm.onStart(root)

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver.assertNoValue()
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

        vm.onStart(pageId)
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should debounce values when dispatching text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        stubUpdateText()
        stubOpenPage(context = pageId)
        givenViewModel()

        vm.onStart(pageId)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(2)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should add a new block to the already existing one when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

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

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val added = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

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
        vm.onStart(root)

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
                        background = paragraph.parseThemeBackgroundColor()
                    ),
                    BlockView.Text.Paragraph(
                        id = added.id,
                        text = added.content.asText().text,
                        background = added.parseThemeBackgroundColor()
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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = true)

        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(any())
        }
    }

    @Test
    fun `should update block text without dispatching it to UI when we receive an appropriate event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

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

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    background = paragraph.parseThemeBackgroundColor()
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

        vm.onStart(root)

        testObserver.assertValue(ViewState.Loading)
    }

    @Test
    fun `should apply two different markup actions`() {

        val root = MockDataFactory.randomUuid()
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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

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
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        val secondTimeRange = 0..5
        val secondTimeMarkup = StylingEvent.Markup.Italic

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
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should apply two markup actions of the same markup type`() {

        val root = MockDataFactory.randomUuid()
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
            content = Block.Content.Smart(),
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
        givenViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

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
        val secondTimeMarkup = StylingEvent.Markup.Bold

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
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should dispatch texts changes and markup even if only markup is changed`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red"
            ),
            children = emptyList(),
            backgroundColor = "yellow"
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val range = 0..3
        val markup = StylingEvent.Markup.Bold

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

        runBlockingTest {
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
    }

    @Test
    fun `test changes from UI do not trigger re-rendering`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    background = paragraph.parseThemeBackgroundColor()
                )
            )
        )

        testObserver.assertValue(state).assertHistorySize(2)

        val userInput = MockDataFactory.randomString()

        val range = 0..3

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                range = range
            )
        )

        vm.onTextChanged(
            id = paragraph.id,
            marks = marks,
            text = userInput
        )

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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = emptyList())

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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()
        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = marks)

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

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        val expected = ControlPanelState.init()

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should add a header-one block on add-header-one event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                        background = paragraph.parseThemeBackgroundColor()
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
                        background = paragraph.parseThemeBackgroundColor()
                    ),
                    BlockView.Text.Header.One(
                        id = new.id,
                        text = new.content<Block.Content.Text>().text,
                        background = new.parseThemeBackgroundColor(),
                        indent = 0
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


        vm.onStart(root)

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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

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

        val firstChild = Block(
            id = "FIRST CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "FIRST CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val secondChild = Block(
            id = "SECOND CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "SECOND CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                        background = firstChild.parseThemeBackgroundColor()
                    ),
                    BlockView.Text.Paragraph(
                        id = secondChild.id,
                        text = secondChild.content<Block.Content.Text>().text,
                        background = secondChild.parseThemeBackgroundColor()
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
                    background = secondChild.parseThemeBackgroundColor()
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
            content = Block.Content.Smart(),
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

        vm.onStart(root)

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

    private fun stubUnlinkBlocks(root: String) {
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new block on end-line-enter-press event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
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
    }

    @Test
    fun `should start updating text style of the focused block on turn-into-option-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChild = secondChild,
            secondChildStyle = Block.Content.Text.Style.P
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
        stubTurnIntoStyle()

        givenViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        val newStyle = Block.Content.Text.Style.H1

        vm.onSlashItemClicked(SlashItem.Style.Type.Title)

        runBlockingTest {
            verify(turnIntoStyle, times(1)).invoke(
                params = eq(
                    TurnIntoStyle.Params(
                        context = root,
                        targets = listOf(secondChild),
                        style = newStyle
                    )
                )
            )
        }
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

        vm.onStart(root)

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
    fun `should start creating a new paragraph on endline-enter-pressed event inside a quote block`() {

        val style = Block.Content.Text.Style.QUOTE
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
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
        stubOpenPage()
        stubCreateBlock(root)
        givenViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
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

        vm.onStart(root)

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

            verify(createBlock, never()).invoke(
                scope = any(),
                params = any(),
                onResult = any()
            )

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
    fun `should send update text style intent when is list and empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 0..0
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = text,
                        target = child,
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
                        block = page[3],
                        range = 0..0,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should preserve text style while splitting`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val style = Block.Content.Text.Style.BULLET

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
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
        stubOpenPage()
        givenViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 1..1
        )

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = page[3],
                        range = 1..1,
                        isToggled = null
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.startDownloadingFile(id = file.id)

        runBlockingTest {
            verify(downloadFile, times(1)).invoke(
                params = eq(
                    DownloadFile.Params(
                        name = file.content<Block.Content.File>().name.orEmpty(),
                        url = builder.file(
                            hash = file.content<Block.Content.File>().hash
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
    fun `should start closing page after successful archive operation`() {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
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
        stubOpenPage()
        stubClosePage()
        givenViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        stubClosePage()

        // TESTING

        vm.proceedWithExitingBack()

        runBlockingTest {
            verify(closePage, times(1)).invoke(
                params = eq(
                    CloseBlock.Params(
                        id = root
                    )
                )
            )
        }
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
    fun `should ignore create-numbered-list-item pattern and update text with delay`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val numbered = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList()
        )
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
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
        givenViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextChanged(
            id = numbered.id,
            marks = numbered.content<Block.Content.Text>().marks,
            text = update
        )

        runBlockingTest {
            verify(updateText, never()).invoke(
                params = any()
            )

            verify(replaceBlock, never()).invoke(
                params = any()
            )
        }

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(replaceBlock, never()).invoke(
                params = any()
            )

            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        target = numbered.id,
                        marks = numbered.content<Block.Content.Text>().marks,
                        text = update
                    )
                )
            )
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val newBlockId = MockDataFactory.randomUuid()

        stubDuplicateBlock(newBlockId, root)

        val focus = vm.focus.test()

        focus.assertValue { id -> id.isEmpty() }

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        focus.assertValue { id -> id == paragraph.id }

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

        focus.assertValue { id -> id == newBlockId }

        coroutineTestRule.advanceTime(200)
    }

    private fun stubDuplicateBlock(newBlockId: String, root: String) {
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
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
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

        // At this momemnt, we expect that all blocks are unselected, therefore we should exit to read mode.

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    @Test
    fun `should exit multi-select mode and unselect blocks`() {

        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    text = p.content<Block.Content.Text>().text
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
                hash = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
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


        vm.onStart(root)


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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = ThemeColor.DEFAULT,
                            style = BlockView.Decoration.Style.None
                        )
                    )
                } else {
                    emptyList()
                }
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
                hash = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
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


        vm.onStart(root)


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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = ThemeColor.DEFAULT,
                            style = BlockView.Decoration.Style.None
                        )
                    )
                } else {
                    emptyList()
                }
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

        val file = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                hash = MockDataFactory.randomString(),
                type = Block.Content.File.Type.FILE,
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
                content = Block.Content.Smart(SmartBlockType.PAGE),
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


        vm.onStart(root)


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
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = ThemeColor.DEFAULT,
                            style = BlockView.Decoration.Style.None
                        )
                    )
                } else {
                    emptyList()
                }
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

    private fun stubClosePage(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        closePage.stub {
            onBlocking { invoke(any()) } doReturn response
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

    fun stubOpenPage(
        context: Id = MockDataFactory.randomString(),
        events: List<Event> = emptyList()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = context,
                        events = events
                    )
                )
            )
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
        interceptThreadStatus.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
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

    private fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
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

    private fun stubGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn flow {
                emit(
                    GetDefaultEditorType.Response(
                        type,
                        name
                    )
                )
            }
        }
    }

    fun givenViewModel(urlBuilder: UrlBuilder = builder) {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )
        updateDetail = UpdateDetail(repo)
        setDocCoverImage = SetDocCoverImage(repo)
        setDocImageIcon = SetDocumentImageIcon(repo)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)

        vm = EditorViewModel(
            openPage = openPage,
            closePage = closePage,
            createObject = createObject,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                coverImageHashProvider = coverImageHashProvider
            ),
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            analytics = analytics,
            getDefaultEditorType = getDefaultEditorType,
            orchestrator = Orchestrator(
                createBlock = createBlock,
                replaceBlock = replaceBlock,
                updateTextColor = updateTextColor,
                duplicateBlock = duplicateBlock,
                downloadFile = downloadFile,
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
                setObjectType = setObjectType,
                createTable = createTable,
                fillTableRow = fillTableRow
            ),
            dispatcher = Dispatcher.Default(),
            detailModificationManager = InternalDetailModificationManager(storage.details),
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider,
            searchObjects = searchObjects,
            findObjectSetForType = findObjectSetForType,
            createObjectSet = createObjectSet,
            copyFileToCache = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            delegator = delegator,
            templateDelegate = editorTemplateDelegate,
            createNewObject = createNewObject,
            simpleTableDelegate = simpleTableDelegate
        )
    }

    @Test
    fun `should enter multi select mode and selections should be empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
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
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
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

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
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
                content = Block.Content.Smart(),
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

        vm.onStart(root)

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

    @Test
    fun `should close editor and navigate to page screen - when page is created`() {

        val id = MockDataFactory.randomUuid()
        stubOpenPage(root)
        stubInterceptEvents()
        stubClosePage()
        givenViewModel()
        vm.onStart(root)


        givenDelegateId(id)
        vm.onAddNewDocumentClicked()

        vm.navigation
            .test()
            .assertHasValue()
            .assertValue { value ->
                (value.peekContent() as AppNavigation.Command.OpenObject).id == id
            }
    }

    private fun givenDelegateId(id: String) {
        createNewObject.stub {
            onBlocking { execute(Unit) } doAnswer ValueClassAnswer(id)
        }
    }
}