package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewFilters
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

class ObjectSetNavigationTest : ObjectSetViewModelTestSetup() {

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
    private val header = StubHeader(children = listOf(title.id))

    private val linkedProjectRelation = StubRelationObject(
        key = MockDataFactory.randomString(),
        name = "Linked objects",
        format = Relation.Format.OBJECT,
        isHidden = false,
        isReadOnly = false
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
            relations = emptyList(),
            viewers = listOf(viewer),
            relationsIndex = objectRelations.map {
                RelationLink(
                    key = it.key,
                    format = it.format
                )
            }
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @ExperimentalTime
    @Test
    fun `should emit command for editing relation-object cell`() = runTest {

        // SETUP

        val linkedProjectTargetId = "Linked project ID"
        val firstRecordId = "First record ID"

        val record = mapOf(
            Relations.ID to firstRecordId,
            linkedProjectRelation.key to linkedProjectTargetId
        )

        val obj = ObjectWrapper.Basic(record)

        val setOf = listOf(MockDataFactory.randomString())

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSubscriptionEventChannel()
        stubSearchWithSubscription(
            subscription = root,
            filters = dv.content<DV>().viewers.first().filters + defaultDataViewFilters(),
            sorts = dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = setOf,
            keys = (ObjectSearchConstants.defaultDataViewKeys + dv.content<DV>().relationsIndex.map { it.key }).distinct(),
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
            dataViewRestrictions = emptyList(),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf("setOf" to setOf)
                    )
                )
            )
        )

        storeOfRelations.merge(listOf(linkedProjectRelation))

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
                        relationKey = linkedProjectRelation.key,
                        targetObjectTypes = emptyList()
                    )
                )
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Test
    fun `should emit command for editing relation-object cell if this relation is read-only and object's layout is supported`() =
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

            val setOf = listOf(MockDataFactory.randomString())

            val details = Block.Details(
                details = mapOf(
                    linkedProjectTargetId to Block.Fields(
                        mapOf(
                            Relations.ID to linkedProjectTargetId,
                            Relations.LAYOUT to supportedObjectLayouts.random().code.toDouble()
                        )
                    ),
                    root to Block.Fields(
                        mapOf("setOf" to setOf)
                    )
                )
            )

            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubCloseBlock()
            stubSubscriptionEventChannel()
            stubSearchWithSubscription(
                subscription = root,
                filters = dv.content<DV>().viewers.first().filters + defaultDataViewFilters(),
                sorts = dv.content<DV>().viewers.first().sorts,
                afterId = null,
                beforeId = null,
                sources = setOf,
                keys = (ObjectSearchConstants.defaultDataViewKeys + dv.content<DV>().relationsIndex.map { it.key }).distinct(),
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
                    dv
                ),
                dataViewRestrictions = emptyList(),
                details = details
            )

            val vm = givenViewModel()

            storeOfRelations.merge(
                listOf(
                    ObjectWrapper.Relation(
                        linkedProjectRelation.map + mapOf(
                            Relations.IS_READ_ONLY to true
                        )
                    )
                )
            )

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

                vm.commands.test {
                    vm.onGridCellClicked(stateAfterLoaded.rows.first().cells.last())
                    assertEquals(
                        awaitItem(),
                        ObjectSetCommand.Modal.EditRelationCell(
                            ctx = root,
                            dataview = dv.id,
                            target = firstRecordId,
                            viewer = viewer.id,
                            relationKey = linkedProjectRelation.key,
                            targetObjectTypes = emptyList()
                        )
                    )
                    cancelAndConsumeRemainingEvents()
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
            linkedProjectRelation.key to linkedProjectTargetId,
            Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
        )
        val obj = ObjectWrapper.Basic(record)
        val linkedObject = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to linkedProjectTargetId,
                Relations.NAME to MockDataFactory.randomString(),
                Relations.LAYOUT to ObjectType.Layout.BASIC
            )
        )

        val setOf = listOf(MockDataFactory.randomString())

        val details = Block.Details(
            details = mapOf(
                linkedProjectTargetId to Block.Fields(
                    mapOf(
                        Relations.ID to linkedProjectTargetId,
                        Relations.LAYOUT to SupportedLayouts.layouts.random().code.toDouble()
                    )
                ),
                root to Block.Fields(
                    mapOf("setOf" to setOf)
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubCloseBlock()
        stubSubscriptionEventChannel()
        stubSearchWithSubscription(
            subscription = root,
            filters = dv.content<DV>().viewers.first().filters + defaultDataViewFilters(),
            sorts = dv.content<DV>().viewers.first().sorts,
            afterId = null,
            beforeId = null,
            sources = setOf,
            keys = (ObjectSearchConstants.defaultDataViewKeys + dv.content<DV>().relationsIndex.map { it.key }).distinct(),
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
                dv
            ),
            dataViewRestrictions = emptyList(),
            details = details
        )

        val vm = givenViewModel()

        storeOfRelations.merge(
            listOf(
                ObjectWrapper.Relation(
                    linkedProjectRelation.map + mapOf(
                        Relations.IS_READ_ONLY to true
                    )
                )
            )
        )

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

            // Clicking on object's header in grid view

            vm.onObjectHeaderClicked(firstRecordId)

            verifyBlocking(closeBlock, times(1)) {
                execute(root)
            }
        }
    }

    @Test
    fun `should not emit any navigation command for opening an object if object's layout is not supported`() =
        runTest {

            // SETUP

            val unsupportedLayouis = ObjectType.Layout.values().toList() - SupportedLayouts.layouts

            val linkedProjectTargetId = "Linked project ID"
            val firstRecordId = "First record ID"

            val record = mapOf(
                Relations.ID to firstRecordId,
                linkedProjectRelation.key to linkedProjectTargetId,
                Relations.LAYOUT to ObjectType.Layout.SPACE.code
            )
            val obj = ObjectWrapper.Basic(record)
            val linkedObject = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to linkedProjectTargetId,
                    Relations.NAME to MockDataFactory.randomString(),
                    Relations.LAYOUT to ObjectType.Layout.BASIC
                )
            )

            val setOf = listOf(MockDataFactory.randomString())

            val details = Block.Details(
                details = mapOf(
                    linkedProjectTargetId to Block.Fields(
                        mapOf(
                            Relations.ID to linkedProjectTargetId,
                            Relations.LAYOUT to unsupportedLayouis.random().code.toDouble()
                        )
                    ),
                    root to Block.Fields(
                        mapOf("setOf" to setOf)
                    )
                )
            )

            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubCloseBlock()
            stubSubscriptionEventChannel()
            stubSearchWithSubscription(
                subscription = root,
                filters = dv.content<DV>().viewers.first().filters + defaultDataViewFilters(),
                sorts = dv.content<DV>().viewers.first().sorts,
                afterId = null,
                beforeId = null,
                sources = setOf,
                keys = (ObjectSearchConstants.defaultDataViewKeys + dv.content<DV>().relationsIndex.map { it.key }).distinct(),
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
                    dv
                ),
                dataViewRestrictions = emptyList(),
                details = details
            )

            val vm = givenViewModel()

            storeOfRelations.merge(
                listOf(
                    ObjectWrapper.Relation(
                        linkedProjectRelation.map + mapOf(
                            Relations.IS_READ_ONLY to true
                        )
                    )
                )
            )

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

                vm.onObjectHeaderClicked(firstRecordId)
                testObserver.assertNoValue()
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
        createObject.stub {
            onBlocking { execute(CreateObject.Param(null)) } doReturn Resultat.success(
                CreateObject.Result(
                    objectId = id,
                    event = Payload(
                        context = id,
                        events = listOf()
                    )
                )
            )
        }
    }
}