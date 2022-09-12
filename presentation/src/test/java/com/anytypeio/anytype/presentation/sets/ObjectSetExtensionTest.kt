package com.anytypeio.anytype.presentation.sets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
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
    }

    val doc = TypicalTwoRecordObjectSet()

    @Test
    fun `should delete first record from active view`() {

        // SETUP

        val flow: Flow<List<Event>> = flow {
            delay(200)
            emit(
                listOf(
                    Event.Command.DataView.SetRecords(
                        context = root,
                        view = doc.viewer1.id,
                        id = doc.dv.id,
                        total = MockDataFactory.randomInt(),
                        records = doc.initialRecords
                    )
                )
            )
            delay(200)
            emit(
                listOf(
                    Event.Command.DataView.DeleteRecord(
                        context = root,
                        dataViewId = doc.dv.id,
                        viewerId = doc.viewer1.id,
                        recordIds = listOf(doc.firstRecordId)
                    )
                )
            )
        }
        stubInterceptEvents(
            flow = flow
        )
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            )
        )
        stubSetActiveViewer()
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

        coroutineTestRule.advanceTime(200)

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

        coroutineTestRule.advanceTime(1000)
    }
}