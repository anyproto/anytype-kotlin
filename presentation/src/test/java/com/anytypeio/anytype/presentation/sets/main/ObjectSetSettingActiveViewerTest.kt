package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
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
    }

    @Test
    fun `should set active viewer and update row order and column count`() {

        // SETUP

        val updatedRecords = listOf(doc.secondRecord, doc.firstRecord)

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    context = root,
                    view = doc.viewer1.id,
                    id = doc.dv.id,
                    total = MockDataFactory.randomInt(),
                    records = doc.initialRecords
                )
            )
        )

        setActiveViewer.stub {
            onBlocking { invoke(
                SetActiveViewer.Params(
                    context = root,
                    block = doc.dv.id,
                    view = doc.viewer1.id,
                    limit = ObjectSetConfig.DEFAULT_LIMIT
                )
            ) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = listOf(
                        Event.Command.DataView.SetRecords(
                            context = root,
                            view = doc.viewer1.id,
                            id = doc.dv.id,
                            total = MockDataFactory.randomInt(),
                            records = doc.initialRecords
                        )
                    )
                )
            )
        }

        setActiveViewer.stub {
            onBlocking { invoke(
                SetActiveViewer.Params(
                    context = root,
                    block = doc.dv.id,
                    view = doc.viewer2.id,
                    limit = ObjectSetConfig.DEFAULT_LIMIT
                )
            ) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = listOf(
                        Event.Command.DataView.SetRecords(
                            context = root,
                            id = doc.dv.id,
                            view = doc.viewer2.id,
                            records = updatedRecords,
                            total = MockDataFactory.randomInt()
                        )
                    )
                )
            )
        }

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        val valueBefore = vm.viewerGrid.value

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

        assertIs<Viewer.GridView>(valueBefore)

        assertEquals(
            expected = expectedColumnsBefore,
            actual = valueBefore.columns
        )

        assertEquals(
            expected = expectedRowsBefore,
            actual = valueBefore.rows
        )

        // Selecting second viewer

        vm.onViewerTabClicked(doc.viewer2.id)

        val valueAfter = vm.viewerGrid.value

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

        assertTrue { valueAfter is Viewer.GridView }

        check(valueAfter is Viewer.GridView)

        assertEquals(
            expected = expectedColumnsAfter,
            actual = valueAfter.columns
        )

        assertEquals(
            expected = expectedRowsAfter,
            actual = valueAfter.rows
        )
    }
}