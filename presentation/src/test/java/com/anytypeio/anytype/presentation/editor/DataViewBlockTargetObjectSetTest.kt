package com.anytypeio.anytype.presentation.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataViewBlock
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class DataViewBlockTargetObjectSetTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))
    private val block = StubParagraph()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should render EmptySource set dataView block when targetObjectId is empty`() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val dv = StubDataViewBlock(targetObjectId = "")
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )

        stubOpenDocument(document = listOf(page, header, title, block, dv))
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.EmptySource(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = false
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render EmptyData set dataView block when SetOf with empty strings`() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val targetObjectId = MockDataFactory.randomUuid()
        val dv = StubDataViewBlock(targetObjectId = targetObjectId)
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )
        val objectDetails = Block.Fields(
            mapOf(
                Relations.TYPE to ObjectTypeIds.SET,
                Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                Relations.SET_OF to listOf("")
            )
        )

        val detailsList = Block.Details(details = mapOf(targetObjectId to objectDetails))

        stubOpenDocument(document = listOf(page, header, title, block, dv), details = detailsList)
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.EmptyData(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = false
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render EmptyData set dataView block when SetOf is empty`() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val targetObjectId = MockDataFactory.randomUuid()
        val dv = StubDataViewBlock(targetObjectId = targetObjectId)
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )
        val objectDetails = Block.Fields(
            mapOf(
                Relations.TYPE to ObjectTypeIds.SET,
                Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                Relations.SET_OF to emptyList<String>()
            )
        )

        val detailsList = Block.Details(details = mapOf(targetObjectId to objectDetails))

        stubOpenDocument(document = listOf(page, header, title, block, dv), details = detailsList)
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.EmptyData(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = false
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render Default set dataView block when SetOf with non empty id `() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val targetObjectId = MockDataFactory.randomUuid()
        val dv = StubDataViewBlock(targetObjectId = targetObjectId)
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )
        val objectDetails = Block.Fields(
            mapOf(
                Relations.TYPE to ObjectTypeIds.SET,
                Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                Relations.SET_OF to listOf<String>("", "   ", RandomString.make())
            )
        )

        val detailsList = Block.Details(details = mapOf(targetObjectId to objectDetails))

        stubOpenDocument(document = listOf(page, header, title, block, dv), details = detailsList)
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.Default(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = false
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render Default collection dataView block when targetObjectId is not empty `() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val targetObjectId = MockDataFactory.randomUuid()
        val dv = StubDataViewBlock(targetObjectId = targetObjectId, isCollection = true)
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )
        val objectDetails = Block.Fields(
            mapOf(
                Relations.TYPE to ObjectTypeIds.COLLECTION,
                Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
            )
        )

        val detailsList = Block.Details(details = mapOf(targetObjectId to objectDetails))

        stubOpenDocument(document = listOf(page, header, title, block, dv), details = detailsList)
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.Default(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = true
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @Test
    fun `should render EmptySource collection dataView block when targetObjectId is empty `() = runBlocking {

        // SETUP
        stubInterceptThreadStatus()
        val params = InterceptEvents.Params(context = root)
        val targetObjectId = ""
        val dv = StubDataViewBlock(targetObjectId = targetObjectId, isCollection = true)
        val page = Block(
            id = root,
            fields = Block.Fields.empty(),
            children = listOf(header.id, block.id, dv.id),
            content = Block.Content.Smart()
        )

        stubOpenDocument(document = listOf(page, header, title, block, dv))
        stubInterceptEvents(params = params)

        val vm = buildViewModel()

        //EXPECTING
        val state = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false
                ),
                BlockView.Text.Paragraph(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text,
                    decorations = listOf(BlockView.Decoration())
                ),
                BlockView.DataView.EmptySource(
                    id = dv.id,
                    title = null,
                    background = ThemeColor.DEFAULT,
                    isSelected = false,
                    icon = ObjectIcon.None,
                    decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                    isCollection = true
                )
            )
        )

        // TESTING
        vm.onStart(root)

        assertEquals(
            expected = state,
            actual = vm.state.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when getting event for set target object id for data view block, should render as empty data inline set block`() =
        runBlocking {

            // SETUP
            stubInterceptThreadStatus()
            val params = InterceptEvents.Params(context = root)
            val emptyViewer = StubDataViewView(
                name = "",
                viewerRelations = emptyList(),
                sorts = emptyList()
            )
            val dv = StubDataViewBlock(viewers = listOf(emptyViewer))
            val targetObjectId = MockDataFactory.randomUuid()
            val page = Block(
                id = root,
                fields = Block.Fields.empty(),
                children = listOf(header.id, block.id, dv.id),
                content = Block.Content.Smart()
            )

            val events = flow<List<Event>> {
                delay(DELAY)
                emit(
                    listOf(
                        Event.Command.DataView.SetTargetObjectId(
                            context = root,
                            dv = dv.id,
                            targetObjectId = targetObjectId
                        )
                    )
                )
            }

            stubOpenDocument(document = listOf(page, header, title, block, dv))
            stubInterceptEvents(
                params = params,
                flow = events
            )

            val vm = buildViewModel()

            //EXPECTING
            val state = ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<Block.Content.Text>().text,
                        isFocused = false
                    ),
                    BlockView.Text.Paragraph(
                        id = block.id,
                        text = block.content<Block.Content.Text>().text,
                        decorations = listOf(BlockView.Decoration())
                    ),
                    BlockView.DataView.EmptyData(
                        id = dv.id,
                        title = null,
                        background = ThemeColor.DEFAULT,
                        isSelected = false,
                        icon = ObjectIcon.None,
                        decorations = listOf(BlockView.Decoration(style = BlockView.Decoration.Style.Card)),
                        isCollection = false
                    )
                )
            )

            // TESTING
            vm.onStart(root)

            coroutineTestRule.advanceTime(DELAY)

            assertEquals(
                expected = state,
                actual = vm.state.value
            )
        }

    companion object {
        private const val DELAY = 100L
    }
}