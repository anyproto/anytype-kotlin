package com.anytypeio.anytype.feature_create_object.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class NewCreateObjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val spaceId = SpaceId("test-space-id")

    private lateinit var storeOfObjectTypes: DefaultStoreOfObjectTypes
    private lateinit var spaceViewContainer: FakeSpaceViewSubscriptionContainer

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        spaceViewContainer = FakeSpaceViewSubscriptionContainer()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region: Loading Object Types

    @Test
    fun `should load object types on init`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val noteType = StubObjectType(
            uniqueKey = "ot-note",
            name = "Note",
            recommendedLayout = ObjectType.Layout.NOTE.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, noteType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.objectTypes.size)
            assertEquals(2, state.filteredObjectTypes.size)
            assertTrue(state.objectTypes.any { it.name == "Page" })
            assertTrue(state.objectTypes.any { it.name == "Note" })
        }
    }

    @Test
    fun `should filter out deleted types`() = runTest {
        // Arrange
        val activeType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            isDeleted = false,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val deletedType = StubObjectType(
            uniqueKey = "ot-deleted",
            name = "Deleted",
            isDeleted = true,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(activeType, deletedType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.objectTypes.size)
            assertEquals("Page", state.objectTypes.first().name)
        }
    }

    @Test
    fun `should filter out archived types`() = runTest {
        // Arrange
        val activeType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            isArchived = false,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val archivedType = StubObjectType(
            uniqueKey = "ot-archived",
            name = "Archived",
            isArchived = true,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(activeType, archivedType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.objectTypes.size)
            assertEquals("Page", state.objectTypes.first().name)
        }
    }

    @Test
    fun `should filter out template type`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val templateType = StubObjectType(
            uniqueKey = ObjectTypeIds.TEMPLATE,
            name = "Template",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, templateType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.objectTypes.size)
            assertEquals("Page", state.objectTypes.first().name)
        }
    }

    @Test
    fun `should filter out system layouts like OBJECT_TYPE and PARTICIPANT`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val objectTypeLayout = StubObjectType(
            uniqueKey = "ot-type",
            name = "Type",
            recommendedLayout = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
        )
        val participantLayout = StubObjectType(
            uniqueKey = "ot-participant",
            name = "Participant",
            recommendedLayout = ObjectType.Layout.PARTICIPANT.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, objectTypeLayout, participantLayout))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.objectTypes.size)
            assertEquals("Page", state.objectTypes.first().name)
        }
    }

    @Test
    fun `should handle empty object types list`() = runTest {
        // Arrange - don't add any types
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.objectTypes.isEmpty())
            assertTrue(state.filteredObjectTypes.isEmpty())
        }
    }

    // endregion

    // region: Search Filtering

    @Test
    fun `should filter object types based on search query`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val noteType = StubObjectType(
            uniqueKey = "ot-note",
            name = "Note",
            recommendedLayout = ObjectType.Layout.NOTE.code.toDouble()
        )
        val taskType = StubObjectType(
            uniqueKey = "ot-task",
            name = "Task",
            recommendedLayout = ObjectType.Layout.TODO.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, noteType, taskType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        vm.onSearchQueryChanged("note")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals("note", state.searchQuery)
            assertEquals(3, state.objectTypes.size)
            assertEquals(1, state.filteredObjectTypes.size)
            assertEquals("Note", state.filteredObjectTypes.first().name)
        }
    }

    @Test
    fun `should perform case-insensitive search`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val projectType = StubObjectType(
            uniqueKey = "ot-project",
            name = "Project",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, projectType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act - search with uppercase
        vm.onSearchQueryChanged("PAGE")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredObjectTypes.size)
            assertEquals("Page", state.filteredObjectTypes.first().name)
        }
    }

    @Test
    fun `should show all types when search query is empty`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val noteType = StubObjectType(
            uniqueKey = "ot-note",
            name = "Note",
            recommendedLayout = ObjectType.Layout.NOTE.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType, noteType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // First filter
        vm.onSearchQueryChanged("page")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act - clear search
        vm.onSearchQueryChanged("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(2, state.filteredObjectTypes.size)
        }
    }

    @Test
    fun `should show all types when search query is blank`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act - search with whitespace only
        vm.onSearchQueryChanged("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredObjectTypes.size)
        }
    }

    @Test
    fun `should return empty filtered list when no matches found`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        vm.onSearchQueryChanged("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals("nonexistent", state.searchQuery)
            assertEquals(1, state.objectTypes.size)
            assertTrue(state.filteredObjectTypes.isEmpty())
        }
    }

    // endregion

    // region: Action Handling

    @Test
    fun `should handle UpdateSearch action`() = runTest {
        // Arrange
        val pageType = StubObjectType(
            uniqueKey = "ot-page",
            name = "Page",
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )

        storeOfObjectTypes.merge(listOf(pageType))
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        vm.onAction(CreateObjectAction.UpdateSearch("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertEquals("test", state.searchQuery)
        }
    }

    // endregion

    // region: Default State Values

    @Test
    fun `should have correct default state values`() = runTest {
        // Arrange
        spaceViewContainer.setSpaceUxType(SpaceUxType.DATA)

        // Act
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.showMediaSection)
            assertTrue(state.showAttachExisting)
            assertEquals("", state.searchQuery)
        }
    }

    // endregion

    // region: Helpers

    private fun createViewModel(): NewCreateObjectViewModel {
        val vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId)
        return NewCreateObjectViewModel(
            storeOfObjectTypes = storeOfObjectTypes,
            spaceViewContainer = spaceViewContainer,
            vmParams = vmParams
        )
    }

    // endregion

    // region: Test Doubles

    /**
     * Fake implementation of SpaceViewSubscriptionContainer for testing.
     * Only implements the observe method with mapper that the ViewModel uses.
     */
    private class FakeSpaceViewSubscriptionContainer : SpaceViewSubscriptionContainer {
        private val spaceUxType = MutableStateFlow(SpaceUxType.DATA)
        private val spaceViews = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())

        fun setSpaceUxType(type: SpaceUxType) {
            spaceUxType.value = type
        }

        override fun start() {}
        override fun stop() {}

        override fun observe(): Flow<List<ObjectWrapper.SpaceView>> = spaceViews

        override fun observe(space: SpaceId): Flow<ObjectWrapper.SpaceView> =
            spaceViews.map { it.firstOrNull() ?: throw NoSuchElementException() }

        @Suppress("UNCHECKED_CAST")
        override fun <T> observe(
            space: SpaceId,
            keys: List<String>,
            mapper: (ObjectWrapper.SpaceView) -> T
        ): Flow<T> {
            // For testing, we return the spaceUxType directly since that's what the ViewModel needs
            return spaceUxType as Flow<T>
        }

        override fun get(): List<ObjectWrapper.SpaceView> = spaceViews.value

        override fun get(space: SpaceId): ObjectWrapper.SpaceView? =
            spaceViews.value.firstOrNull { it.targetSpaceId == space.id }
    }

    // endregion
}
