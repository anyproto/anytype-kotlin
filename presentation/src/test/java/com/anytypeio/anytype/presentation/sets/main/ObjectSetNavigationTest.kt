package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.ValueClassAnswer
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.stub
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
        initDataViewSubscriptionContainer()
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(ObjectSetViewModel.TITLE_CHANNEL_DISPATCH_DELAY)
    }

    private val title = StubTitle()

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
    fun `should emit navigation command for editing relation-object cell`() = runTest {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val obj = ObjectWrapper.Basic(record)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSubscriptionEventChannel()
        stubSearchWithSubscription(
            subscription = root,
            filters = dv.content<DV>().viewers.first().filters,
            sorts = dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = dv.content<DV>().sources,
            keys = ObjectSearchConstants.defaultKeys + dv.content<DV>().relations.map { it.key },
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0,
            result = SearchResult(
                results = listOf(obj),
                dependencies = emptyList(),
                counter = SearchResult.Counter(
                    total = 1,
                    prev = 0,
                    next = 0
                )
            )
        )
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            dataViewRestrictions = emptyList()
        )

        val vm = givenViewModel()

        // TESTING

        vm.onStart(root)


        vm.currentViewer.test {
            // State before objects are laoded
            val first = awaitItem()

            assertIs<Viewer.GridView>(first)

            assertEquals(
                expected = 0,
                actual = first.rows.size
            )

            // Now object is loaded
            val second = awaitItem()

            assertIs<Viewer.GridView>(second)

            assertEquals(
                expected = 1,
                actual = second.rows.size
            )

            // Clicking on cell with linked projects.

            vm.commands.test {
                vm.onGridCellClicked(second.rows.first().cells.last())
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
    fun `should emit navigation command for opening an object contained in given relation if this relation is read-only and object's layout is supported`() =
        runTest {

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

            val obj = ObjectWrapper.Basic(record)
            val linkedObject = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to linkedProjectTargetId,
                    Relations.NAME to MockDataFactory.randomString(),
                    Relations.LAYOUT to ObjectType.Layout.BASIC
                )
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
            stubCloseBlock()
            stubSubscriptionEventChannel()
            stubSearchWithSubscription(
                subscription = root,
                filters = dv.content<DV>().viewers.first().filters,
                sorts = dv.content<DV>().viewers.first().sorts,
                afterId = null,
                beforeId = null,
                sources = dv.content<DV>().sources,
                keys = ObjectSearchConstants.defaultKeys + dv.content<DV>().relations.map { it.key },
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                offset = 0,
                result = SearchResult(
                    results = listOf(obj),
                    dependencies = listOf(linkedObject),
                    counter = SearchResult.Counter(
                        total = 1,
                        prev = 0,
                        next = 0
                    )
                )
            )
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
                details = details
            )

            val vm = givenViewModel()

            vm.onStart(root)

            // TESTING

            vm.currentViewer.test {
                val stateBeforeLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateBeforeLoaded)

                assertEquals(
                    expected = 0,
                    actual = stateBeforeLoaded.rows.size
                )

                val stateAfterLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateAfterLoaded)

                assertEquals(
                    expected = 1,
                    actual = stateAfterLoaded.rows.size
                )

                // Clicking on cell with linked projects.
                val testObserver = vm.navigation.test()

                vm.onGridCellClicked(stateAfterLoaded.rows.first().cells.last())

                testObserver.assertValue { value ->
                    val content = value.peekContent()
                    content == AppNavigation.Command.OpenObject(linkedProjectTargetId)
                }
            }
        }

    @Test
    fun `should close current object before navigating to some other object`() = runTest {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )
        val obj = ObjectWrapper.Basic(record)
        val linkedObject = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to linkedProjectTargetId,
                Relations.NAME to MockDataFactory.randomString(),
                Relations.LAYOUT to ObjectType.Layout.BASIC
            )
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
        stubCloseBlock()
        stubSubscriptionEventChannel()
        stubSearchWithSubscription(
            subscription = root,
            filters = dv.content<DV>().viewers.first().filters,
            sorts = dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = dv.content<DV>().sources,
            keys = ObjectSearchConstants.defaultKeys + dv.content<DV>().relations.map { it.key },
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0,
            result = SearchResult(
                results = listOf(obj),
                dependencies = listOf(linkedObject),
                counter = SearchResult.Counter(
                    total = 1,
                    prev = 0,
                    next = 0
                )
            )
        )
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
            details = details
        )

        val vm = givenViewModel()

        vm.onStart(root)

        // TESTING

        vm.currentViewer.test {
            val stateBeforeLoaded = awaitItem()

            assertIs<Viewer.GridView>(stateBeforeLoaded)

            assertEquals(
                expected = 0,
                actual = stateBeforeLoaded.rows.size
            )

            assertIs<Viewer.GridView>(stateBeforeLoaded)

            val stateAfterLoaded = awaitItem()

            assertIs<Viewer.GridView>(stateAfterLoaded)

            assertEquals(
                expected = 1,
                actual = stateAfterLoaded.rows.size
            )

            // Clicking on cell with linked projects.

            vm.onGridCellClicked(stateAfterLoaded.rows.first().cells.last())

            verifyBlocking(closeBlock, times(1)) {
                execute(root)
            }
        }
    }

    @Test
    fun `should not emit any navigation command for opening an object contained in given relation if object's layout is not supported`() =
        runTest {

            // SETUP

            val unsupportedLayouis = ObjectType.Layout.values().toList() - SupportedLayouts.layouts

            val linkedProjectTargetId = "Linked project ID"
            val firstRecordId = "First record ID"

            val record = mapOf(
                Relations.ID to firstRecordId,
                linkedProjectRelation.key to linkedProjectTargetId
            )
            val obj = ObjectWrapper.Basic(record)
            val linkedObject = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to linkedProjectTargetId,
                    Relations.NAME to MockDataFactory.randomString(),
                    Relations.LAYOUT to ObjectType.Layout.BASIC
                )
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
            stubCloseBlock()
            stubSubscriptionEventChannel()
            stubSearchWithSubscription(
                subscription = root,
                filters = dv.content<DV>().viewers.first().filters,
                sorts = dv.content<DV>().viewers.first().sorts,
                afterId = null,
                beforeId = null,
                sources = dv.content<DV>().sources,
                keys = ObjectSearchConstants.defaultKeys + dv.content<DV>().relations.map { it.key },
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                offset = 0,
                result = SearchResult(
                    results = listOf(obj),
                    dependencies = listOf(linkedObject),
                    counter = SearchResult.Counter(
                        total = 1,
                        prev = 0,
                        next = 0
                    )
                )
            )
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
                details = details
            )

            val vm = givenViewModel()

            vm.onStart(root)

            // TESTING

            vm.currentViewer.test {
                val stateBeforeLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateBeforeLoaded)

                assertEquals(
                    expected = 0,
                    actual = stateBeforeLoaded.rows.size
                )

                assertIs<Viewer.GridView>(stateBeforeLoaded)

                val stateAfterLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateAfterLoaded)

                assertEquals(
                    expected = 1,
                    actual = stateAfterLoaded.rows.size
                )

                // Clicking on cell with linked projects.

                val testObserver = vm.navigation.test()

                vm.onGridCellClicked(stateAfterLoaded.rows.first().cells.last())

                testObserver.assertNoValue()
            }
        }

    @Test
    fun `should emit navigation command opening an object set contained in given relation if this relation is read-only`() =
        runTest {

            // SETUP

            val linkedProjectTargetId = "Linked project ID"
            val firstRecordId = "First record ID"

            val record = mapOf(
                Relations.ID to firstRecordId,
                linkedProjectRelation.key to linkedProjectTargetId
            )
            val obj = ObjectWrapper.Basic(record)
            val linkedObject = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to linkedProjectTargetId,
                    Relations.NAME to MockDataFactory.randomString(),
                    Relations.LAYOUT to ObjectType.Layout.BASIC
                )
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
            stubCloseBlock()
            stubSubscriptionEventChannel()
            stubSearchWithSubscription(
                subscription = root,
                filters = dv.content<DV>().viewers.first().filters,
                sorts = dv.content<DV>().viewers.first().sorts,
                afterId = null,
                beforeId = null,
                sources = dv.content<DV>().sources,
                keys = ObjectSearchConstants.defaultKeys + dv.content<DV>().relations.map { it.key },
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                offset = 0,
                result = SearchResult(
                    results = listOf(obj),
                    dependencies = listOf(linkedObject),
                    counter = SearchResult.Counter(
                        total = 1,
                        prev = 0,
                        next = 0
                    )
                )
            )
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
                details = details
            )

            val vm = givenViewModel()

            vm.onStart(root)

            // TESTING

            vm.currentViewer.test {
                val stateBeforeLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateBeforeLoaded)

                assertEquals(
                    expected = 0,
                    actual = stateBeforeLoaded.rows.size
                )

                assertIs<Viewer.GridView>(stateBeforeLoaded)

                val stateAfterLoaded = awaitItem()

                assertIs<Viewer.GridView>(stateAfterLoaded)

                assertEquals(
                    expected = 1,
                    actual = stateAfterLoaded.rows.size
                )

                // Clicking on cell with linked projects.

                val testObserver = vm.navigation.test()

                vm.onGridCellClicked(stateAfterLoaded.rows.first().cells.last())

                testObserver.assertValue { value ->
                    val content = value.peekContent()
                    content == AppNavigation.Command.OpenObjectSet(linkedProjectTargetId)
                }
            }
        }

    @Test
    fun `should close editor and navigate to page screen - when page is created`() {

        val id = MockDataFactory.randomUuid()
        stubCloseBlock()
        val vm = givenViewModel()


        vm.onStart(root)


        givenDelegateId(id)
        vm.onAddNewDocumentClicked()

        vm.navigation
            .test()
            .assertHasValue()
            .assertValue { value ->
                (value.peekContent() as AppNavigation.Command.OpenObject).id == id
            }
    }

    private fun givenDelegateId(id: String) {
        createNewObject.stub {
            onBlocking { execute(Unit) } doAnswer ValueClassAnswer(id)
        }
    }
}