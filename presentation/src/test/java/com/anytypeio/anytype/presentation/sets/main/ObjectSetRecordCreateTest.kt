package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Relation
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
        initDataViewSubscriptionContainer()
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
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
        stubCreateDataViewRecord()
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
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
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
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
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
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
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

    @Test
    fun `should create new pre-populated record - when filter has all_in condition`() {
        `create pre-populated record`(DVFilterCondition.ALL_IN)
    }

    @Test
    fun `should create new pre-populated record - when filter has in condition`() {
        `create pre-populated record`(DVFilterCondition.IN)
    }

    @Test
    fun `should create new pre-populated record - when filter has equal condition`() {
        `create pre-populated record`(DVFilterCondition.EQUAL)
    }

    private fun `create pre-populated record`(condition: DVFilterCondition) {

        // SETUP

        val relationStakeholderKey = MockDataFactory.randomUuid()
        val relationStakeholderValue = MockDataFactory.randomString()

        val relationStakeHolders = Relation(
            key = relationStakeholderKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.OBJECT,
            selections = emptyList()
        )

        val givenTemplate = MockDataFactory.randomUuid()

        val source = MockDataFactory.randomString()

        val filter = DVFilter(
            relationKey = relationStakeholderKey,
            operator = DVFilterOperator.AND,
            condition = condition,
            value = relationStakeholderValue,
        )

        val viewerRelationName = DVViewerRelation(
            key = MockDataFactory.randomString(),
            isVisible = true
        )

        val viewerRelationStakeHolder = DVViewerRelation(
            key = relationStakeholderKey,
            isVisible = true
        )

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = listOf(filter),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = listOf(viewerRelationName, viewerRelationStakeHolder)
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(source),
                relations = listOf(relationStakeHolders),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubCreateDataViewRecord()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
        stubGetTemplates(
            type = source,
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
                            Relations.SET_OF to listOf(source)
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
                    template = givenTemplate,
                    prefilled = mapOf(relationStakeholderKey to relationStakeholderValue)
                )
            )
        }
    }
}