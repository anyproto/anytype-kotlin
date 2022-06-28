package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.presentation.TypicalTwoRecordObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetRestrictionsTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(100)
    }

    val doc = TypicalTwoRecordObjectSet()

    @Test
    fun `should show error toast when clicked on viewer button`() = runBlocking {

        val dvRestrictions = listOf<DataViewRestrictions>(
            DataViewRestrictions(
                block = doc.dv.id,
                restrictions = listOf(DataViewRestriction.VIEWS)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onExpandViewerMenuClicked()

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED, result)
    }

    @Test
    fun `should show error toast when clicked on add object button`() = runBlocking {

        val dvRestrictions = listOf(
            DataViewRestrictions(
                block = doc.dv.id,
                restrictions = listOf(DataViewRestriction.CREATE_OBJECT)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onCreateNewRecord()

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED, result)
    }

    @Test
    fun `should show error toast when clicked on add filter button`() = runBlocking {

        val dvRestrictions = listOf(
            DataViewRestrictions(
                block = doc.dv.id,
                restrictions = listOf(DataViewRestriction.VIEWS)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onViewerFiltersClicked()

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED, result)
    }

    @Test
    fun `should show error toast when clicked on add sorts button`() = runBlocking {

        val dvRestrictions = listOf(
            DataViewRestrictions(
                block = doc.dv.id,
                restrictions = listOf(DataViewRestriction.VIEWS)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onViewerSortsClicked()

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED, result)
    }

    @Test
    fun `should show error toast when clicked on relations button`() = runBlocking {

        val dvRestrictions = listOf(
            DataViewRestrictions(
                block = doc.dv.id,
                restrictions = listOf(DataViewRestriction.RELATION)
            )
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                doc.header,
                doc.title,
                doc.dv
            ),
            dataViewRestrictions = dvRestrictions
        )

        stubSetActiveViewer()
        stubUpdateDataViewViewer()

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)

        vm.onViewerRelationsClicked()

        val result = vm.toasts.stream().first()

        assertEquals(ObjectSetViewModel.NOT_ALLOWED, result)
    }
}