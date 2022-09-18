package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ObjectSetSettingActiveViewerTest : ObjectSetViewModelTestSetup() {

    val doc = TypicalTwoRecordObjectSet()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    @Test
    fun `should set active viewer and update row order and column count`() = runTest {

        // SETUP

        val delayBeforeChangingOrder = 3000L

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSubscriptionEventChannel(
            flow = flow {
                delay(delayBeforeChangingOrder)
                emit(
                    listOf(
                        SubscriptionEvent.Position(
                            target = doc.firstRecordId,
                            afterId = doc.secondRecordId
                        )
                    )
                )
            }
        )
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
        stubSearchWithSubscription(
            subscription = root,
            filters = doc.dv.content<DV>().viewers.last().filters,
            sorts = doc.dv.content<DV>().viewers.last().sorts,
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
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            )
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.currentViewer.test {
            val valueBeforeLoading = awaitItem()

            assertIs<Viewer.GridView>(valueBeforeLoading)

            assertEquals(
                expected = 0,
                actual = valueBeforeLoading.rows.size
            )

            val valueBeforeChangingViewer = awaitItem()

            // Expecting that all columns are visibile (number of columns == number of relations)

            val expectedColumnsBefore = listOf(
                ColumnView(
                    key = doc.relations[0].key,
                    text = doc.relations[0].name,
                    format = ColumnView.Format.LONG_TEXT,
                    width = 0,
                    isVisible = true,
                    isHidden = false,
                    isReadOnly = false
                ),
                ColumnView(
                    key = doc.relations[1].key,
                    text = doc.relations[1].name,
                    format = ColumnView.Format.LONG_TEXT,
                    width = 0,
                    isVisible = true,
                    isHidden = false,
                    isReadOnly = false
                )
            )

            // Expecting that cells corresponding to both the first and the second relation are visible.

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

            assertIs<Viewer.GridView>(valueBeforeChangingViewer)

            assertEquals(
                expected = expectedColumnsBefore,
                actual = valueBeforeChangingViewer.columns
            )

            assertEquals(
                expected = expectedRowsBefore,
                actual = valueBeforeChangingViewer.rows
            )

            // Selecting second viewer

            vm.onViewerTabClicked(doc.viewer2.id)

            coroutineTestRule.advanceTime(delayBeforeChangingOrder)

            val valueAfterSelectingSecondViewerAndLoading = awaitItem()

            val valueAfterSelectingSecondViewer = awaitItem()

            val expectedColumnsAfter = listOf(expectedColumnsBefore[1])

            // Expecting that cells corresponding to the first relations are not visible
            // Expecting that row order is reversed, because record order was reversed.

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
                            text = doc.secondRecord[doc.relations[1].key] as String,
                            key = doc.relations[1].key
                        )
                    )
                ),
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
                            text = doc.firstRecord[doc.relations[1].key] as String,
                            key = doc.relations[1].key
                        )
                    )
                )
            )

            assertTrue { valueAfterSelectingSecondViewer is Viewer.GridView }

            check(valueAfterSelectingSecondViewer is Viewer.GridView)

            assertEquals(
                expected = expectedColumnsAfter,
                actual = valueAfterSelectingSecondViewer.columns
            )

            assertEquals(
                expected = expectedRowsAfter,
                actual = valueAfterSelectingSecondViewer.rows
            )
        }
    }
}