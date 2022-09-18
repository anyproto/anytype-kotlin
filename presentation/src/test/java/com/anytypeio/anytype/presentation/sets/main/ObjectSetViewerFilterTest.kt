package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.FilterExpression
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

class ObjectSetViewerFilterTest : ObjectSetViewModelTestSetup() {

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
    fun `should set second viewer as active viewer and request sort updates for this viewer`() {

        // SETUP

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            )
        )
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onViewerTabClicked(viewer = doc.viewer2.id)

        // Verifying that the second view is now active

        assertEquals(
            expected = doc.viewer2.id,
            actual = session.currentViewerId.value
        )

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