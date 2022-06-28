package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class ObjectSetUpdateViewerSortTest : ObjectSetViewModelTestSetup() {

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

        val vm = givenViewModel()

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
                    limit = ObjectSetConfig.DEFAULT_LIMIT,
                    offset = 0
                )
            )
        }

        val sorts = listOf(
            SortingExpression(
                key = doc.relations.first().key,
                type = Viewer.SortType.DESC
            )
        )

        vm.onUpdateViewerSorting(
            sorts = sorts
        )

        // Veryfing that sorts are updated for the second viewer.

        verifyBlocking(updateDataViewViewer, times(1)) {
            invoke(
                UpdateDataViewViewer.Params(
                    context = root,
                    target = doc.dv.id,
                    viewer = doc.viewer2.copy(
                        sorts = listOf(
                            DVSort(
                                relationKey = doc.relations.first().key,
                                type = DVSortType.DESC
                            )
                        )
                    )
                )
            )
        }
    }
}