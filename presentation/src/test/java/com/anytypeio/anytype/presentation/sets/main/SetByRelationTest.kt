package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class SetByRelationTest : ObjectSetViewModelTestSetup() {

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))

    private val relations = listOf(
        StubRelationObject(),
        StubRelationObject(),
        StubRelationObject()
    )

    private val viewer = StubDataViewView(
        viewerRelations = relations.map {
            StubDataViewViewRelation(
                key = it.key
            )
        }
    )

    private val dv = StubDataView(
        views = listOf(viewer),
        relations = relations.map {
            RelationLink(
                key = it.key,
                format = it.relationFormat
            )
        }
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    @Test
    fun `should create new object with source object type if given set is aggregated by specific object type`() {

        // SETUP

        val givenSourceType = MockDataFactory.randomUuid()

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        map = mapOf(Relations.SET_OF to listOf(givenSourceType))
                    ),
                    givenSourceType to Block.Fields(
                        map = mapOf(Relations.TYPE to ObjectTypeIds.OBJECT_TYPE)
                    )
                )
            )
        )
        stubGetTemplates(type = givenSourceType)

        val vm = givenViewModel()


        // TESTING

        with(vm) {
            onStart(root)
            onCreateNewDataViewObject()
        }

        verifyBlocking(createDataViewObject, times(1)) {
            invoke(
                CreateDataViewObject.Params.SetByType(
                    type = givenSourceType,
                    filters = viewer.filters
                )
            )
        }
    }

    @Test
    fun `should create new object with default object type if given set is aggregated by relations`() {

        // SETUP

        val givenSourceRelation = relations.random()

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        map = mapOf(
                            Relations.SET_OF to listOf(givenSourceRelation.id)
                        )
                    ),
                    givenSourceRelation.id to Block.Fields(
                        map = mapOf(
                            Relations.ID to givenSourceRelation.id,
                            Relations.RELATION_KEY to givenSourceRelation.key,
                            Relations.TYPE to ObjectTypeIds.RELATION
                        )
                    )
                )
            )
        )

        val vm = givenViewModel()

        // TESTING

        with(vm) {
            onStart(root)
            onCreateNewDataViewObject()
        }

        verifyBlocking(createDataViewObject, times(1)) {
            invoke(
                CreateDataViewObject.Params.SetByRelation(
                    filters = viewer.filters,
                    relations = listOf(givenSourceRelation.id)
                )
            )
        }
    }
}