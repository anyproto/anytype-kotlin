package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * Tests for [ObjectSetViewModel.subscribeToSyncTypeRelations] functionality.
 *
 * This function syncs type's recommended relations to DataView's relationLinks
 * when opening a TypeSet. It should only be called for TypeSet states (not Set or Collection).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetSyncTypeRelationsTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel

    // Type-related test data
    private val typeId = root // For TypeSet, the context IS the type
    private val typeUniqueKey = "typeKey-${RandomString.make()}"

    // Relation that the type recommends (e.g., "Done" checkbox)
    private val doneRelation = StubRelationObject(
        key = "done-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.CHECKBOX
    )

    // Basic relation for the DataView
    private val nameRelation = StubRelationObject(
        key = Relations.NAME,
        isReadOnlyValue = false,
        format = Relation.Format.SHORT_TEXT
    )

    private val createdDateRelation = StubRelationObject(
        key = Relations.CREATED_DATE,
        isReadOnlyValue = true,
        format = Relation.Format.DATE
    )

    // View relations
    private val dvViewerRelation =
        StubDataViewViewRelation(key = nameRelation.key, isVisible = true)

    // Relation links
    private val nameRelationLink = StubRelationLink(Relations.NAME, Relation.Format.LONG_TEXT)
    private val createdDateRelationLink =
        StubRelationLink(Relations.CREATED_DATE, RelationFormat.DATE)
    private val doneRelationLink = StubRelationLink(doneRelation.key, Relation.Format.CHECKBOX)

    // Subscription ID
    private val subscriptionId = DefaultDataViewSubscription.getDataViewSubscriptionId(root)

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `should call setDataViewProperties when TypeSet is initialized with recommended relations`() =
        runTest {
            // SETUP
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header =
                StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(dvViewerRelation)
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )

            // TypeSet details - layout is OBJECT_TYPE
            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                    )
                )
            )

            // Setup storeOfObjectTypes with the type that has recommended relations
            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                )
            )

            // Setup storeOfRelations with the done relation
            storeOfRelations.merge(listOf(doneRelation, nameRelation, createdDateRelation))

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(header, title, dataView),
                details = details
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                keys = listOf(nameRelation.key),
                sources = listOf(typeId),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY
            verify(setDataViewProperties).async(
                argThat<SetDataViewProperties.Params> { params ->
                    params.objectId == root &&
                            params.properties.containsAll(
                                listOf(
                                    Relations.NAME,
                                    Relations.DESCRIPTION,
                                    doneRelation.key
                                )
                            )
                }
            )
        }

    @Test
    fun `should not call setDataViewProperties for Set state`() = runTest {
        // SETUP
        val setOfValue = "setOf-${RandomString.make()}"
        val title = StubTitle(id = "title-${RandomString.make()}")
        val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

        val viewer = StubDataViewView(
            id = "viewer-${RandomString.make()}",
            viewerRelations = listOf(dvViewerRelation)
        )

        val dataView = StubDataView(
            id = "dv-${RandomString.make()}",
            views = listOf(viewer),
            relationLinks = listOf(nameRelationLink, createdDateRelationLink)
        )

        // Set details - layout is SET (not OBJECT_TYPE)
        val details = ObjectViewDetails(
            details = mapOf(
                root to mapOf(
                    Relations.ID to root,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                    Relations.SET_OF to listOf(setOfValue)
                ),
                setOfValue to mapOf(
                    Relations.ID to setOfValue,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                )
            )
        )

        storeOfRelations.merge(listOf(nameRelation, createdDateRelation))

        stubSpaceManager(defaultSpace)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(header, title, dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = subscriptionId,
            spaceId = defaultSpace,
            keys = listOf(nameRelation.key),
            sources = listOf(setOfValue),
            dvSorts = listOf(
                DVSort(
                    relationKey = Relations.CREATED_DATE,
                    type = Block.Content.DataView.Sort.Type.DESC,
                    relationFormat = RelationFormat.DATE,
                    includeTime = true
                )
            ),
            dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
        )
        stubTemplatesForTemplatesContainer()

        viewModel = givenViewModel()

        // TESTING
        viewModel.onStart()
        advanceUntilIdle()

        // VERIFY - setDataViewProperties should NOT be called for Set
        verify(setDataViewProperties, never()).async(any())
    }

    @Test
    fun `should not call setDataViewProperties for Collection state`() = runTest {
        // SETUP
        val title = StubTitle(id = "title-${RandomString.make()}")
        val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

        val viewer = StubDataViewView(
            id = "viewer-${RandomString.make()}",
            viewerRelations = listOf(dvViewerRelation)
        )

        val dataView = StubDataView(
            id = "dv-${RandomString.make()}",
            views = listOf(viewer),
            relationLinks = listOf(nameRelationLink, createdDateRelationLink),
            isCollection = true
        )

        // Collection details - layout is COLLECTION (not OBJECT_TYPE)
        val details = ObjectViewDetails(
            details = mapOf(
                root to mapOf(
                    Relations.ID to root,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
                )
            )
        )

        storeOfRelations.merge(listOf(nameRelation, createdDateRelation))

        stubSpaceManager(defaultSpace)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(header, title, dataView),
            details = details
        )
        stubSubscriptionResults(
            subscription = subscriptionId,
            spaceId = defaultSpace,
            collection = root,
            keys = listOf(nameRelation.key),
            dvSorts = listOf(
                DVSort(
                    relationKey = Relations.CREATED_DATE,
                    type = Block.Content.DataView.Sort.Type.DESC,
                    relationFormat = RelationFormat.DATE,
                    includeTime = true
                )
            ),
            dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
        )
        stubTemplatesForTemplatesContainer()

        viewModel = givenViewModel()

        // TESTING
        viewModel.onStart()
        advanceUntilIdle()

        // VERIFY - setDataViewProperties should NOT be called for Collection
        verify(setDataViewProperties, never()).async(any())
    }

    @Test
    fun `should not call setDataViewProperties when TypeSet is not initialized`() = runTest {
        // SETUP - No DataView block means not initialized
        val title = StubTitle(id = "title-${RandomString.make()}")
        val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

        // TypeSet details - layout is OBJECT_TYPE but NO DataView block
        val details = ObjectViewDetails(
            details = mapOf(
                root to mapOf(
                    Relations.ID to root,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                )
            )
        )

        storeOfObjectTypes.set(
            typeId,
            mapOf(
                Relations.ID to typeId,
                Relations.UNIQUE_KEY to typeUniqueKey,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
            )
        )

        storeOfRelations.merge(listOf(doneRelation, nameRelation))

        stubSpaceManager(defaultSpace)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        // Open object WITHOUT DataView block
        stubOpenObject(
            doc = listOf(header, title),  // No DataView block!
            details = details
        )
        stubTemplatesForTemplatesContainer()

        viewModel = givenViewModel()

        // TESTING
        viewModel.onStart()
        advanceUntilIdle()

        // VERIFY - setDataViewProperties should NOT be called when not initialized
        verify(setDataViewProperties, never()).async(any())
    }

    @Test
    fun `should include name description and all recommended relation keys in properties`() =
        runTest {
            // SETUP
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header =
                StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            // Additional recommended relations
            val statusRelation = StubRelationObject(
                key = "status-${RandomString.make()}",
                isReadOnlyValue = false,
                format = Relation.Format.STATUS
            )
            val priorityRelation = StubRelationObject(
                key = "priority-${RandomString.make()}",
                isReadOnlyValue = false,
                format = Relation.Format.NUMBER
            )

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(dvViewerRelation)
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )

            // TypeSet details with multiple recommended relations
            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(
                            doneRelation.id,
                            statusRelation.id,
                            priorityRelation.id
                        )
                    )
                )
            )

            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(
                        doneRelation.id,
                        statusRelation.id,
                        priorityRelation.id
                    )
                )
            )

            storeOfRelations.merge(
                listOf(
                    doneRelation,
                    statusRelation,
                    priorityRelation,
                    nameRelation,
                    createdDateRelation
                )
            )

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(header, title, dataView),
                details = details
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                keys = listOf(nameRelation.key),
                sources = listOf(typeId),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY - should include name, description, and all recommended relation keys
            verify(setDataViewProperties).async(
                argThat<SetDataViewProperties.Params> { params ->
                    params.objectId == root &&
                            params.properties.contains(Relations.NAME) &&
                            params.properties.contains(Relations.DESCRIPTION) &&
                            params.properties.contains(doneRelation.key) &&
                            params.properties.contains(statusRelation.key) &&
                            params.properties.contains(priorityRelation.key)
                }
            )
        }

    /**
     * Tests the distinctUntilChanged logic:
     * ```kotlin
     * .distinctUntilChanged { old, new ->
     *     old.isInitialized == new.isInitialized
     * }
     * ```
     * This ensures setDataViewProperties is called EXACTLY ONCE when isInitialized
     * changes from false to true, and not on subsequent state emissions.
     */
    @Test
    fun `should call setDataViewProperties exactly once when state becomes initialized`() =
        runTest {
            // SETUP
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header =
                StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(dvViewerRelation)
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )

            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                    )
                )
            )

            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                )
            )

            storeOfRelations.merge(listOf(doneRelation, nameRelation, createdDateRelation))

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(header, title, dataView),
                details = details
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                keys = listOf(nameRelation.key),
                sources = listOf(typeId),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY - should be called exactly once, not more
            verify(setDataViewProperties, times(1)).async(any())
        }

    /**
     * Tests that subsequent state updates (while state remains initialized)
     * do NOT trigger additional calls to setDataViewProperties.
     *
     * The distinctUntilChanged logic filters out emissions where
     * old.isInitialized == new.isInitialized (both true), so only the
     * first initialization triggers the sync.
     */
    @Test
    fun `should not call setDataViewProperties again when state updates while remaining initialized`() =
        runTest {
            // SETUP
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header =
                StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewerId = "viewer-${RandomString.make()}"
            val viewer = StubDataViewView(
                id = viewerId,
                viewerRelations = listOf(dvViewerRelation)
            )

            val dataViewId = "dv-${RandomString.make()}"
            val dataView = StubDataView(
                id = dataViewId,
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink, doneRelationLink)
            )

            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                    )
                )
            )

            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(doneRelation.id)
                )
            )

            storeOfRelations.merge(listOf(doneRelation, nameRelation, createdDateRelation))

            // Create a flow that emits additional events AFTER initial setup
            // These events will update the state but NOT change isInitialized
            val additionalEvents = listOf(
                Event.Command.DataView.DeleteRelation(
                    context = root,
                    dv = dataViewId,
                    keys = listOf(doneRelationLink.key)
                )
            )

            stubSpaceManager(defaultSpace)
            // Stub interceptEvents to emit additional events after initialization
            stubInterceptEvents(
                params = InterceptEvents.Params(context = root),
                flow = flow {
                    // Emit empty list first (initial)
                    emit(emptyList())
                    delay(100) // Small delay to simulate time gap
                    // Then emit events that update state but keep it initialized
                    emit(additionalEvents)
                }
            )
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(header, title, dataView),
                details = details
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                keys = listOf(nameRelation.key),
                sources = listOf(typeId),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY - should STILL be called only once despite state updates
            // The distinctUntilChanged filters out emissions where isInitialized stays true
            verify(setDataViewProperties, times(1)).async(any())
        }

    private fun stubSetDataViewProperties() {
        setDataViewProperties.stub {
            onBlocking { async(any()) }.thenReturn(Resultat.success(Payload("", emptyList())))
        }
    }
}
