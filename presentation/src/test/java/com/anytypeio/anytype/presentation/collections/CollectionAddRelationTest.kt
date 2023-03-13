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
import com.anytypeio.anytype.presentation.sets.DataViewViewState
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
    }

    @After
    fun after() {
        rule.advanceTime(100)
    }

    private val COLLECTION_LAYOUT = ObjectType.Layout.COLLECTION.code.toDouble()

    @Test
    fun `should add relation and update value of new relation`() = runTest {
        // SETUP
        val objectCollection = MockCollection(root)

        stubWorkspaceManager(objectCollection.workspaceId)
        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            workspace = objectCollection.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = objectCollection.dvKeys,
            objects = listOf(objectCollection.obj1, objectCollection.obj2)
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

        val vm = givenViewModel()

        // TESTING
        vm.onStart(ctx = root)

        val relationId3 = "rel-newRelationKey"
        val relationKey3 = "newRelationKey"
        val relationName3 = "NEW RELATION"
        val relationObject3 = StubRelationObject(
            id = relationId3,
            key = relationKey3,
            name = relationName3,
            workspaceId = objectCollection.workspaceId
        )
        val relationLink3 = RelationLink(relationKey3, relationObject3.format)
        val dvViewerRelation3 = DVViewerRelation(
            key = relationKey3,
            isVisible = true
        )

        vm.currentViewer.test {

            val initState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Init,
                actual = initState
            )

            val noItemsState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Collection.NoItems(title = objectCollection.viewer.name),
                actual = noItemsState
            )

            val gridState = awaitItem()
            assertEquals(
                expected = DataViewViewState.Collection.Default(
                    viewer = Viewer.ListView(
                        id = objectCollection.viewer.id,
                        title = objectCollection.viewer.name,
                        items = listOf(
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj1.id,
                                name = objectCollection.obj1.getProperName(),
                                description = objectCollection.obj1.description,
                                hideIcon = false,
                                icon = ObjectIcon.Basic.Avatar(name = objectCollection.obj1.name!!),
                                relations = listOf()
                            ),
                            Viewer.ListView.Item.Default(
                                objectId = objectCollection.obj2.id,
                                name = objectCollection.obj2.getProperName(),
                                description = objectCollection.obj2.description,
                                hideIcon = false,
                                icon = ObjectIcon.Basic.Avatar(name = objectCollection.obj2.name!!),
                                relations = listOf()
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
                target = relationId3,
                details = Block.Fields(relationObject3.map)
            )
            val eventDataViewRelationSet = Event.Command.DataView.SetRelation(
                context = root,
                dv = objectCollection.dataView.id,
                links = listOf(relationLink3)
            )

            val eventDataViewUpdateView = Event.Command.DataView.UpdateView(
                context = root,
                block = objectCollection.dataView.id,
                viewerId = objectCollection.viewer.id,
                relationUpdates = listOf(
                    Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Add(
                        afterId = objectCollection.relationKey2,
                        relations = listOf(dvViewerRelation3)
                    )
                ),
                fields = null,
                filterUpdates = listOf(),
                sortUpdates = listOf()
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

            val expectedView = objectCollection.viewer.copy(
                viewerRelations = listOf(
                    objectCollection.dvViewerRelation1,
                    objectCollection.dvViewerRelation2,
                    dvViewerRelation3
                )
            )

            val dataViewContent = objectCollection.dataView.content as Block.Content.DataView
            val expectedDataView = objectCollection.dataView.copy(
                content = dataViewContent.copy(
                    viewers = listOf(expectedView),
                    relationLinks = listOf(
                        objectCollection.relationLink1,
                        objectCollection.relationLink2,
                        relationLink3
                    )
                )
            )

            assertEquals(
                expected = ObjectState.DataView.Collection(
                    blocks = listOf(
                        objectCollection.header,
                        objectCollection.title,
                        expectedDataView
                    ),
                    details = mapOf(
                        rootObject.id to Block.Fields(rootObject.map),
                        relationObject3.id to Block.Fields(relationObject3.map)
                    ),
                    objectRestrictions = listOf(),
                    dataViewRestrictions = listOf()
                ),
                actual = second
            )
            expectNoEvents()

           // TODO ADD LOGIC : SET VALUE TO NEW RELATION
//            //set value to new relation3 to object1
//
//            val eventRelationLinksAmend = Event.Command.ObjectRelationLinks.Amend(
//                context = root,
//                id = obj1.id,
//                relationLinks = listOf(relationLink3)
//            )
//
//            val eventObjectDetailsAmend = Event.Command.Details.Amend(
//                context = root,
//                target = obj1.id,
//                details = mapOf(relationKey3 to "Foobar")
//            )
//
//            dispatcher.send(
//                Payload(
//                    context = root,
//                    events = listOf(eventRelationLinksAmend, eventObjectDetailsAmend)
//                )
//            )
//            expectNoEvents()
//            awaitComplete()
        }
    }
}