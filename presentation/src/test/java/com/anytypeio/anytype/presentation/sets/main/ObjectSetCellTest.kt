package com.anytypeio.anytype.presentation.sets.main

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewKeys
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetCellTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))

    private val relations = listOf(
        StubRelationObject(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            isReadOnlyValue = false
        ),
        StubRelationObject(
            key = MockDataFactory.randomUuid(),
            format = Relation.Format.LONG_TEXT,
            isReadOnlyValue = true
        )
    )

    private val vrelations = relations.map { relation ->
        DVViewerRelation(
            key = relation.key,
            isVisible = true
        )
    }

    private val firstRecordId = "firstRecordId"
    private val secondRecordId = "secondRecordId"
    private val firstRecordName = MockDataFactory.randomString()
    private val secondRecordName = MockDataFactory.randomString()
    private val firstRecordType = MockDataFactory.randomString()
    private val secondRecordType = MockDataFactory.randomString()

    private val firstRecord = mapOf(
        ObjectSetConfig.ID_KEY to firstRecordId,
        ObjectSetConfig.NAME_KEY to firstRecordName,
        ObjectSetConfig.TYPE_KEY to firstRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    private val firstObject = ObjectWrapper.Basic(firstRecord)

    private val secondRecord = mapOf(
        ObjectSetConfig.ID_KEY to secondRecordId,
        ObjectSetConfig.NAME_KEY to secondRecordName,
        ObjectSetConfig.TYPE_KEY to secondRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    private val secondObject = ObjectWrapper.Basic(secondRecord)

    private val initialObjects = listOf(firstObject, secondObject)

    private val viewer1 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations
    )

    private val viewer2 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations.mapIndexed { index, viewerRelation ->
            if (index == 0)
                viewerRelation.copy(isVisible = false)
            else
                viewerRelation
        }
    )

    private val dv = Block(
        id = MockDataFactory.randomUuid(),
        content = DV(
            sources = listOf(MockDataFactory.randomString()),
            relationsIndex = relations.map {
                RelationLink(
                    key = it.key,
                    format = it.format
                )
            },
            viewers = listOf(viewer1, viewer2),
            relations = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val dvRestrictions = listOf(
        DataViewRestrictions(
            block = dv.id,
            restrictions = listOf(DataViewRestriction.VIEWS)
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(ObjectSetViewModel.TITLE_CHANNEL_DISPATCH_DELAY)
    }

    @Test
    fun `should show error toast when clicked on read only cell`() = runBlocking {

        // SETUP

        stubSearchWithSubscription(
            subscription = root,
            filters = dv.content<DV>().viewers.first().filters,
            sorts = dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = dv.content<DV>().sources,
            keys = dv.content<DV>().relationsIndex.map { it.key } + defaultDataViewKeys,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0,
            result = SearchResult(
                results = initialObjects,
                dependencies = emptyList(),
                counter = SearchResult.Counter(
                    total = initialObjects.size,
                    prev = 0,
                    next = 0
                )
            )
        )

        stubSubscriptionEventChannel()
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        relations.forEach {
            storeOfRelations.set(
                target = it.id,
                data = it.map
            )
        }

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        val cell = CellView.Description(
            id = firstRecordId,
            relationKey = relations[1].key,
            text = firstRecord[relations[1].key].toString()
        )

        vm.onGridCellClicked(cell = cell)

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED_CELL, result)
    }
}