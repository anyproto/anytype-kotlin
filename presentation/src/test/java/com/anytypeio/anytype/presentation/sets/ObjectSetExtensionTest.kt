package com.anytypeio.anytype.presentation.sets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObjectSetExtensionTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    val doc = TypicalTwoRecordObjectSet()

    @Test
    fun `should delete first record from active view`() {

        // SETUP

        stubSearchWithSubscription(
            subscription = root,
            filters = doc.dv.content<DV>().viewers.first().filters,
            sorts = doc.dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = doc.dv.content<DV>().sources,
            keys = doc.dv.content<DV>().relations.map { it.key },
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0,
            result = SearchResult(
                results = doc.initialObjects,
                dependencies = emptyList(),
                counter = SearchResult.Counter(
                    total = doc.initialObjects.size,
                    prev = 0,
                    next = 0
                )
            )
        )

        val delayBeforeDeletionEvent = 3000L

        stubSubscriptionEventChannel(
            flow = flow {
                delay(delayBeforeDeletionEvent)
                emit(
                    listOf(
                        SubscriptionEvent.Remove(
                            subscription = root,
                            target = doc.firstRecordId
                        )
                    )
                )
            }
        )

        stubInterceptEvents(
            flow = emptyFlow()
        )
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            )
        )
        stubUpdateDataViewViewer()
        stubInterceptThreadStatus()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        coroutineTestRule.advanceTime(200)

        val viewerBefore = vm.currentViewer.value

        val expectedRowsBefore = listOf(
            Viewer.GridView.Row(
                id = doc.firstRecordId,
                name = doc.firstRecordName,
                emoji = null,
                image = null,
                type = doc.firstRecordType,
                showIcon = true,
                cells = listOf(
                    CellView.Description(
                        id = doc.firstRecordId,
                        text = doc.firstRecord[doc.relations[0].key] as String,
                        key = doc.relations[0].key
                    ),
                    CellView.Description(
                        id = doc.firstRecordId,
                        text = doc.firstRecord[doc.relations[1].key] as String,
                        key = doc.relations[1].key
                    )
                )
            ),
            Viewer.GridView.Row(
                id = doc.secondRecordId,
                name = doc.secondRecordName,
                emoji = null,
                image = null,
                type = doc.secondRecordType,
                showIcon = true,
                cells = listOf(
                    CellView.Description(
                        id = doc.secondRecordId,
                        text = doc.secondRecord[doc.relations[0].key] as String,
                        key = doc.relations[0].key
                    ),
                    CellView.Description(
                        id = doc.secondRecordId,
                        text = doc.secondRecord[doc.relations[1].key] as String,
                        key = doc.relations[1].key
                    )
                )
            )
        )

        assertIs<Viewer.GridView>(viewerBefore)

        assertEquals(
            expected = expectedRowsBefore,
            actual = viewerBefore.rows
        )

        coroutineTestRule.advanceTime(delayBeforeDeletionEvent)

        val viewerAfter = vm.currentViewer.value

        val expectedRowsAfter = listOf(
            Viewer.GridView.Row(
                id = doc.secondRecordId,
                name = doc.secondRecordName,
                emoji = null,
                image = null,
                type = doc.secondRecordType,
                showIcon = true,
                cells = listOf(
                    CellView.Description(
                        id = doc.secondRecordId,
                        text = doc.secondRecord[doc.relations[0].key] as String,
                        key = doc.relations[0].key
                    ),
                    CellView.Description(
                        id = doc.secondRecordId,
                        text = doc.secondRecord[doc.relations[1].key] as String,
                        key = doc.relations[1].key
                    )
                )
            )
        )

        assertIs<Viewer.GridView>(viewerAfter)

        assertEquals(
            expected = expectedRowsAfter,
            actual = viewerAfter.rows
        )
    }
}