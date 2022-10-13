package com.anytypeio.anytype.presentation.sets

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals

class ObjectSetRecordViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    private val ctx: Id = MockDataFactory.randomUuid()
    private val obj: Id = MockDataFactory.randomUuid()
    private val record = mapOf(
        Relations.ID to obj
    )

    //region Defining dummy object set

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private val viewer = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.values().random(),
        name = MockDataFactory.randomString(),
        viewerRelations = emptyList()
    )

    private val dv = Block(
        id = MockDataFactory.randomUuid(),
        content = DV(
            sources = listOf(MockDataFactory.randomString()),
            relations = emptyList(),
            viewers = listOf(viewer)
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    //endregion

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not crash if cache does not contain relevant data`() {

        // SETUP

        val emptyCache = ObjectSetRecordCache()

        val vm = buildViewModel(cache = emptyCache)

        // TESTING

        vm.onComplete(
            ctx = ctx,
            input = MockDataFactory.randomString()
        )

        verifyNoInteractions(setObjectDetails)
    }

    @Test
    fun `should proceed with updating record name based on user input on action done`() {

        // SETUP

        val doc = listOf(
            header,
            title,
            dv
        )

        val state = MutableStateFlow(ObjectSet(blocks = doc))

        val cache = ObjectSetRecordCache().apply {
            map[ctx] = record
        }

        val vm = buildViewModel(
            cache = cache
        )

        val input = MockDataFactory.randomString()

        val params = UpdateDetail.Params(
            ctx = obj,
            key = Relations.NAME,
            value = input
        )

        stubSetObjectDetails(params)

        // TESTING

        vm.onComplete(
            ctx = ctx,
            input = input
        )

        verifyBlocking(setObjectDetails, times(1)) {
            invoke(params)
        }
    }

    @Test
    fun `should update record name before opening this object`() = runTest {

        // SETUP

        val doc = listOf(
            header,
            title,
            dv
        )

        val state = MutableStateFlow(ObjectSet(blocks = doc))

        val cache = ObjectSetRecordCache().apply {
            map[ctx] = record
        }

        val input = MockDataFactory.randomString()

        val params = UpdateDetail.Params(
            ctx = obj,
            key = Relations.NAME,
            value = input
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel(
            cache = cache
        )

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                ctx = ctx,
                input = input
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj
                ),
                actual = awaitItem()
            )
        }

        verifyBlocking(setObjectDetails, times(1)) {
            invoke(params)
        }
    }

    @Test
    fun `should not update record name before opening this object if user input is empty`() = runTest {

        // SETUP

        val doc = listOf(
            header,
            title,
            dv
        )

        val state = MutableStateFlow(ObjectSet(blocks = doc))

        val cache = ObjectSetRecordCache().apply {
            map[ctx] = record
        }

        val emptyInput = ""

        val params = UpdateDetail.Params(
            ctx = obj,
            key = Relations.NAME,
            value = emptyInput
        )

        stubSetObjectDetails(params)

        val vm = buildViewModel(cache = cache)

        // TESTING

        vm.commands.test {
            vm.onButtonClicked(
                ctx = ctx,
                input = emptyInput
            )
            assertEquals(
                expected = ObjectSetRecordViewModel.Command.OpenObject(
                    ctx = obj
                ),
                actual = awaitItem()
            )
        }

        verifyNoInteractions(setObjectDetails)
    }

    fun buildViewModel(
        cache: ObjectSetRecordCache
    ) : ObjectSetRecordViewModel = ObjectSetRecordViewModel(
        setObjectDetails = setObjectDetails,
        objectSetRecordCache = cache
    )

    private fun stubSetObjectDetails(
        params: UpdateDetail.Params,
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        setObjectDetails.stub {
            onBlocking {
                invoke(params)
            } doReturn Either.Right(payload)
        }
    }
}