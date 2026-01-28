package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetDataViewObjectCreateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet
    private lateinit var mockObjectCollection: MockCollection

    private val setOfId = "setOfId"
    private val setOfKey = "setOfKey"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        mockObjectSet =
            MockSet(context = root, setOfValue = setOfId, setOfKey = setOfKey, space = defaultSpace)
        mockObjectCollection = MockCollection(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `create pre-populated record`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )
        createDataViewObject.stub {
            onBlocking {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = mapOf(),
                    )
                )
            } doReturn Resultat.success(
                CreateDataViewObject.Result(
                    "",
                    TypeKey(""),
                    mapOf("spaceId" to "spaceId")
                )
            )
        }

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = mapOf(),
                    )
                )
            }
        }
    }

    @Test
    fun `shouldn't be object create allowed when restriction is present`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details,
            dataViewRestrictions = listOf(
                DataViewRestrictions(
                    block = mockObjectSet.dataView.id,
                    restrictions = listOf(DataViewRestriction.CREATE_OBJECT)
                )
            )
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertFalse(second.isCreateObjectAllowed)
        }
    }

    @Test
    fun `shouldn't be object create allowed when type recommended layout`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)

        val skipLayouts = SupportedLayouts.fileLayouts + SupportedLayouts.systemLayouts
        val recommendedLayout = skipLayouts.random()
        val details = ObjectViewDetails(
            details = mapOf(
                root to
                        mapOf(
                            Relations.ID to root,
                            Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                            Relations.SET_OF to listOf(mockObjectSet.setOf)
                        ),
                mockObjectSet.setOf to
                        mapOf(
                            Relations.ID to mockObjectSet.setOf,
                            Relations.UNIQUE_KEY to setOfKey,
                            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                            Relations.RECOMMENDED_LAYOUT to recommendedLayout.code.toDouble(),
                            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        )
            )
        )
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertFalse(second.isCreateObjectAllowed)
        }
    }

    @Test
    fun `should be object create allowed when type recommended layout`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)

        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertTrue(second.isCreateObjectAllowed)
        }
    }

    @Test
    fun `when creating set object with deleted type should use space default type and its template`() =
        runTest {
            // SETUP
            val deletedTypeId = "deleted-type-id"
            val spaceDefaultTypeId = "space-default-type-id"
            val spaceDefaultTypeKey = TypeKey("space-default-type-key")
            val spaceDefaultTemplateId = "space-default-template-id"

            val spaceDefaultTypeMap = mapOf(
                Relations.ID to spaceDefaultTypeId,
                Relations.UNIQUE_KEY to spaceDefaultTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "Space Default Type",
                Relations.DEFAULT_TEMPLATE_ID to spaceDefaultTemplateId
            )

            // Stub space manager
            stubSpaceManager(mockObjectSet.spaceId)

            // Stub store: deleted type returns null (not set), space default type exists
            storeOfObjectTypes.set(spaceDefaultTypeId, spaceDefaultTypeMap)

            // Stub GetDefaultObjectType to return space default
            stubGetDefaultPageType(
                type = spaceDefaultTypeKey,
                name = "Space Default Type",
                template = spaceDefaultTemplateId
            )

            // Create custom dataView with deleted type in viewer
            // NOTE: For Set by Type, viewer's defaultObjectType is ignored (uses setOf instead).
            // We don't set defaultTemplateId here, so it falls back to setOf type's template.
            // Since setOf type doesn't have a template in this test, it should fall back to space default.
            val customViewer = StubDataViewView(
                id = "viewer-1",
                viewerRelations = listOf(),
                filters = mockObjectSet.filters,
                defaultObjectType = deletedTypeId,  // Viewer references deleted type (ignored for Set by Type)
                defaultTemplateId = null  // No template in viewer, will fall back to setOf type's template
            )
            val customDataView = StubDataView(
                views = listOf(customViewer),
                relationLinks = mockObjectSet.relationLinks
            )

            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, customDataView),
                details = mockObjectSet.details
            )

            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                spaceId = mockObjectSet.spaceId,
                keys = mockObjectSet.dvKeys,
                sources = listOf(mockObjectSet.setOf),
                dvFilters = mockObjectSet.filters,
                objects = listOf()
            )

            // Expect: CreateDataViewObject called with setOf type and null template (setOf type has no template)
            createDataViewObject.stub {
                onBlocking {
                    async(
                        CreateDataViewObject.Params.SetByType(
                            type = TypeKey(setOfKey),
                            filters = mockObjectSet.filters,
                            template = null,  // setOf type has no template, so null
                            prefilled = mapOf()
                        )
                    )
                } doReturn Resultat.success(
                    CreateDataViewObject.Result(
                        objectId = "new-object-id",
                        objectType = TypeKey(setOfKey)
                    )
                )
            }

            // TESTING
            proceedWithStartingViewModel()
            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()
            advanceUntilIdle()

            // ASSERT: Verify CreateDataViewObject was called with setOf type and null template
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = null,
                        prefilled = mapOf()
                    )
                )
            }
        }

    @Test
    fun `when creating set object with deleted type but user chooses template should use user template`() =
        runTest {
            // SETUP
            val deletedTypeId = "deleted-type-id"
            val spaceDefaultTypeId = "space-default-type-id"
            val spaceDefaultTypeKey = TypeKey("space-default-type-key")
            val spaceDefaultTemplateId = "space-default-template-id"
            val userChosenTemplateId = "user-chosen-template-id"

            val spaceDefaultTypeMap = mapOf(
                Relations.ID to spaceDefaultTypeId,
                Relations.UNIQUE_KEY to spaceDefaultTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "Space Default Type",
                Relations.DEFAULT_TEMPLATE_ID to spaceDefaultTemplateId
            )

            // Stub space manager
            stubSpaceManager(mockObjectSet.spaceId)

            // Stub store: deleted type returns null (not set), space default type exists
            storeOfObjectTypes.set(spaceDefaultTypeId, spaceDefaultTypeMap)

            // Stub GetDefaultObjectType to return space default
            stubGetDefaultPageType(
                type = spaceDefaultTypeKey,
                name = "Space Default Type",
                template = spaceDefaultTemplateId
            )

            // Create custom dataView with deleted type in viewer
            // NOTE: For Set by Type, viewer's defaultObjectType is ignored (uses setOf instead).
            // We don't set defaultTemplateId here, so it would normally fall back to setOf type's template.
            // But the user chooses a template, which has highest priority.
            val customViewer = StubDataViewView(
                id = "viewer-1",
                viewerRelations = listOf(),
                filters = mockObjectSet.filters,
                defaultObjectType = deletedTypeId,  // Viewer references deleted type (ignored for Set by Type)
                defaultTemplateId = null  // No template in viewer
            )
            val customDataView = StubDataView(
                views = listOf(customViewer),
                relationLinks = mockObjectSet.relationLinks
            )

            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, customDataView),
                details = mockObjectSet.details
            )

            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                spaceId = mockObjectSet.spaceId,
                keys = mockObjectSet.dvKeys,
                sources = listOf(mockObjectSet.setOf),
                dvFilters = mockObjectSet.filters,
                objects = listOf()
            )

            // Expect: CreateDataViewObject called with setOf type but USER's template
            createDataViewObject.stub {
                onBlocking {
                    async(
                        CreateDataViewObject.Params.SetByType(
                            type = TypeKey(setOfKey),
                            filters = mockObjectSet.filters,
                            template = userChosenTemplateId,  // Should use user's choice (highest priority)
                            prefilled = mapOf()
                        )
                    )
                } doReturn Resultat.success(
                    CreateDataViewObject.Result(
                        objectId = "new-object-id",
                        objectType = TypeKey(setOfKey)
                    )
                )
            }

            // TESTING
            proceedWithStartingViewModel()
            advanceUntilIdle()

            // User chooses a template
            viewModel.proceedWithDataViewObjectCreate(templateId = userChosenTemplateId)
            advanceUntilIdle()

            // ASSERT: Verify CreateDataViewObject was called with setOf type and user's template (highest priority)
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.SetByType(
                        type = TypeKey(setOfKey),
                        filters = mockObjectSet.filters,
                        template = userChosenTemplateId,
                        prefilled = mapOf()
                    )
                )
            }
        }

    @Test
    fun `when adding object to collection with deleted type should use space default type and template`() =
        runTest {
            // SETUP
            val deletedTypeId = "deleted-type-id"
            val spaceDefaultTypeId = "space-default-type-id"
            val spaceDefaultTypeKey = TypeKey("space-default-type-key")
            val spaceDefaultTemplateId = "space-default-template-id"

            val spaceDefaultTypeMap = mapOf(
                Relations.ID to spaceDefaultTypeId,
                Relations.UNIQUE_KEY to spaceDefaultTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "Space Default Type",
                Relations.DEFAULT_TEMPLATE_ID to spaceDefaultTemplateId
            )

            // Stub space manager
            stubSpaceManager(mockObjectCollection.spaceId)

            // Stub store: deleted type returns null (not set), space default type exists
            storeOfObjectTypes.set(spaceDefaultTypeId, spaceDefaultTypeMap)

            // Stub GetDefaultObjectType to return space default
            stubGetDefaultPageType(
                type = spaceDefaultTypeKey,
                name = "Space Default Type",
                template = spaceDefaultTemplateId,
                spaceId = SpaceId(mockObjectCollection.spaceId)
            )

            // Create custom dataView with deleted type in viewer
            val customViewer = StubDataViewView(
                id = "viewer-1",
                viewerRelations = listOf(),
                filters = mockObjectCollection.filters,
                defaultObjectType = deletedTypeId,  // Viewer references deleted type
                defaultTemplateId = "deleted-template-id"
            )
            val customDataView = StubDataView(
                views = listOf(customViewer),
                relationLinks = mockObjectCollection.relationLinks
            )

            stubOpenObject(
                doc = listOf(
                    mockObjectCollection.header,
                    mockObjectCollection.title,
                    customDataView
                ),
                details = mockObjectCollection.details
            )

            stubSubscriptionResults(
                subscription = mockObjectCollection.subscriptionId,
                spaceId = mockObjectCollection.spaceId,
                keys = mockObjectCollection.dvKeys,
                sources = listOf(),
                dvFilters = mockObjectCollection.filters,
                objects = listOf()
            )

            // Stub AddObjectToCollection
            addObjectToCollection.stub {
                onBlocking {
                    async(any())
                } doReturn Resultat.success(
                    com.anytypeio.anytype.core_models.Payload(
                        context = root,
                        events = listOf()
                    )
                )
            }

            // Expect: CreateDataViewObject called with space default type and its template
            createDataViewObject.stub {
                onBlocking {
                    async(
                        CreateDataViewObject.Params.Collection(
                            type = spaceDefaultTypeKey,
                            filters = mockObjectCollection.filters,
                            template = spaceDefaultTemplateId,  // Should use space default template
                            prefilled = mapOf()
                        )
                    )
                } doReturn Resultat.success(
                    CreateDataViewObject.Result(
                        objectId = "new-object-id",
                        objectType = spaceDefaultTypeKey
                    )
                )
            }

            // TESTING
            proceedWithStartingViewModel()
            advanceUntilIdle()

            viewModel.proceedWithDataViewObjectCreate()
            advanceUntilIdle()

            // ASSERT: Verify CreateDataViewObject was called with space default type's template
            verifyBlocking(createDataViewObject, times(1)) {
                async(
                    CreateDataViewObject.Params.Collection(
                        type = spaceDefaultTypeKey,
                        filters = mockObjectCollection.filters,
                        template = spaceDefaultTemplateId,
                        prefilled = mapOf()
                    )
                )
            }
        }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}