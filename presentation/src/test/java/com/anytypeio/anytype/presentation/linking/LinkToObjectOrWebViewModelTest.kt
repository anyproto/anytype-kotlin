package com.anytypeio.anytype.presentation.linking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub


open class LinkToObjectOrWebViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var urlValidator: UrlValidator

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var gateway: Gateway

    var store: Editor.Storage = Editor.Storage()
    var ctx = ""
    val spaceId = MockDataFactory.randomString()

    protected val builder: UrlBuilder get() = UrlBuilder(gateway)
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        release()
    }

    @Test
    fun `should return selected block error state when block is not present in state`() = runTest {

        spaceManager.set(spaceId)

        val target = MockDataFactory.randomString()

        val block = StubParagraph()

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()
        stubSearchObjects(params = vm.getSearchObjectsParams(ctx))

        vm.onStart(
            blockId = target,
            rangeStart = 0,
            rangeEnd = 10,
            clipboardUrl = null,
            ignore = ctx
        )

        assertEquals(
            expected = LinkToObjectOrWebViewModel.ViewState.ErrorSelectedBlock,
            actual = vm.viewState.value
        )
    }

    @Test
    fun `should return selected range error state when selection range end is bigger then text length`() = runTest {

        spaceManager.set(spaceId)
        val target = MockDataFactory.randomString()

        val start = 0
        val end = 7

        val block = StubParagraph(id = target, text = "FooBar")

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()

        stubSearchObjects(params = vm.getSearchObjectsParams(ctx))

        vm.onStart(
            blockId = target,
            rangeStart = start,
            rangeEnd = end,
            clipboardUrl = null,
            ignore = ctx
        )

        val result = vm.viewState.value

        assertEquals(
            expected = LinkToObjectOrWebViewModel.ViewState.ErrorSelection,
            actual = result
        )
    }

    @Test
    fun `should return selected range error state when selection range start is equal end`() = runTest {

        spaceManager.set(spaceId)
        val target = MockDataFactory.randomString()

        val start = 3
        val end = 3

        val block = StubParagraph(id = target, text = "FooBar")

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()

        vm.onStart(
            blockId = target,
            rangeStart = start,
            rangeEnd = end,
            clipboardUrl = null,
            ignore = ctx
        )

        val result = vm.viewState.value

        assertEquals(
            expected = LinkToObjectOrWebViewModel.ViewState.ErrorSelection,
            actual = result
        )
    }

    @Test
    fun `should set markup link param when present in text link type markup`() = runTest {

        spaceManager.set(spaceId)
        val target = MockDataFactory.randomUuid()
        val url = MockDataFactory.randomString()

        val block = StubParagraph(
            id = target, text = "FooBar", marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 6),
                    type = Block.Content.Text.Mark.Type.LINK,
                    param = url
                )
            )
        )

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()

        stubSearchObjects(params = vm.getSearchObjectsParams(ctx))

        vm.onStart(
            blockId = target,
            rangeStart = 0,
            rangeEnd = 6,
            clipboardUrl = null,
            ignore = ctx
        )

        val result = vm.markupLinkParam.value

        assertEquals(expected = url, actual = result)
        assertNull(vm.markupObjectParam.value)
    }

    @Test
    fun `should set markup object param when present in text object type markup`() = runTest {

        spaceManager.set(spaceId)
        val target = MockDataFactory.randomUuid()
        val url = MockDataFactory.randomString()

        val block = StubParagraph(
            id = target, text = "FooBar", marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(2, 5),
                    type = Block.Content.Text.Mark.Type.OBJECT,
                    param = url
                )
            )
        )

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()

        stubSearchObjects(params = vm.getSearchObjectsParams(ctx))

        vm.onStart(
            blockId = target,
            rangeStart = 0,
            rangeEnd = 6,
            clipboardUrl = null,
            ignore = ctx
        )

        val result = vm.markupObjectParam.value
        assertEquals(expected = url, actual = result)
        assertNull(vm.markupLinkParam.value)
    }

    @Test
    fun `should not set object link when not present in range text markup`() = runTest {

        spaceManager.set(spaceId)
        val target = MockDataFactory.randomUuid()
        val url = MockDataFactory.randomString()

        val block = StubParagraph(
            id = target, text = "FooBarFooBar", marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 3),
                    type = Block.Content.Text.Mark.Type.OBJECT,
                    param = url
                ),
                Block.Content.Text.Mark(
                    range = IntRange(7, 12),
                    type = Block.Content.Text.Mark.Type.LINK,
                    param = url
                )
            )
        )

        runBlocking { store.document.update(listOf(block)) }

        val vm = givenViewModel()

        stubSearchObjects(params = vm.getSearchObjectsParams(ctx))

        vm.onStart(
            blockId = target,
            rangeStart = 4,
            rangeEnd = 6,
            clipboardUrl = null,
            ignore = ctx
        )

        assertNull(vm.markupObjectParam.value)
        assertNull(vm.markupLinkParam.value)
    }

    private fun release() {
        coroutineTestRule.advanceTime(LinkToObjectOrWebViewModel.SEARCH_INPUT_DEBOUNCE)
    }

    private fun stubSearchObjects(
        params: SearchObjects.Params,
        objects: List<ObjectWrapper.Basic> = emptyList()
    ) {
        searchObjects.stub {
            onBlocking { invoke(params) } doReturn Either.Right(objects)
        }
    }

    private fun givenViewModel() = LinkToObjectOrWebViewModel(
        searchObjects = searchObjects,
        analytics = analytics,
        stores = store,
        urlBuilder = builder,
        urlValidator = urlValidator,
        storeOfObjectTypes = storeOfObjectTypes,
        spaceManager = spaceManager
    )
}