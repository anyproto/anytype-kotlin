package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.widgets.TemplatesWidgetUiState
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetTemplatesMenuTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    val templateView = StubObject(objectType = ObjectTypeIds.PAGE)

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        runTest {
            mockObjectSet = MockSet(context = root, setOfValue = ObjectTypeIds.PAGE)
            val pageTypeMap = mapOf(
                Relations.ID to ObjectTypeIds.PAGE,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString()
            )
            stubWorkspaceManager(mockObjectSet.workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
                details = mockObjectSet.details
            )
            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                workspace = mockObjectSet.workspaceId,
                storeOfRelations = storeOfRelations,
                keys = mockObjectSet.dvKeys,
                sources = listOf(ObjectTypeIds.PAGE),
                dvFilters = mockObjectSet.filters
            )
            stubStoreOfObjectTypes(pageTypeMap)
            stubTemplatesContainer(
                type = ObjectTypeIds.PAGE,
                templates = listOf(templateView)
            )
        }
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    //region INIT STATE
    @Test
    fun `should be empty state on start`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT STATES
        val templatesFlow = viewModel.templatesWidgetState.testIn(backgroundScope)

        // ASSERT STATES
        val state = templatesFlow.awaitItem()
        assertIs<TemplatesWidgetUiState>(state)
        assertEquals(
            TemplatesWidgetUiState.init(), state
        )
    }
    //endregion

    //region SHOW WIDGET
    @Test
    fun `should show widget when click on new button`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            val second = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(showWidget = true), second
            )
        }
    }

    @Test
    fun `should reset widget when click on dismiss`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onDismissTemplatesWidget()
            val result = awaitItem()
            assertEquals(TemplatesWidgetUiState.init(), result)
        }
    }

    @Test
    fun `should reset widget when click on template`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onTemplateItemClicked(item = StubTemplateView(id = templateView.id))
            val result = awaitItem()
            assertEquals(TemplatesWidgetUiState.init(), result)
        }
    }
    //endregion

    //region SHOW WIDGET - EDIT MODE
    @Test
    fun `should be in edit mode when click on edit`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            val third = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true
                ), third
            )
        }
    }

    @Test
    fun `should not be in edit mode when click on done`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onDoneTemplateButtonClicked()
            val fourth = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = false
                ), fourth
            )
        }
    }

    @Test
    fun `should reset widget when click on template in edit mode`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onTemplateItemClicked(item = StubTemplateView(id = templateView.id))
            val fourth = awaitItem()
            assertEquals(TemplatesWidgetUiState.init(), fourth)
        }
    }

    @Test
    fun `should reset widget when click on dismiss in edit mode`() = runTest {
        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onDismissTemplatesWidget()
            val fourth = awaitItem()
            assertEquals(TemplatesWidgetUiState.init(), fourth)
        }
    }
    //endregion

    //region SHOW WIDGET - EDIT MODE - MORE MENU
    @Test
    fun `should show more menu when click on more button in edit mode`() = runTest {
        //SETUP
        val templateView = StubTemplateView(id = templateView.id)

        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            val fourth = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true,
                    isMoreMenuVisible = true,
                    moreMenuTemplate = templateView
                ), fourth
            )
        }
    }

    @Test
    fun `should not show more menu when click on dismiss`() = runTest {
        //SETUP
        val templateView = StubTemplateView(id = templateView.id)

        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            awaitItem()
            viewModel.onDismissTemplatesWidget()
            val result = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true,
                    isMoreMenuVisible = false,
                    moreMenuTemplate = null
                ), result
            )
        }
    }

    @Test
    fun `should not show more menu when click on item`() = runTest {
        //SETUP
        val templateView = StubTemplateView(id = templateView.id)

        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            awaitItem()
            viewModel.onTemplateItemClicked(item = templateView)
            val result = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true,
                    isMoreMenuVisible = false,
                    moreMenuTemplate = null
                ), result
            )
        }
    }

    @Test
    fun `should not show more menu when click on done`() = runTest {
        //SETUP
        val templateView = StubTemplateView(id = templateView.id)

        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            awaitItem()
            viewModel.onDoneTemplateButtonClicked()
            val result = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true,
                    isMoreMenuVisible = false,
                    moreMenuTemplate = null
                ), result
            )
        }
    }

    @Test
    fun `should not show more menu when click on more`() = runTest {
        //SETUP
        val templateView = StubTemplateView(id = templateView.id)

        // TESTING
        viewModel.onStart(ctx = root)

        viewModel.templatesWidgetState.test {
            awaitItem()
            viewModel.onNewButtonIconClicked()
            awaitItem()
            viewModel.onEditTemplateButtonClicked()
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            awaitItem()
            viewModel.onMoreTemplateButtonClicked(template = templateView)
            val result = awaitItem()
            assertEquals(
                TemplatesWidgetUiState.init().copy(
                    showWidget = true,
                    isEditing = true,
                    isMoreMenuVisible = false,
                    moreMenuTemplate = null
                ), result
            )
        }
    }

}


fun StubTemplateView(id: Id = MockDataFactory.randomString()): TemplateView.Template {
    return TemplateView.Template(
        id = id,
        name = MockDataFactory.randomString(),
        typeId = MockDataFactory.randomString(),
        layout = ObjectType.Layout.BASIC,
        image = null,
        emoji = null,
        coverColor = null,
        coverImage = null,
        coverGradient = null
    )
}