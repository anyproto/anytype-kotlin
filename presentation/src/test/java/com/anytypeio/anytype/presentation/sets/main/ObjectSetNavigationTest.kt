package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

class ObjectSetNavigationTest : ObjectSetViewModelTestSetup() {

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
        coroutineTestRule.advanceTime(ObjectSetViewModel.TITLE_CHANNEL_DISPATCH_DELAY)
    }

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private val linkedProjectRelation = Relation(
        key = MockDataFactory.randomString(),
        name = "Linked objects",
        source = Relation.Source.values().random(),
        defaultValue = null,
        format = Relation.Format.OBJECT,
        isHidden = false,
        isMulti = true,
        isReadOnly = false,
        selections = emptyList()
    )

    private val objectRelations = listOf(linkedProjectRelation)

    val viewerRelations = objectRelations.map { relation ->
        DVViewerRelation(
            key = relation.key,
            isVisible = true
        )
    }

    private val viewer = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = viewerRelations
    )

    private val dv = Block(
        id = MockDataFactory.randomUuid(),
        content = DV(
            sources = listOf(MockDataFactory.randomString()),
            relations = objectRelations,
            viewers = listOf(viewer)
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @ExperimentalTime
    @Test
    fun `should emit navigation command for editing relation-object cell`() {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            dataViewRestrictions = emptyList(),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    records = listOf(record),
                    view = viewer.id,
                    id = dv.id,
                    total = 1,
                    context = root
                )
            )
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val state = vm.viewerGrid.value

        assertIs<Viewer.GridView>(state)

        assertEquals(
            expected = 1,
            actual = state.rows.size
        )

        // Clicking on cell with linked projects.

        runBlocking {
            vm.commands.test {
                vm.onGridCellClicked(state.rows.first().cells.last())
                assertEquals(
                    awaitItem(),
                    ObjectSetCommand.Modal.EditRelationCell(
                        ctx = root,
                        dataview = dv.id,
                        target = firstRecordId,
                        viewer = viewer.id,
                        relation = linkedProjectRelation.key,
                        targetObjectTypes = emptyList()
                    )
                )
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Test
    fun `should emit navigation command for opening an object contained in given relation if this relation is read-only and object's layout is supported`() {

        // SETUP

        val supportedObjectLayouts = listOf(
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE
        )

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val details = Block.Details(
            details = mapOf(
                linkedProjectTargetId to Block.Fields(
                    mapOf(
                        Relations.ID to linkedProjectTargetId,
                        Relations.LAYOUT to supportedObjectLayouts.random().code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubCloseBlock()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv.copy(
                    content = dv.content<DV>().copy(
                        relations = listOf(
                            linkedProjectRelation.copy(
                                isReadOnly = true
                            )
                        )
                    )
                )
            ),
            dataViewRestrictions = emptyList(),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    records = listOf(record),
                    view = viewer.id,
                    id = dv.id,
                    total = 1,
                    context = root
                )
            ),
            details = details
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val state = vm.viewerGrid.value

        assertIs<Viewer.GridView>(state)

        assertEquals(
            expected = 1,
            actual = state.rows.size
        )

        // Clicking on cell with linked projects.

        val testObserver = vm.navigation.test()

        vm.onGridCellClicked(state.rows.first().cells.last())

        testObserver.assertValue { value ->
            val content = value.peekContent()
            content == AppNavigation.Command.OpenObject(linkedProjectTargetId)
        }
    }

    @Test
    fun `should close current object before navitating to some other object`() {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val details = Block.Details(
            details = mapOf(
                linkedProjectTargetId to Block.Fields(
                    mapOf(
                        Relations.ID to linkedProjectTargetId,
                        Relations.LAYOUT to SupportedLayouts.layouts.random().code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubCloseBlock()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv.copy(
                    content = dv.content<DV>().copy(
                        relations = listOf(
                            linkedProjectRelation.copy(
                                isReadOnly = true
                            )
                        )
                    )
                )
            ),
            dataViewRestrictions = emptyList(),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    records = listOf(record),
                    view = viewer.id,
                    id = dv.id,
                    total = 1,
                    context = root
                )
            ),
            details = details
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val state = vm.viewerGrid.value

        assertIs<Viewer.GridView>(state)

        assertEquals(
            expected = 1,
            actual = state.rows.size
        )

        // Clicking on cell with linked projects.

        vm.onGridCellClicked(state.rows.first().cells.last())

        verifyBlocking(closeBlock, times(1)) {
            invoke(CloseBlock.Params(root))
        }
    }

    @Test
    fun `should not emit any navigation command for opening an object contained in given relation if object's layout is not supported`() {

        // SETUP

        val unsupportedLayouis = ObjectType.Layout.values().toList() - SupportedLayouts.layouts

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val details = Block.Details(
            details = mapOf(
                linkedProjectTargetId to Block.Fields(
                    mapOf(
                        Relations.ID to linkedProjectTargetId,
                        Relations.LAYOUT to unsupportedLayouis.random().code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubCloseBlock()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv.copy(
                    content = dv.content<DV>().copy(
                        relations = listOf(
                            linkedProjectRelation.copy(
                                isReadOnly = true
                            )
                        )
                    )
                )
            ),
            dataViewRestrictions = emptyList(),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    records = listOf(record),
                    view = viewer.id,
                    id = dv.id,
                    total = 1,
                    context = root
                )
            ),
            details = details
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val state = vm.viewerGrid.value

        assertIs<Viewer.GridView>(state)

        assertEquals(
            expected = 1,
            actual = state.rows.size
        )

        // Clicking on cell with linked projects.

        val testObserver = vm.navigation.test()

        vm.onGridCellClicked(state.rows.first().cells.last())

        testObserver.assertNoValue()
    }

    @Test
    fun `should emit navigation command opening an object set contained in given relation if this relation is read-only`() {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val details = Block.Details(
            details = mapOf(
                linkedProjectTargetId to Block.Fields(
                    mapOf(
                        Relations.ID to linkedProjectTargetId,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble()
                    )
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSetActiveViewer()
        stubCloseBlock()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv.copy(
                    content = dv.content<DV>().copy(
                        relations = listOf(
                            linkedProjectRelation.copy(
                                isReadOnly = true
                            )
                        )
                    )
                )
            ),
            dataViewRestrictions = emptyList(),
            additionalEvents = listOf(
                Event.Command.DataView.SetRecords(
                    records = listOf(record),
                    view = viewer.id,
                    id = dv.id,
                    total = 1,
                    context = root
                )
            ),
            details = details
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val state = vm.viewerGrid.value

        assertIs<Viewer.GridView>(state)

        assertEquals(
            expected = 1,
            actual = state.rows.size
        )

        // Clicking on cell with linked projects.

        val testObserver = vm.navigation.test()

        vm.onGridCellClicked(state.rows.first().cells.last())

        testObserver.assertValue { value ->
            val content = value.peekContent()
            content == AppNavigation.Command.OpenObjectSet(linkedProjectTargetId)
        }
    }
}