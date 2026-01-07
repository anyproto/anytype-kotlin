package com.anytypeio.anytype.presentation.sets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.MockRelationFactory.relationCustomNumber
import com.anytypeio.anytype.presentation.MockRelationFactory.relationCustomText
import com.anytypeio.anytype.presentation.MockRelationFactory.relationIconEmoji
import com.anytypeio.anytype.presentation.MockRelationFactory.relationLastModifiedDate
import com.anytypeio.anytype.presentation.MockRelationFactory.relationName
import com.anytypeio.anytype.presentation.MockRelationFactory.relationStatus
import com.anytypeio.anytype.presentation.MockRelationFactory.relationTag
import com.anytypeio.anytype.presentation.sets.SearchRelationViewModel.Companion.notAllowedRelationFormats
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SearchRelationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val dataViewId = MockDataFactory.randomString()

    @Mock
    lateinit var updateDataViewViewer: UpdateDataViewViewer

    @Mock
    lateinit var analytics: Analytics

    private val dispatcher = Dispatcher.Default<Payload>()

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        // Clear store before each test to prevent state pollution
        runBlocking { storeOfRelations.clear() }
    }

    @ExperimentalTime
    @Test
    fun `non-hidden relations with allowed formats should be visible`() = runTest {

        // SETUP - use only non-hidden relations with allowed formats
        // Note: Hidden relations (isHidden=true) are filtered out by notAllowedRelations()

        val relationNotAcceptedFormat = StubRelationObject(
            key = "key_not_accepted_format",
            name = "Not accepted format",
            format = notAllowedRelationFormats.random(),
            isHidden = false,
            isReadOnly = false,
            objectTypes = listOf()
        )

        val relations = listOf(
            relationLastModifiedDate,  // isHidden=false, DATE format
            relationCustomNumber,      // isHidden=false, NUMBER format
            relationCustomText,        // isHidden=false, LONG_TEXT format
            relationStatus,            // isHidden=false, STATUS format
            relationTag,               // isHidden=false, TAG format
            relationNotAcceptedFormat  // isHidden=false, Not Accepted format
        )

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)

        vm.onStart(viewerId = state.value.dataViewState()!!.viewers.first().id)

        // TESTING - all non-hidden relations should be visible

        val expected = listOf(
            SimpleRelationView(
                key = relationLastModifiedDate.key,
                title = relationLastModifiedDate.name.orEmpty(),
                format = RelationFormat.DATE,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = true
            ),
            SimpleRelationView(
                key = relationCustomNumber.key,
                title = relationCustomNumber.name.orEmpty(),
                format = RelationFormat.NUMBER,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationCustomText.key,
                title = relationCustomText.name.orEmpty(),
                format = RelationFormat.LONG_TEXT,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationStatus.key,
                title = relationStatus.name.orEmpty(),
                format = RelationFormat.STATUS,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationTag.key,
                title = relationTag.name.orEmpty(),
                format = RelationFormat.TAG,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            )
        )

        vm.views.test {
            delay(100)
            assertEquals(expected = expected, actual = awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `relation with format Emoji should not be visible`() = runTest {

        // SETUP - include both emoji and non-emoji format relations
        // relationIconEmoji has EMOJI format and should be filtered out
        // relationCustomNumber and relationCustomText should be visible

        val relations = listOf(
            relationIconEmoji,     // EMOJI format, isHidden=true - should be filtered
            relationCustomNumber,  // NUMBER format, isHidden=false - should be visible
            relationCustomText     // LONG_TEXT format, isHidden=false - should be visible
        )

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)

        vm.onStart(viewerId = state.value.dataViewState()!!.viewers.first().id)

        // TESTING - only non-emoji, non-hidden relations should be visible

        val expected = listOf(
            SimpleRelationView(
                key = relationCustomNumber.key,
                title = relationCustomNumber.name.orEmpty(),
                format = RelationFormat.NUMBER,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            ),
            SimpleRelationView(
                key = relationCustomText.key,
                title = relationCustomText.name.orEmpty(),
                format = RelationFormat.LONG_TEXT,
                isVisible = false,
                isHidden = false,
                isReadonly = false,
                isDefault = false
            )
        )

        vm.views.test {
            delay(100)
            assertEquals(expected = expected, actual = awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `search query filters relations by title`() = runTest {
        // SETUP - use only specific relations to make test predictable
        // Use relations that are NOT hidden and NOT filtered by format
        val relations = listOf(
            relationLastModifiedDate, // isHidden=false, format=DATE
            relationStatus,           // isHidden=false, format=STATUS
            relationTag               // isHidden=false, format=TAG
        )

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING - search for "Status"
        vm.views.test {
            delay(100)
            val initial = awaitItem()
            // Initial should have all 3 relations
            assertEquals(3, initial.size)

            // Now search for "Status"
            vm.onSearchQueryChanged("Status")
            delay(100)

            val filtered = awaitItem()
            // Should only have relations with "Status" in title
            assertEquals(1, filtered.size)
            assertEquals("Status", filtered.first().title)

            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `empty search query shows all relations`() = runTest {
        // SETUP - use only specific relations that won't be filtered
        val relations = listOf(
            relationLastModifiedDate,
            relationStatus,
            relationTag
        )

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING
        vm.views.test {
            delay(100)
            val initial = awaitItem()
            assertEquals(3, initial.size)

            // Search for something specific
            vm.onSearchQueryChanged("Status")
            delay(100)
            val filtered = awaitItem()
            assertEquals(1, filtered.size)

            // Clear the search query
            vm.onSearchQueryChanged("")
            delay(100)
            val restored = awaitItem()

            // Should restore all relations
            assertEquals(3, restored.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `search is case insensitive`() = runTest {
        // SETUP - use multiple relations to verify case-insensitive filtering
        val relations = listOf(
            relationStatus,           // title = "Status"
            relationTag,              // title = "Tag"
            relationLastModifiedDate  // title = "Last modified date"
        )

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING - search with lowercase should find "Status"
        vm.views.test {
            delay(100)
            val initial = awaitItem()
            assertEquals(3, initial.size)

            // Search with lowercase
            vm.onSearchQueryChanged("status")
            delay(100)
            val lowercase = awaitItem()
            assertEquals(1, lowercase.size)
            assertEquals("Status", lowercase.first().title)

            // Reset to show all, then try uppercase
            vm.onSearchQueryChanged("")
            delay(100)
            val reset = awaitItem()
            assertEquals(3, reset.size)

            // Search with uppercase
            vm.onSearchQueryChanged("STATUS")
            delay(100)
            val uppercase = awaitItem()
            assertEquals(1, uppercase.size)
            assertEquals("Status", uppercase.first().title)

            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `onStop cancels all jobs`() = runTest {
        // SETUP - use only one visible relation
        val relations = listOf(relationStatus)

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = listOf(
                    DVViewerRelation(
                        key = relationStatus.key,
                        isVisible = true
                    )
                )
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING
        vm.views.test {
            delay(100)
            val initial = awaitItem()
            assertEquals(1, initial.size)

            // Stop the ViewModel
            vm.onStop()

            // After stop, search query changes should not update views
            // (jobs are cancelled)
            vm.onSearchQueryChanged("NonExistent")
            delay(100)

            // Views should remain unchanged (no new emissions)
            expectNoEvents()

            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `empty state when no relations in dataview`() = runTest {
        // SETUP - empty relations
        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = emptyList(),
                viewerRelations = emptyList()
            )
        )

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING
        vm.views.test {
            delay(100)
            val result = awaitItem()
            assertEquals(emptyList<SimpleRelationView>(), result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `hidden relations are filtered out from results`() = runTest {
        // SETUP - include both hidden and non-hidden relations
        // relationName has isHidden=true, relationStatus has isHidden=false
        val relations = listOf(relationName, relationStatus)

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING - only non-hidden relations should appear
        vm.views.test {
            delay(100)
            val result = awaitItem()
            // Only relationStatus should appear (isHidden=false)
            // relationName should be filtered out (isHidden=true)
            assertEquals(1, result.size)
            assertEquals("Status", result.first().title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `emoji format relations are filtered out`() = runTest {
        // SETUP - include emoji format relation and normal relation
        val relations = listOf(relationIconEmoji, relationStatus)

        val state = MutableStateFlow(
            MockObjectSetFactory.makeDefaultSetObjectState(
                dataViewId = dataViewId,
                relations = relations,
                viewerRelations = relations.map { DVViewerRelation(key = it.key, isVisible = true) }
            )
        )

        storeOfRelations.merge(relations)

        val vm = buildViewModel(state)
        val viewerId = state.value.dataViewState()!!.viewers.first().id

        vm.onStart(viewerId = viewerId)

        // TESTING - emoji format should be filtered out
        vm.views.test {
            delay(100)
            val result = awaitItem()
            // relationIconEmoji has EMOJI format + isHidden=true, so filtered
            // relationStatus should appear
            assertEquals(1, result.size)
            assertEquals("Status", result.first().title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `init state when dataViewState is null returns empty`() = runTest {
        // SETUP - use Init state instead of DataView
        val state = MutableStateFlow<ObjectState>(ObjectState.Init)

        val vm = buildViewModel(state)

        vm.onStart(viewerId = "any-viewer-id")

        // TESTING
        vm.views.test {
            delay(100)
            val result = awaitItem()
            assertEquals(emptyList<SimpleRelationView>(), result)
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun buildViewModel(state: MutableStateFlow<ObjectState>): SelectSortRelationViewModel {
        return SelectSortRelationViewModel(
            objectState = state,
            updateDataViewViewer = updateDataViewViewer,
            dispatcher = dispatcher,
            analytics = analytics,
            storeOfRelations = storeOfRelations
        )
    }
}