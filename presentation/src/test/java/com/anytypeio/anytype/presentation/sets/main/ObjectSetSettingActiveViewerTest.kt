package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewFilters
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ObjectSetSettingActiveViewerTest : ObjectSetViewModelTestSetup() {

    val doc = TypicalTwoRecordObjectSet()

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

        val setOf = listOf(MockDataFactory.randomString())
        val keys = (ObjectSearchConstants.defaultDataViewKeys + doc.dv.content<DV>().relationsIndex.map { it.key }).distinct()

        stubSearchWithSubscription(
            subscription = root,
            filters = doc.dv.content<DV>().viewers.first().filters + defaultDataViewFilters(),
            sorts = doc.dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = setOf,
            keys = keys,
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

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf("setOf" to setOf)
                )
            )
        )

        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            details = details
        )

        println(storeOfRelations.getAll().toString())

        println(doc.relationsObjects)

        runBlocking {
            storeOfRelations.merge(
                relations = doc.relationsObjects
            )
        }

        println(storeOfRelations.getAll().toString())

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
                    key = doc.relationsObjects[0].key,
                    text = doc.relationsObjects[0].name.orEmpty(),
                    format = ColumnView.Format.LONG_TEXT,
                    width = 0,
                    isVisible = true,
                    isHidden = false,
                    isReadOnly = false
                ),
                ColumnView(
                    key = doc.relationsObjects[1].key,
                    text = doc.relationsObjects[1].name.orEmpty(),
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
                            text = doc.firstRecord[doc.relationsObjects[0].key] as String,
                            relationKey = doc.relationsObjects[0].key
                        ),
                        CellView.Description(
                            id = doc.firstRecordId,
                            text = doc.firstRecord[doc.relationsObjects[1].key] as String,
                            relationKey = doc.relationsObjects[1].key
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
                            text = doc.secondRecord[doc.relationsObjects[0].key] as String,
                            relationKey = doc.relationsObjects[0].key
                        ),
                        CellView.Description(
                            id = doc.secondRecordId,
                            text = doc.secondRecord[doc.relationsObjects[1].key] as String,
                            relationKey = doc.relationsObjects[1].key
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
                            text = doc.secondRecord[doc.relationsObjects[1].key] as String,
                            relationKey = doc.relationsObjects[1].key
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
                            text = doc.firstRecord[doc.relationsObjects[1].key] as String,
                            relationKey = doc.relationsObjects[1].key
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