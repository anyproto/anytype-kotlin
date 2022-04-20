package com.anytypeio.anytype.presentation.sets

import MockDataFactory
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals

class ObjectSetRecordViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var updateDataViewRecord: UpdateDataViewRecord

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

        val doc = listOf(
            header,
            title,
            dv
        )

        val state = MutableStateFlow(ObjectSet(blocks = doc))

        val emptyCache = ObjectSetRecordCache()

        val vm = buildViewModel(
            state = state,
            cache = emptyCache
        )

        // TESTING

        vm.onComplete(
            ctx = ctx,
            input = MockDataFactory.randomString()
        )

        verifyZeroInteractions(updateDataViewRecord)
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
            state = state,
            cache = cache
        )

        val input = MockDataFactory.randomString()

        val params = UpdateDataViewRecord.Params(
            context = ctx,
            record = obj,
            target = dv.id,
            values = mapOf(Relations.NAME to input)
        )

        stubUpdateDataViewRecord(params)

        // TESTING

        vm.onComplete(
            ctx = ctx,
            input = input
        )

        verifyBlocking(updateDataViewRecord, times(1)) {
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

        val params = UpdateDataViewRecord.Params(
            context = ctx,
            record = obj,
            target = dv.id,
            values = mapOf(Relations.NAME to input)
        )

        stubUpdateDataViewRecord(params)

        val vm = buildViewModel(
            state = state,
            cache = cache
        )

        // TESTING

        vm.commands.test {
            vm.onExpandButtonClicked(
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

        verifyBlocking(updateDataViewRecord, times(1)) {
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

        val params = UpdateDataViewRecord.Params(
            context = ctx,
            record = obj,
            target = dv.id,
            values = mapOf(Relations.NAME to emptyInput)
        )

        stubUpdateDataViewRecord(params)

        val vm = buildViewModel(
            state = state,
            cache = cache
        )

        // TESTING

        vm.commands.test {
            vm.onExpandButtonClicked(
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

        verifyZeroInteractions(updateDataViewRecord)
    }

    fun buildViewModel(
        state: StateFlow<ObjectSet>,
        cache: ObjectSetRecordCache
    ) : ObjectSetRecordViewModel = ObjectSetRecordViewModel(
        updateDataViewRecord = updateDataViewRecord,
        objectSetRecordCache = cache,
        objectSetState = state
    )

    private fun stubUpdateDataViewRecord(params: UpdateDataViewRecord.Params) {
        updateDataViewRecord.stub {
            onBlocking {
                invoke(params)
            } doReturn Either.Right(Unit)
        }
    }
}