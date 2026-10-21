package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
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
import com.anytypeio.anytype.core_models.StubRelationOptionObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression test for DROID-4542: Tag/Status option chips render empty in a TypeSet's
 * embedded grid because the option objects never reach the shared object store.
 *
 * A TypeSet adds the type's recommended relations (Tag/Status) to the data view's
 * relationLinks only after the record subscription starts, so the option objects are
 * not delivered via that subscription's dependency snapshot. A dedicated options
 * subscription must fetch them and merge them into the shared [ObjectSetViewModel]'s store,
 * so `buildGridRow`'s `store.get(optionId)` can resolve the chip's name + color.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetTypeSetTagOptionsTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel

    private val typeId = root // For a TypeSet, the context IS the type
    private val typeUniqueKey = "typeKey-${RandomString.make()}"

    private val tagRelation = StubRelationObject(
        key = "tag-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.TAG
    )
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

    private val nameRelationLink = StubRelationLink(Relations.NAME, Relation.Format.LONG_TEXT)
    private val createdDateRelationLink = StubRelationLink(Relations.CREATED_DATE, RelationFormat.DATE)
    private val tagRelationLink = StubRelationLink(tagRelation.key, Relation.Format.TAG)

    private val subscriptionId = DefaultDataViewSubscription.getDataViewSubscriptionId(root)

    // Mirrors DefaultDataViewSubscription/ObjectSetViewModel's dedicated options subscription id.
    private val optionsSubscriptionId = "$root-dataview-options"

    // The option the object actually holds (named + colored), initially only reachable via the
    // dedicated options subscription, NOT via the record subscription's dependencies.
    private val tagOption = StubRelationOptionObject(
        id = "opt-${RandomString.make()}",
        space = defaultSpace,
        text = "tag3",
        color = "orange"
    )

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
    fun `type set should merge tag relation options into the store via a dedicated subscription`() =
        runTest {
            // SETUP
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = nameRelation.key, isVisible = true),
                    StubDataViewViewRelation(key = tagRelation.key, isVisible = true)
                )
            )

            // The Tag relation is already in relationLinks (models the post-sync state, where the
            // Tag column is visible but the option object is missing from the store).
            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink, tagRelationLink)
            )

            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(tagRelation.id)
                    )
                )
            )

            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(tagRelation.id)
                )
            )

            storeOfRelations.merge(listOf(tagRelation, nameRelation, createdDateRelation))

            // The object of this type holds the tag option value.
            val record = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "record-${RandomString.make()}",
                    Relations.SPACE_ID to defaultSpace,
                    Relations.TYPE to listOf(typeId),
                    tagRelation.key to listOf(tagOption.id)
                )
            )

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(doc = listOf(header, title, dataView), details = details)
            // The record subscription delivers the record but NO option dependency (the bug condition).
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                objects = listOf(record),
                dependencies = emptyList(),
                keys = listOf(tagRelation.key),
                sources = listOf(typeId),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink, tagRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            // The dedicated options subscription returns the tag option; all other storeless
            // subscriptions (e.g. board options) keep the default empty result.
            storelessSubscriptionContainer.stub {
                on {
                    subscribe(argThat<StoreSearchParams> { subscription == optionsSubscriptionId })
                } doReturn flowOf(listOf(ObjectWrapper.Basic(tagOption.map)))
            }

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY — the option object is now present in the shared store, so grid cells can
            // resolve the chip. Fails before the fix (nothing merges it into the store).
            val stored = objectStore.get(tagOption.id)
            assertNotNull(stored, "Tag option should be present in the object store")
            assertEquals("tag3", stored.name)
            assertEquals("orange", ObjectWrapper.Option(stored.map).color)
        }

    @Test
    fun `regular set still resolves tag options delivered as record subscription dependencies`() =
        runTest {
            // SETUP — a normal Set (not a TypeSet). The option arrives as a record-subscription
            // dependency (the pre-existing path); the dedicated options subscription returns nothing.
            val setOfValue = "setOf-${RandomString.make()}"
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = nameRelation.key, isVisible = true),
                    StubDataViewViewRelation(key = tagRelation.key, isVisible = true)
                )
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink, tagRelationLink)
            )

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

            storeOfRelations.merge(listOf(tagRelation, nameRelation, createdDateRelation))

            val record = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "record-${RandomString.make()}",
                    Relations.SPACE_ID to defaultSpace,
                    tagRelation.key to listOf(tagOption.id)
                )
            )

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(doc = listOf(header, title, dataView), details = details)
            // The record subscription delivers the option as a dependency (the existing path).
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                objects = listOf(record),
                dependencies = listOf(ObjectWrapper.Basic(tagOption.map)),
                keys = listOf(tagRelation.key),
                sources = listOf(setOfValue),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink, tagRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            // Dedicated options subscription returns nothing (default stub) — the option must still
            // be resolvable purely from the record-subscription dependency.

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY — the tag option is still in the store; the new subscription did not evict it.
            val stored = objectStore.get(tagOption.id)
            assertNotNull(stored, "Tag option delivered as a record dependency should stay in the store")
            assertEquals("tag3", stored.name)
        }

    @Test
    fun `type set board with no grouping property shows group-by hint instead of endless loading`() =
        runTest {
            // SETUP — a Board (Kanban) viewer with NO groupRelationKey (StubDataViewView defaults it
            // to null). Such a board can never build columns, so it must not sit on a spinner.
            val title = StubTitle(id = "title-${RandomString.make()}")
            val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                type = DVViewerType.BOARD,
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = nameRelation.key, isVisible = true)
                )
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink)
            )

            // The type page renders its own objects as a Set: the type is its own source (setOf).
            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                        Relations.UNIQUE_KEY to typeUniqueKey,
                        Relations.RECOMMENDED_RELATIONS to listOf(nameRelation.id),
                        Relations.SET_OF to listOf(typeId)
                    )
                )
            )

            storeOfObjectTypes.set(
                typeId,
                mapOf(
                    Relations.ID to typeId,
                    Relations.UNIQUE_KEY to typeUniqueKey,
                    Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                    Relations.RECOMMENDED_RELATIONS to listOf(nameRelation.id)
                )
            )
            storeOfRelations.merge(listOf(nameRelation, createdDateRelation))

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(doc = listOf(header, title, dataView), details = details)
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY — explicit "choose a grouping property" empty state, not an endless spinner.
            val state = viewModel.currentViewer.value
            assertTrue(
                state is DataViewViewState.TypeSet.NoItems && state.isBoardGroupByRequired,
                "Expected TypeSet.NoItems(isBoardGroupByRequired=true), actual: $state"
            )
        }

    @Test
    fun `collection should merge status relation options into the store via a dedicated subscription`() =
        runTest {
            // SETUP — a Collection (DROID-4545). A Status property is visible and lives in the
            // data view's relationLinks, but its option object is NOT delivered as a record
            // subscription dependency, so the Status cell would render empty ("takes up space"
            // but blank) without the dedicated options subscription. The store-merge under test
            // is viewer-agnostic (it feeds Grid, Gallery and List alike), so we assert it on the
            // default Grid viewer like the sibling Set/TypeSet cases.
            val statusRelation = StubRelationObject(
                key = "status-${RandomString.make()}",
                isReadOnlyValue = false,
                format = Relation.Format.STATUS
            )
            val statusRelationLink = StubRelationLink(statusRelation.key, Relation.Format.STATUS)
            val statusOption = StubRelationOptionObject(
                id = "opt-${RandomString.make()}",
                space = defaultSpace,
                text = "In progress",
                color = "red"
            )

            val title = StubTitle(id = "title-${RandomString.make()}")
            val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

            val viewer = StubDataViewView(
                id = "viewer-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = nameRelation.key, isVisible = true),
                    StubDataViewViewRelation(key = statusRelation.key, isVisible = true)
                )
            )

            val dataView = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer),
                relationLinks = listOf(nameRelationLink, createdDateRelationLink, statusRelationLink),
                isCollection = true
            )

            val details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
                    )
                )
            )

            storeOfRelations.merge(listOf(statusRelation, nameRelation, createdDateRelation))

            val record = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "record-${RandomString.make()}",
                    Relations.SPACE_ID to defaultSpace,
                    statusRelation.key to listOf(statusOption.id)
                )
            )

            stubSpaceManager(defaultSpace)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(doc = listOf(header, title, dataView), details = details)
            // Collection subscription (collection = ctx, no sources); the option is NOT a dependency.
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = defaultSpace,
                collection = root,
                objects = listOf(record),
                dependencies = emptyList(),
                keys = listOf(statusRelation.key),
                sources = emptyList(),
                dvSorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                ),
                dvRelationLinks = listOf(nameRelationLink, createdDateRelationLink, statusRelationLink)
            )
            stubTemplatesForTemplatesContainer()
            stubSetDataViewProperties()

            // The dedicated options subscription returns the status option.
            storelessSubscriptionContainer.stub {
                on {
                    subscribe(argThat<StoreSearchParams> { subscription == optionsSubscriptionId })
                } doReturn flowOf(listOf(ObjectWrapper.Basic(statusOption.map)))
            }

            viewModel = givenViewModel()

            // TESTING
            viewModel.onStart()
            advanceUntilIdle()

            // VERIFY — the option object reaches the shared store so a Collection's Status cell
            // can resolve the chip's name + color. Fails if the options merge is skipped for Collections.
            val stored = objectStore.get(statusOption.id)
            assertNotNull(stored, "Status option should be present in the object store for a Collection")
            assertEquals("In progress", stored.name)
            assertEquals("red", ObjectWrapper.Option(stored.map).color)
        }

    private fun stubSetDataViewProperties() {
        setDataViewProperties.stub {
            onBlocking { async(any()) }.thenReturn(Resultat.success(Payload("", emptyList())))
        }
    }
}
