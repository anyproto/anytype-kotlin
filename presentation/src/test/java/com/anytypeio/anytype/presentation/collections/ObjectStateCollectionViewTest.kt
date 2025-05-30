package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.SetOrCollectionHeaderState
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectStateCollectionViewTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectCollection: MockCollection

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        mockObjectCollection = MockCollection(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should display init state when opening object without DataView block`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(mockObjectCollection.header, mockObjectCollection.title),
                details = mockObjectCollection.details
            )

            // TESTING
            proceedWithStartingViewModel()

            val headerFlow = viewModel.header.testIn(backgroundScope)
            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            assertIs<SetOrCollectionHeaderState.None>(headerFlow.awaitItem())
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

            assertEquals(
                expected = mockObjectCollection.details.details[mockObjectCollection.root]?.getSingleValue(
                    Relations.NAME
                ),
                actual = (headerFlow.awaitItem() as SetOrCollectionHeaderState.Default).title.text
            )
            viewerFlow.expectNoEvents()
        }
    }

    @Test
    fun `should display collection no view state when opening object without view`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewEmpty
                ),
                details = mockObjectCollection.details
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Collection.NoView>(viewerFlow.awaitItem())

            viewerFlow.expectNoEvents()
        }
    }


    @Test
    fun `should display collection no items state when opening object without records`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataView
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                dvSorts = mockObjectCollection.sorts
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Collection.NoItems>(viewerFlow.awaitItem())
        }
    }

    @Test
    fun `should display collection default state when opening object with records`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataView
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(mockObjectCollection.obj1, mockObjectCollection.obj2),
                dvSorts = mockObjectCollection.sorts
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())
            //assertIs<DataViewViewState.Collection.NoItems>(viewerFlow.awaitItem())
            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem())
        }
    }

    @Test
    fun `should sort LIST collection objects by data view object order`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewWithObjectOrder
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj1,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj5
                ),
                dvSorts = mockObjectCollection.sorts
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.ListView).items
                assertEquals(5, rows.size)

                // SHOULD BE 3, 2, 4, 5, 1
                assertEquals(mockObjectCollection.obj3.id, rows[0].objectId)
                assertEquals(mockObjectCollection.obj2.id, rows[1].objectId)
                assertEquals(mockObjectCollection.obj4.id, rows[2].objectId)
                assertEquals(mockObjectCollection.obj5.id, rows[3].objectId)
                assertEquals(mockObjectCollection.obj1.id, rows[4].objectId)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not sort LIST collection objects by empty data view object order`() = runTest {
        turbineScope {
            // SETUP
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataView
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj5,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj1
                ),
                dvSorts = mockObjectCollection.sorts
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.ListView).items
                assertEquals(5, rows.size)

                // SHOULD BE NOT SORTED 5, 2, 3, 4, 1
                assertEquals(mockObjectCollection.obj5.id, rows[0].objectId)
                assertEquals(mockObjectCollection.obj2.id, rows[1].objectId)
                assertEquals(mockObjectCollection.obj3.id, rows[2].objectId)
                assertEquals(mockObjectCollection.obj4.id, rows[3].objectId)
                assertEquals(mockObjectCollection.obj1.id, rows[4].objectId)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should sort GRID collection objects by data view object order`() = runTest {
        turbineScope {
            // SETUP
            session.currentViewerId.value = mockObjectCollection.viewerGrid.id
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewWithObjectOrder
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj1,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj5
                ),
                dvSorts = listOf(mockObjectCollection.sortGrid)
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            //assertIs<DataViewViewState.Collection.NoItems>(viewerFlow.awaitItem())
            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.GridView).rows
                assertEquals(5, rows.size)

                // SHOULD BE SORTED 1, 2, 4, 3, 5
                assertEquals(mockObjectCollection.obj1.id, rows[0].id)
                assertEquals(mockObjectCollection.obj2.id, rows[1].id)
                assertEquals(mockObjectCollection.obj4.id, rows[2].id)
                assertEquals(mockObjectCollection.obj3.id, rows[3].id)
                assertEquals(mockObjectCollection.obj5.id, rows[4].id)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should NOT sort GRID collection objects by empty data view object order`() = runTest {
        turbineScope {
            // SETUP
            session.currentViewerId.value = mockObjectCollection.viewerGrid.id
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewGrid
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj1,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj5
                ),
                dvSorts = listOf(mockObjectCollection.sortGrid)
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.GridView).rows
                assertEquals(5, rows.size)

                // SHOULD BE SORTED 1, 2, 4, 3, 5
                assertEquals(mockObjectCollection.obj1.id, rows[0].id)
                assertEquals(mockObjectCollection.obj2.id, rows[1].id)
                assertEquals(mockObjectCollection.obj3.id, rows[2].id)
                assertEquals(mockObjectCollection.obj4.id, rows[3].id)
                assertEquals(mockObjectCollection.obj5.id, rows[4].id)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should sort GALLERY collection objects by data view object order`() = runTest {
        turbineScope {
            // SETUP
            session.currentViewerId.value = mockObjectCollection.viewerGallery.id
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewWithObjectOrder
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj1,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj5
                ),
                dvSorts = listOf(mockObjectCollection.sortGallery)
            )

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.GalleryView).items
                assertEquals(5, rows.size)

                // SHOULD BE SORTED 5, 4, 3, 2, 1
                assertEquals(mockObjectCollection.obj5.id, rows[0].objectId)
                assertEquals(mockObjectCollection.obj4.id, rows[1].objectId)
                assertEquals(mockObjectCollection.obj3.id, rows[2].objectId)
                assertEquals(mockObjectCollection.obj2.id, rows[3].objectId)
                assertEquals(mockObjectCollection.obj1.id, rows[4].objectId)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should NOT sort GALLERY collection objects by empty data view object order`() = runTest {
        turbineScope {
            // SETUP
            session.currentViewerId.value = mockObjectCollection.viewerGallery.id
            stubSpaceManager(mockObjectCollection.spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    mockObjectCollection.dataViewGallery
                ),
                details = mockObjectCollection.details
            )
            stubStoreOfRelations(mockObjectCollection)
            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                collection = root,
                storeOfRelations = storeOfRelations,
                keys = mockObjectCollection.dvKeys,
                objects = listOf(
                    mockObjectCollection.obj1,
                    mockObjectCollection.obj2,
                    mockObjectCollection.obj3,
                    mockObjectCollection.obj4,
                    mockObjectCollection.obj5
                ),
                dvSorts = listOf(mockObjectCollection.sortGallery)
            )
            stubTemplatesForTemplatesContainer()

            // TESTING
            proceedWithStartingViewModel()

            val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // ASSERT STATES
            assertIs<ObjectState.Init>(stateFlow.awaitItem())
            assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())

            assertIs<DataViewViewState.Collection.Default>(viewerFlow.awaitItem()).also { dataViewState ->
                val rows = (dataViewState.viewer as Viewer.GalleryView).items
                assertEquals(5, rows.size)

                // SHOULD BE SORTED 5, 4, 3, 2, 1
                assertEquals(mockObjectCollection.obj1.id, rows[0].objectId)
                assertEquals(mockObjectCollection.obj2.id, rows[1].objectId)
                assertEquals(mockObjectCollection.obj3.id, rows[2].objectId)
                assertEquals(mockObjectCollection.obj4.id, rows[3].objectId)
                assertEquals(mockObjectCollection.obj5.id, rows[4].objectId)
            }
            stateFlow.cancelAndIgnoreRemainingEvents()
            viewerFlow.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should be collection with templates present when active view hasn't defaultTemplateId`() =
        runTest {
            turbineScope {
                // SETUP

                val defaultObjectType = TypeKey(MockDataFactory.randomString())
                val defaultObjectTypeName = "CustomName"

                stubSpaceManager(mockObjectCollection.spaceId)
                stubInterceptEvents()
                stubInterceptThreadStatus()
                stubTemplatesForTemplatesContainer(
                    type = VIEW_DEFAULT_OBJECT_TYPE,
                    templates = listOf(StubObject(objectType = VIEW_DEFAULT_OBJECT_TYPE))
                )

                session.currentViewerId.value = mockObjectCollection.viewerList.id
                val dataview = mockObjectCollection.dataView.copy(
                    content = (mockObjectCollection.dataView.content as DV).copy(
                        viewers = listOf(
                            mockObjectCollection.viewerGrid.copy(defaultObjectType = ObjectTypeIds.PROFILE),
                            mockObjectCollection.viewerList.copy(defaultObjectType = ObjectTypeIds.PAGE)
                        )
                    )
                )

                stubOpenObject(
                    doc = listOf(
                        mockObjectCollection.header,
                        mockObjectCollection.title,
                        dataview
                    ),
                    details = mockObjectCollection.details
                )
                stubStoreOfObjectTypes(
                    VIEW_DEFAULT_OBJECT_TYPE,
                    mapOf(
                        Relations.ID to VIEW_DEFAULT_OBJECT_TYPE,
                        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.NAME to "VIEW_DEFAULT_OBJECT_TYPE"
                    )
                )
                stubStoreOfRelations(mockObjectCollection)
                stubSubscriptionResults(
                    subscription = mockObjectCollection.subscriptionId,
                    spaceId = mockObjectCollection.spaceId,
                    collection = root,
                    storeOfRelations = storeOfRelations,
                    keys = mockObjectCollection.dvKeys,
                    dvSorts = mockObjectCollection.sorts
                )

                // TESTING
                proceedWithStartingViewModel()

                val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
                val stateFlow = stateReducer.state.testIn(backgroundScope)

                // ASSERT STATES
                assertIs<ObjectState.Init>(stateFlow.awaitItem())
                assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())
                assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

                val item = viewerFlow.awaitItem()
                assertIs<DataViewViewState.Collection.NoItems>(item)
                assertTrue(item.isCreateObjectAllowed)
            }
        }

    @Test
    fun `should be collection with object create templates allowed when active viewer default type is NOTE`() =
        runTest {
            turbineScope {
                // SETUP

                val noteTypeId = MockDataFactory.randomString()
                val noteTypeUniqueKey = TypeKey(ObjectTypeIds.NOTE)
                val noteTypeName = "Note"

                stubSpaceManager(mockObjectCollection.spaceId)
                stubInterceptEvents()
                stubInterceptThreadStatus()
                stubTemplatesForTemplatesContainer()

                session.currentViewerId.value = mockObjectCollection.viewerList.id
                val dataview = mockObjectCollection.dataView.copy(
                    content = (mockObjectCollection.dataView.content as DV).copy(
                        viewers = listOf(
                            mockObjectCollection.viewerGrid.copy(defaultObjectType = RandomString.make()),
                            mockObjectCollection.viewerList.copy(defaultObjectType = noteTypeId)
                        )
                    )
                )

                stubOpenObject(
                    doc = listOf(
                        mockObjectCollection.header,
                        mockObjectCollection.title,
                        dataview
                    ),
                    details = mockObjectCollection.details
                )
                stubStoreOfObjectTypes(
                    noteTypeId,
                    mapOf(
                        Relations.ID to noteTypeId,
                        Relations.UNIQUE_KEY to noteTypeUniqueKey.key,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                        Relations.NAME to noteTypeName
                    )
                )
                stubStoreOfRelations(mockObjectCollection)
                stubSubscriptionResults(
                    subscription = mockObjectCollection.subscriptionId,
                    spaceId = mockObjectCollection.spaceId,
                    collection = root,
                    storeOfRelations = storeOfRelations,
                    keys = mockObjectCollection.dvKeys,
                    dvSorts = mockObjectCollection.sorts
                )

                // TESTING
                proceedWithStartingViewModel()

                val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
                val stateFlow = stateReducer.state.testIn(backgroundScope)

                // ASSERT STATES
                assertIs<ObjectState.Init>(stateFlow.awaitItem())
                assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())
                assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

                val item = viewerFlow.awaitItem()
                assertIs<DataViewViewState.Collection.NoItems>(item)
                assertTrue(item.isCreateObjectAllowed)
            }
        }

    @Test
    fun `should be collection without object create allowed when restriction is present`() =
        runTest {
            turbineScope {
                // SETUP

                val defaultObjectType = RandomString.make()
                val defaultObjectTypeName = RandomString.make()

                stubSpaceManager(mockObjectCollection.spaceId)
                stubInterceptEvents()
                stubInterceptThreadStatus()

                session.currentViewerId.value = mockObjectCollection.viewerList.id
                val dataview = mockObjectCollection.dataView.copy(
                    content = (mockObjectCollection.dataView.content as DV).copy(
                        viewers = listOf(
                            mockObjectCollection.viewerGrid.copy(defaultObjectType = ObjectTypeIds.PROFILE),
                            mockObjectCollection.viewerList.copy(defaultObjectType = defaultObjectType)
                        )
                    )
                )

                stubOpenObject(
                    doc = listOf(
                        mockObjectCollection.header,
                        mockObjectCollection.title,
                        dataview
                    ),
                    details = mockObjectCollection.details,
                    dataViewRestrictions = listOf(
                        DataViewRestrictions(
                            block = dataview.id,
                            restrictions = listOf(DataViewRestriction.CREATE_OBJECT)
                        )
                    )
                )
                stubStoreOfObjectTypes(
                    defaultObjectType,
                    mapOf(
                        Relations.ID to defaultObjectType,
                        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble(),
                        Relations.NAME to defaultObjectTypeName
                    )
                )
                stubStoreOfRelations(mockObjectCollection)
                stubSubscriptionResults(
                    subscription = mockObjectCollection.subscriptionId,
                    spaceId = mockObjectCollection.spaceId,
                    collection = root,
                    storeOfRelations = storeOfRelations,
                    keys = mockObjectCollection.dvKeys,
                    dvSorts = mockObjectCollection.sorts
                )

                // TESTING
                proceedWithStartingViewModel()

                val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
                val stateFlow = stateReducer.state.testIn(backgroundScope)

                // ASSERT STATES
                assertIs<ObjectState.Init>(stateFlow.awaitItem())
                assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())
                assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

                val item = viewerFlow.awaitItem()
                assertIs<DataViewViewState.Collection.NoItems>(item)
                assertFalse(item.isCreateObjectAllowed)
            }
        }

    @Test
    fun `should be collection with object create allowed when active viewer default type is custom type with recommended layouts`() =
        runTest {
            turbineScope {
                // SETUP

                val defaultObjectTypeId = RandomString.make()
                val defaultObjectTypeUniqueKey = TypeKey(ObjectTypeIds.PAGE)
                val defaultObjectTypeName = "Page"

                stubSpaceManager(mockObjectCollection.spaceId)
                stubInterceptEvents()
                stubInterceptThreadStatus()

                session.currentViewerId.value = mockObjectCollection.viewerList.id
                val dataview = mockObjectCollection.dataView.copy(
                    content = (mockObjectCollection.dataView.content as DV).copy(
                        viewers = listOf(
                            mockObjectCollection.viewerGrid.copy(defaultObjectType = ObjectTypeIds.NOTE),
                            mockObjectCollection.viewerList.copy(defaultObjectType = defaultObjectTypeId)
                        )
                    )
                )

                stubOpenObject(
                    doc = listOf(
                        mockObjectCollection.header,
                        mockObjectCollection.title,
                        dataview
                    ),
                    details = mockObjectCollection.details
                )
                stubStoreOfObjectTypes(
                    defaultObjectTypeId,
                    mapOf(
                        Relations.ID to defaultObjectTypeId,
                        Relations.UNIQUE_KEY to defaultObjectTypeUniqueKey.key,
                        Relations.RECOMMENDED_LAYOUT to SupportedLayouts.editorLayouts.random().code.toDouble(),
                        Relations.NAME to defaultObjectTypeName
                    )
                )
                stubStoreOfRelations(mockObjectCollection)
                stubSubscriptionResults(
                    subscription = mockObjectCollection.subscriptionId,
                    spaceId = mockObjectCollection.spaceId,
                    collection = root,
                    storeOfRelations = storeOfRelations,
                    keys = mockObjectCollection.dvKeys,
                    dvSorts = mockObjectCollection.sorts
                )

                // TESTING
                proceedWithStartingViewModel()

                val viewerFlow = viewModel.currentViewer.testIn(backgroundScope)
                val stateFlow = stateReducer.state.testIn(backgroundScope)

                // ASSERT STATES
                assertIs<ObjectState.Init>(stateFlow.awaitItem())
                assertIs<DataViewViewState.Init>(viewerFlow.awaitItem())
                assertIs<ObjectState.DataView.Collection>(stateFlow.awaitItem())

                val item = viewerFlow.awaitItem()
                assertIs<DataViewViewState.Collection.NoItems>(item)
                assertTrue(item.isCreateObjectAllowed)
            }
        }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}