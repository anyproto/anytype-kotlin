package com.anytypeio.anytype.presentation.collections

import android.util.Log
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Relations.LAYOUT
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionAddRelationTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var objectCollection: MockCollection

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        objectCollection = MockCollection(context = root, space = defaultSpace)
        viewModel = givenViewModel()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    private val COLLECTION_LAYOUT = ObjectType.Layout.COLLECTION.code.toDouble()

    @Test
    fun `should add new relation to data view`() = runTest {
        // SETUP
        stubSpaceManager(space = objectCollection.spaceId)
        stubStoreOfRelations(objectCollection)
        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            spaceId = objectCollection.spaceId,
            keys = objectCollection.dvKeys,
            objects = listOf(objectCollection.obj1, objectCollection.obj2),
            dvSorts = objectCollection.sorts
        )
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val objectViewDetails = ObjectViewDetails(
            details = mapOf(
                root to mapOf(
                    Relations.ID to root,
                    Relations.NAME to objectCollection.details.getObject(root)?.name!!,
                    LAYOUT to COLLECTION_LAYOUT
                )
            )
        )

        stubOpenObject(
            doc = listOf(
                objectCollection.header,
                objectCollection.title,
                objectCollection.dataView
            ),
            details = objectViewDetails
        )

        // TESTING
        proceedWithStartingViewModel()

        val relationId4 = "rel-newRelationKey"
        val relationKey4 = "newRelationKey"
        val relationName4 = "NEW RELATION"
        val relationObject4 = StubRelationObject(
            id = relationId4,
            key = relationKey4,
            name = relationName4,
            spaceId = objectCollection.spaceId
        )
        val relationLink4 = RelationLink(relationKey4, relationObject4.format)
        val dvViewerRelation4 = DVViewerRelation(
            key = relationKey4,
            isVisible = true
        )

        viewModel.currentViewer.test {

            val initState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Init,
                actual = initState
            )

            val gridState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Collection.Default(
                    isCreateObjectAllowed = true,
                    viewer = Viewer.ListView(
                        id = objectCollection.viewerList.id,
                        title = objectCollection.viewerList.name,
                        items = listOf(
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj1.id,
                                name = objectCollection.obj1.name.orEmpty(),
                                description = objectCollection.obj1.description,
                                hideIcon = false,
                                icon = ObjectIcon.TypeIcon.Fallback.DEFAULT,
                                relations = listOf(
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject1.key
                                    ),
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject2.key
                                    ),
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject3.key
                                    )
                                )
                            ),
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj2.id,
                                name = objectCollection.obj2.name.orEmpty(),
                                description = objectCollection.obj2.description,
                                hideIcon = false,
                                icon = ObjectIcon.TypeIcon.Fallback.DEFAULT,
                                relations = listOf(
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject1.key
                                    ),
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject2.key
                                    ),
                                    DefaultObjectRelationValueView.Empty(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject3.key
                                    )
                                )
                            )
                        )
                    )
                ),
                actual = gridState
            )

            expectNoEvents()
        }

        //check new state with new relation object
        stateReducer.state.test {

            val first = awaitItem()

            assertEquals(
                expected = ObjectState.DataView.Collection(
                    root = root,
                    blocks = listOf(
                        objectCollection.header,
                        objectCollection.title,
                        objectCollection.dataView
                    ),
                    details = objectViewDetails,
                    objectRestrictions = listOf(),
                    dataViewRestrictions = listOf()
                ),
                actual = first
            )

            //Add new relation from marketplace to object and view
            val eventObjectDetailsSet = Event.Command.Details.Amend(
                context = root,
                target = root,
                details = mapOf(relationKey4 to "")
            )

            val eventDataViewRelationSet = Event.Command.DataView.SetRelation(
                context = root,
                dv = objectCollection.dataView.id,
                links = listOf(relationLink4)
            )

            val eventDataViewUpdateView = Event.Command.DataView.UpdateView(
                context = root,
                block = objectCollection.dataView.id,
                viewerId = objectCollection.viewerList.id,
                relationUpdates = listOf(
                    Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Add(
                        afterId = objectCollection.relationObject2.id,
                        relations = listOf(dvViewerRelation4)
                    )
                ),
                fields = null,
                filterUpdates = listOf(),
                sortUpdates = listOf()
            )
            stubSubscriptionResults(
                subscription = objectCollection.subscriptionId,
                collection = root,
                spaceId = objectCollection.spaceId,
                keys = objectCollection.dvKeys + relationObject4.key,
                objects = listOf(objectCollection.obj1, objectCollection.obj2),
                dvSorts = objectCollection.sorts
            )
            dispatcher.send(
                Payload(
                    context = root,
                    events = listOf(
                        eventObjectDetailsSet,
                        eventDataViewRelationSet,
                        eventDataViewUpdateView
                    )
                )
            )

            val second = awaitItem()

            val expectedView = objectCollection.viewerList.copy(
                viewerRelations = listOf(
                    objectCollection.dvViewerRelation1,
                    objectCollection.dvViewerRelation2,
                    objectCollection.dvViewerRelation3,
                    dvViewerRelation4
                )
            )

            val dataViewContent = objectCollection.dataView.content as Block.Content.DataView
            val expectedDataView = objectCollection.dataView.copy(
                content = dataViewContent.copy(
                    viewers = listOf(expectedView),
                    relationLinks = listOf(
                        objectCollection.relationLink1,
                        objectCollection.relationLink2,
                        objectCollection.relationLink3,
                        objectCollection.relationLink4,
                        objectCollection.relationLink5,
                        objectCollection.relationLink6,
                        relationLink4
                    )
                )
            )

            assertEquals(
                expected = ObjectState.DataView.Collection(
                    root = root,
                    blocks = listOf(
                        objectCollection.header,
                        objectCollection.title,
                        expectedDataView
                    ),
                    details = objectViewDetails.copy(
                        details = objectViewDetails.details.toMutableMap().apply {
                            this[root] = this[root]!!.toMutableMap().apply {
                                put(relationKey4, "")
                            }
                        }
                    ),
                    objectRestrictions = listOf(),
                    dataViewRestrictions = listOf()
                ),
                actual = second
            )

            expectNoEvents()
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}