package com.anytypeio.anytype.presentation.sets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.MockObjectSetFactory.defaultRelations
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SortingView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ViewerSortByViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should have only add and apply views in state`() {

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf<Block.Content.DataView.Sort>()
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(listOf(SortingView.Add, SortingView.Apply))

        assertEquals(listOf(), vm.sorts.value)
        assertEquals(expected, stateSuccess)
    }


    @Test
    fun `should have set, add and apply in state`() = runTest {


        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[0].key,
                type = Block.Content.DataView.Sort.Type.DESC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(
            defaultRelations
        )

        // TESTING

        vm.onViewCreated(viewerId)

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[0].key,
                    title = defaultRelations[0].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = false,
                    format = ColumnView.Format.LONG_TEXT
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should have proper ordering of sets`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[0].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.DESC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(
            defaultRelations
        )

        // TESTING

        vm.onViewCreated(viewerId)

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[2].key,
                    title = defaultRelations[2].name.orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = false,
                    format = ColumnView.Format.NUMBER
                ),
                SortingView.Set(
                    key = defaultRelations[0].key,
                    title = defaultRelations[0].name.orEmpty().orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.LONG_TEXT
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should remove set from state`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[0].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.DESC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(defaultRelations)

        // TESTING

        vm.onViewCreated(viewerId)
        vm.itemClicked(SortClick.Remove(defaultRelations[0].key))

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[2].key,
                    title = defaultRelations[2].name.orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = false,
                    format = ColumnView.Format.NUMBER
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should add one sort set to state`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.DESC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(defaultRelations)

        // TESTING

        vm.onViewCreated(viewerId)
        vm.itemClicked(SortClick.Add)
        vm.onAddSortKey(defaultRelations[0].key)

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[2].key,
                    title = defaultRelations[2].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = false,
                    format = ColumnView.Format.NUMBER
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Set(
                    key = defaultRelations[0].key,
                    title = defaultRelations[0].name.orEmpty().orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = true,
                    format = ColumnView.Format.LONG_TEXT
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should add one sort set to state and remove add button`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[3].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[4].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(defaultRelations)

        // TESTING

        vm.onViewCreated(viewerId)
        vm.itemClicked(SortClick.Add)
        vm.onAddSortKey(defaultRelations[0].key)

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[2].key,
                    title = defaultRelations[2].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = false,
                    format = ColumnView.Format.NUMBER
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Set(
                    key = defaultRelations[3].key,
                    title = defaultRelations[3].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.TAG
                ),
                SortingView.Set(
                    key = defaultRelations[4].key,
                    title = defaultRelations[4].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.CHECKBOX
                ),
                SortingView.Set(
                    key = defaultRelations[0].key,
                    title = defaultRelations[0].name.orEmpty().orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = true,
                    format = ColumnView.Format.LONG_TEXT
                ),
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should update type value`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(defaultRelations)

        // TESTING

        vm.onViewCreated(viewerId)
        vm.onPickSortType(key = defaultRelations[2].key, type = Viewer.SortType.ASC)
        vm.onPickSortType(key = defaultRelations[1].key, type = Viewer.SortType.DESC)

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[2].key,
                    title = defaultRelations[2].name.orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = false,
                    format = ColumnView.Format.NUMBER
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }

    @Test
    fun `should replace sort`() = runTest {

        // SETUP

        val viewerId = MockDataFactory.randomUuid()
        val sorts = listOf(
            Block.Content.DataView.Sort(
                relationKey = defaultRelations[2].key,
                type = Block.Content.DataView.Sort.Type.DESC
            ),

            Block.Content.DataView.Sort(
                relationKey = defaultRelations[1].key,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultObjectSet(
                viewerId = viewerId,
                sorts = sorts,
                relations = defaultRelations
            )
        )

        val vm = ViewerSortByViewModel(
            state = state,
            storeOfRelations = storeOfRelations
        )

        storeOfRelations.merge(defaultRelations)

        // TESTING

        vm.onViewCreated(viewerId)
        vm.onReplaceSortKey(
            keySelected = defaultRelations[2].key,
            keyNew = defaultRelations[4].key
        )

        val stateSuccess = vm.viewState.value

        val expected = ViewerSortByViewState.Success(
            listOf(
                SortingView.Set(
                    key = defaultRelations[4].key,
                    title = defaultRelations[4].name.orEmpty(),
                    type = Viewer.SortType.DESC,
                    isWithPrefix = false,
                    format = ColumnView.Format.CHECKBOX
                ),
                SortingView.Set(
                    key = defaultRelations[1].key,
                    title = defaultRelations[1].name.orEmpty(),
                    type = Viewer.SortType.ASC,
                    isWithPrefix = true,
                    format = ColumnView.Format.EMAIL
                ),
                SortingView.Add,
                SortingView.Apply
            )
        )

        assertEquals(expected, stateSuccess)
    }
}