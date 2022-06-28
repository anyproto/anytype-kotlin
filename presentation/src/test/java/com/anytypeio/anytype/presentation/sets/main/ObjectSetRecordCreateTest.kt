package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class ObjectSetRecordCreateTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should create new record without template - when no source type is not defined`() {

        // SETUP

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubSetActiveViewer()
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            )
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCreateNewRecord()

        verifyBlocking(createDataViewRecord, times(1)) {
            invoke(
                CreateDataViewRecord.Params(
                    context = root,
                    target = dv.id,
                    template = null
                )
            )
        }
    }

    @Test
    fun `should create new record without template - when source has multiple types`() {

        // SETUP

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubSetActiveViewer()
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        map = mapOf(
                            Relations.SET_OF to listOf(
                                MockDataFactory.randomUuid(),
                                MockDataFactory.randomUuid()
                            )
                        )
                    )
                )
            )
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCreateNewRecord()

        verifyBlocking(createDataViewRecord, times(1)) {
            invoke(
                CreateDataViewRecord.Params(
                    context = root,
                    target = dv.id,
                    template = null
                )
            )
        }
    }

    @Test
    fun `should create new record without template - when source type has multiple templates`() {

        // SETUP

        val givenType = MockDataFactory.randomUuid()

        val givenTemplates = listOf(
            MockDataFactory.randomUuid(),
            MockDataFactory.randomUuid()
        )

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubSetActiveViewer()
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubGetTemplates(
            type = givenType,
            templates = givenTemplates
        )
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        map = mapOf(
                            Relations.SET_OF to listOf(givenType)
                        )
                    )
                )
            )
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCreateNewRecord()

        verifyBlocking(createDataViewRecord, times(1)) {
            invoke(
                CreateDataViewRecord.Params(
                    context = root,
                    target = dv.id,
                    template = null
                )
            )
        }
    }

    @Test
    fun `should create new record with template - when source type has only one template`() {

        // SETUP

        val givenType = MockDataFactory.randomUuid()
        val givenTemplate = MockDataFactory.randomUuid()

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubSetActiveViewer()
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubGetTemplates(
            type = givenType,
            templates = listOf(givenTemplate)
        )
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        map = mapOf(
                            Relations.SET_OF to listOf(givenType)
                        )
                    )
                )
            )
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCreateNewRecord()

        verifyBlocking(createDataViewRecord, times(1)) {
            invoke(
                CreateDataViewRecord.Params(
                    context = root,
                    target = dv.id,
                    template = givenTemplate
                )
            )
        }
    }
}