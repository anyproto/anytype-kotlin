package com.anytypeio.anytype.presentation.collections

import android.util.Log
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations.ID
import com.anytypeio.anytype.core_models.Relations.LAYOUT
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
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
        viewModel = givenViewModel()
        objectCollection = MockCollection(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    private val COLLECTION_LAYOUT = ObjectType.Layout.COLLECTION.code.toDouble()

    @Test
    fun `should add new relation to data view`() = runTest {
        // SETUP

        stubWorkspaceManager(objectCollection.workspaceId)
        stubStoreOfRelations(objectCollection)
        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            workspace = objectCollection.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = objectCollection.dvKeys,
            objects = listOf(objectCollection.obj1, objectCollection.obj2),
            dvSorts = objectCollection.sorts
        )
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val rootObject = ObjectWrapper.Basic(map = mapOf(ID to root, LAYOUT to COLLECTION_LAYOUT))

        stubOpenObject(
            doc = listOf(
                objectCollection.header,
                objectCollection.title,
                objectCollection.dataView
            ),
            details = objectCollection.details
        )

        // TESTING
        viewModel.onStart(ctx = root)

        val relationId4 = "rel-newRelationKey"
        val relationKey4 = "newRelationKey"
        val relationName4 = "NEW RELATION"
        val relationObject4 = StubRelationObject(
            id = relationId4,
            key = relationKey4,
            name = relationName4,
            workspaceId = objectCollection.workspaceId
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

            val noItemsState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Collection.NoItems(title = objectCollection.viewerList.name),
                actual = noItemsState
            )

            val gridState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Collection.Default(
                    viewer = Viewer.ListView(
                        id = objectCollection.viewerList.id,
                        title = objectCollection.viewerList.name,
                        items = listOf(
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj1.id,
                                name = objectCollection.obj1.getProperName(),
                                description = objectCollection.obj1.description,
                                hideIcon = false,
                                icon = ObjectIcon.Basic.Avatar(name = objectCollection.obj1.name!!),
                                relations = listOf(
                                    DefaultObjectRelationValueView.Text(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject1.key,
                                        text = null
                                    ),
                                    DefaultObjectRelationValueView.Text(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject2.key,
                                        text = null
                                    ),
                                    DefaultObjectRelationValueView.Tag(
                                        objectId = objectCollection.obj1.id,
                                        relationKey = objectCollection.relationObject3.key,
                                        tags = emptyList()
                                    )
                                )
                            ),
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj2.id,
                                name = objectCollection.obj2.getProperName(),
                                description = objectCollection.obj2.description,
                                hideIcon = false,
                                icon = ObjectIcon.Basic.Avatar(name = objectCollection.obj2.name!!),
                                relations = listOf(
                                    DefaultObjectRelationValueView.Text(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject1.key,
                                        text = null
                                    ),
                                    DefaultObjectRelationValueView.Text(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject2.key,
                                        text = null
                                    ),
                                    DefaultObjectRelationValueView.Tag(
                                        objectId = objectCollection.obj2.id,
                                        relationKey = objectCollection.relationObject3.key,
                                        tags = emptyList()
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
                    details = mapOf(
                        rootObject.id to Block.Fields(rootObject.map)
                    ),
                    objectRestrictions = listOf(),
                    dataViewRestrictions = listOf()
                ),
                actual = first
            )

            //Add new relation from marketplace to object and view
            val eventObjectDetailsSet = Event.Command.Details.Set(
                context = root,
                target = relationId4,
                details = Block.Fields(relationObject4.map)
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
                workspace = objectCollection.workspaceId,
                storeOfRelations = storeOfRelations,
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
                    details = mapOf(
                        rootObject.id to Block.Fields(rootObject.map),
                        relationObject4.id to Block.Fields(relationObject4.map)
                    ),
                    objectRestrictions = listOf(),
                    dataViewRestrictions = listOf()
                ),
                actual = second
            )

            expectNoEvents()
        }
    }
}