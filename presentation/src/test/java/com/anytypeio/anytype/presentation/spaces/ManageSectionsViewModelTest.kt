package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.WidgetSectionConfig
import com.anytypeio.anytype.core_models.WidgetSections
import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ManageSectionsViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    private val testConfig = StubConfig()
    private val testSpaceId = SpaceId(testConfig.space)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private fun stubDefaults(
        widgetSections: WidgetSections = WidgetSections.default()
    ) {
        spaceManager.stub {
            on { observe() } doReturn flowOf(testConfig)
        }
        userSettingsRepository.stub {
            on { observeWidgetSections(any()) } doReturn flowOf(widgetSections)
        }
    }

    // ========================================
    // Initial load tests
    // ========================================

    @Test
    fun `initial load produces Content state with all sections`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<ManageSectionsState.Content>(state)
        assertEquals(WidgetSectionType.DEFAULT_ORDER.size, state.sections.size)
    }

    @Test
    fun `initial load sets canReorder true for configurable sections and false for UNREAD`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<ManageSectionsState.Content>(state)
        val unread = state.sections.find { it.type == WidgetSectionType.UNREAD }!!
        assertFalse(unread.canReorder, "UNREAD should have canReorder=false")
        state.sections.filter { it.type != WidgetSectionType.UNREAD }.forEach { section ->
            assertTrue(section.canReorder, "${section.type} should have canReorder=true")
        }
    }

    @Test
    fun `initial load sets canToggle false for UNREAD`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val unread = state.sections.find { it.type == WidgetSectionType.UNREAD }!!
        assertFalse(unread.canToggle)
    }

    @Test
    fun `initial load sets canToggle true for configurable sections`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val configurable = state.sections.filter { it.type != WidgetSectionType.UNREAD }
        configurable.forEach { section ->
            assertTrue(section.canToggle, "${section.type} should have canToggle=true")
        }
    }

    @Test
    fun `initial load preserves custom order from settings`() = runTest {
        val customSections = WidgetSections(
            sections = listOf(
                WidgetSectionConfig(id = WidgetSectionType.BIN, isVisible = true, order = 0),
                WidgetSectionConfig(id = WidgetSectionType.PINNED, isVisible = true, order = 1),
                WidgetSectionConfig(id = WidgetSectionType.UNREAD, isVisible = true, order = 2),
                WidgetSectionConfig(id = WidgetSectionType.OBJECTS, isVisible = true, order = 3),
                WidgetSectionConfig(id = WidgetSectionType.RECENTLY_EDITED, isVisible = true, order = 4)
            )
        )
        stubDefaults(widgetSections = customSections)

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        assertEquals(WidgetSectionType.BIN, state.sections[0].type)
        assertEquals(WidgetSectionType.PINNED, state.sections[1].type)
        assertEquals(WidgetSectionType.UNREAD, state.sections[2].type)
    }

    // ========================================
    // onSectionsReordered tests
    // ========================================

    @Test
    fun `onSectionsReordered updates UI state with new order`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        // Simulate dragging last item to first position
        val reordered = state.sections.toMutableList().also {
            val last = it.removeAt(it.lastIndex)
            it.add(0, last)
        }
        vm.onSectionsReordered(reordered)
        advanceUntilIdle()

        val newState = vm.uiState.value as ManageSectionsState.Content
        assertEquals(WidgetSectionType.BIN, newState.sections[0].type)
        assertEquals(0, newState.sections[0].order)
    }

    @Test
    fun `onSectionsReordered assigns sequential order indices`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val reversed = state.sections.reversed()
        vm.onSectionsReordered(reversed)
        advanceUntilIdle()

        val newState = vm.uiState.value as ManageSectionsState.Content
        newState.sections.forEachIndexed { index, section ->
            assertEquals(index, section.order, "Section at position $index should have order=$index")
        }
    }

    @Test
    fun `onSectionsReordered persists to repository`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val reversed = state.sections.reversed()
        vm.onSectionsReordered(reversed)
        advanceUntilIdle()

        val captor = argumentCaptor<WidgetSections>()
        verify(userSettingsRepository).setWidgetSections(any(), captor.capture())

        val saved = captor.firstValue
        // Verify the saved order matches the reversed order
        assertEquals(WidgetSectionType.BIN, saved.sections[0].id)
        assertEquals(0, saved.sections[0].order)
    }

    @Test
    fun `onSectionsReordered is no-op when state is not Content`() = runTest {
        // Don't stub spaceManager so state stays Loading
        spaceManager.stub {
            on { observe() } doReturn flowOf()
        }

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        // Should not crash
        vm.onSectionsReordered(emptyList())
        advanceUntilIdle()

        assertIs<ManageSectionsState.Loading>(vm.uiState.value)
    }

    // ========================================
    // onSectionVisibilityChanged tests
    // ========================================

    @Test
    fun `onSectionVisibilityChanged hides a section`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        vm.onSectionVisibilityChanged(WidgetSectionType.PINNED, false)
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val pinned = state.sections.find { it.type == WidgetSectionType.PINNED }!!
        assertFalse(pinned.isVisible)
    }

    @Test
    fun `onSectionVisibilityChanged shows a hidden section`() = runTest {
        val withHidden = WidgetSections(
            sections = WidgetSectionType.DEFAULT_ORDER.mapIndexed { index, type ->
                WidgetSectionConfig(
                    id = type,
                    isVisible = type != WidgetSectionType.OBJECTS,
                    order = index
                )
            }
        )
        stubDefaults(widgetSections = withHidden)

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        vm.onSectionVisibilityChanged(WidgetSectionType.OBJECTS, true)
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val objects = state.sections.find { it.type == WidgetSectionType.OBJECTS }!!
        assertTrue(objects.isVisible)
    }

    @Test
    fun `onSectionVisibilityChanged persists to repository`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        vm.onSectionVisibilityChanged(WidgetSectionType.BIN, false)
        advanceUntilIdle()

        val captor = argumentCaptor<WidgetSections>()
        verify(userSettingsRepository).setWidgetSections(any(), captor.capture())

        val binConfig = captor.firstValue.sections.find { it.id == WidgetSectionType.BIN }!!
        assertFalse(binConfig.isVisible)
    }

    @Test
    fun `onSectionVisibilityChanged does not affect other sections`() = runTest {
        stubDefaults()

        val vm = ManageSectionsViewModel(
            spaceManager = spaceManager,
            userSettingsRepository = userSettingsRepository
        )
        advanceUntilIdle()

        vm.onSectionVisibilityChanged(WidgetSectionType.PINNED, false)
        advanceUntilIdle()

        val state = vm.uiState.value as ManageSectionsState.Content
        val others = state.sections.filter { it.type != WidgetSectionType.PINNED }
        others.forEach { section ->
            assertTrue(section.isVisible, "${section.type} should remain visible")
        }
    }
}
