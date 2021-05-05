package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.sets.model.FilterExpression
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class ObjectSetViewerFilterTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    val doc = TypicalTwoRecordObjectSet()

    @Test
    fun `should set second viewer as active viewer and request sort updates for this viewer`() {

        // SETUP

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
        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.onViewerTabClicked(viewer = doc.viewer2.id)

        // Verifying that we want to make second viewer active.

        verifyBlocking(setActiveViewer, times(1)) {
            invoke(
                SetActiveViewer.Params(
                    context = root,
                    block = doc.dv.id,
                    view = doc.viewer2.id,
                    limit = 0,
                    offset = 0
                )
            )
        }

        val filters = listOf(
            FilterExpression(
                key = doc.relations.first().key,
                operator = Viewer.FilterOperator.Or,
                condition = Viewer.Filter.Condition.Text.Equal(),
                value = FilterValue.Text(MockDataFactory.randomString())
            ),
            FilterExpression(
                key = doc.relations.first().key,
                operator = Viewer.FilterOperator.Or,
                condition = Viewer.Filter.Condition.Text.Equal(),
                value = FilterValue.Text(MockDataFactory.randomString())
            ),
        )

        vm.onUpdateViewerFilters(
            filters = filters
        )

        // Veryfing that filters are updated for the second viewer.

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params(
                    context = root,
                    target = doc.dv.id,
                    viewer = doc.viewer2.copy(
                        filters = listOf(
                            Block.Content.DataView.Filter(
                                relationKey = doc.relations.first().key,
                                operator = Block.Content.DataView.Filter.Operator.OR,
                                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                                value = (filters[0].value as FilterValue.Text).value
                            ),
                            Block.Content.DataView.Filter(
                                relationKey = doc.relations.first().key,
                                operator = Block.Content.DataView.Filter.Operator.OR,
                                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                                value = (filters[1].value as FilterValue.Text).value
                            )
                        )
                    )
                )
            )
        }
    }
}