package com.anytypeio.anytype.presentation.collections

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.test_utils.MockDataFactory
import net.bytebuddy.utility.RandomString

class MockSet(context: String, val setOfValue: String = "setOf-${RandomString.make()}") {

    val root = context
    val title =
        StubTitle(id = "title-${RandomString.make()}", text = "title-name-${RandomString.make()}")
    val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))
    val emoji = MockDataFactory.randomString()
    val headerWithEmoji = StubHeader(
        id = "headerWithEmoji-${RandomString.make()}",
        children = listOf(title.id),
        fields = Block.Fields(mapOf(Relations.ICON_EMOJI to emoji))
    )
    val workspaceId = "workspace-${RandomString.make()}"
    val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(context)
    val setOf get() = setOfValue

    // RELATION OBJECTS
    val relationObject1 = StubRelationObject(
        key = "relationText-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.LONG_TEXT
    )
    val relationObject2 = StubRelationObject(
        key = "relationTextReadOnly-${RandomString.make()}",
        isReadOnlyValue = true,
        format = Relation.Format.LONG_TEXT
    )
    val relationObject3 = StubRelationObject(
        key = "relationTag-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.TAG
    )
    val relationObject4 = StubRelationObject(
        key = "relationObject-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.OBJECT
    )
    val relationObject5 = StubRelationObject(
        key = "relationObjectReadOnly-${RandomString.make()}",
        isReadOnlyValue = true,
        format = Relation.Format.OBJECT
    )

    // VIEW RELATIONS
    private val dvViewerRelation1 =
        StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
    private val dvViewerRelation2 =
        StubDataViewViewRelation(key = relationObject2.key, isVisible = true)
    val dvViewerRelation3 =
        StubDataViewViewRelation(key = relationObject3.key, isVisible = true)
    private val dvViewerRelation4 =
        StubDataViewViewRelation(key = relationObject4.key, isVisible = true)
    private val dvViewerRelation5 =
        StubDataViewViewRelation(key = relationObject5.key, isVisible = true)

    // RELATION LINKS
    val relationLink1 = StubRelationLink(relationObject1.key)
    val relationLink2 = StubRelationLink(relationObject2.key)
    val relationLink3 = StubRelationLink(relationObject3.key)
    val relationLink4 = StubRelationLink(relationObject4.key)
    val relationLink5 = StubRelationLink(relationObject5.key)

    // SEARCH OBJECTS COMMAND, RELATION KEYS
    val dvKeys = listOf(
        relationObject1.key,
        relationObject2.key,
        relationObject3.key,
        relationObject4.key,
        relationObject5.key
    )

    // VIEW FILTERS
    val filters = listOf(
        DVFilter(
            id = "dvFilter-${RandomString.make()}",
            relation = relationObject1.key,
            relationFormat = RelationFormat.LONG_TEXT,
            condition = Block.Content.DataView.Filter.Condition.EQUAL,
            value = "dvFilterValue-${RandomString.make()}"
        ),
        DVFilter(
            id = "dvFilter-${RandomString.make()}",
            relation = relationObject3.key,
            relationFormat = RelationFormat.TAG,
            condition = Block.Content.DataView.Filter.Condition.IN,
            value = "dvFilterValue-${RandomString.make()}"
        )
    )

    // VIEWS
    val viewerList =
        StubDataViewView(
            id = "dvViewerList-${RandomString.make()}",
            viewerRelations = listOf(
                dvViewerRelation1,
                dvViewerRelation2,
                dvViewerRelation3,
                dvViewerRelation4,
                dvViewerRelation5
            ),
            type = DVViewerType.LIST
        )
    val viewerGrid =
        StubDataViewView(
            id = "dvViewerGrid-${RandomString.make()}",
            viewerRelations = listOf(
                dvViewerRelation1,
                dvViewerRelation2,
                dvViewerRelation3,
                dvViewerRelation4,
                dvViewerRelation5
            ),
            type = DVViewerType.GRID,
            filters = filters
        )
    val viewerGallery =
        StubDataViewView(
            id = "dvViewerGallery-${RandomString.make()}",
            viewerRelations = listOf(
                dvViewerRelation1,
                dvViewerRelation2,
                dvViewerRelation3,
                dvViewerRelation4,
                dvViewerRelation5
            ),
            type = DVViewerType.GALLERY
        )

    var viewer = viewerGrid

    // DATA VIEW BLOCK
    val dataView = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewer),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5)
    )
    val dataViewNoViews = StubDataView(
        id = "dvNoViews-${RandomString.make()}",
        views = listOf(),
        relationLinks = listOf()
    )
    val dataViewWith3Views = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewerGrid, viewerGallery, viewerList),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5)
    )

    // RECORDS
    val obj1 = StubObject(id = "object-${RandomString.make()}")
    val obj2 = StubObject(id = "object-${RandomString.make()}")

    // SET OBJECT DETAILS
    val details = Block.Details(
        details = mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.ID to root,
                    Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                    Relations.SET_OF to listOf(setOf)
                )
            ),
            setOf to Block.Fields(
                map = mapOf(
                    Relations.ID to setOf,
                    Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                    Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                )
            )
        )
    )

    val detailsEmptySetOf = Block.Details(
        details = mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.ID to root,
                    Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                    Relations.SET_OF to listOf<String>()
                )
            )
        )
    )

    val detailsSetByRelation = Block.Details(
        details = mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.ID to root,
                    Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                    Relations.SET_OF to relationObject3.id
                )
            ),
            relationObject3.id to Block.Fields(
                mapOf(
                    Relations.ID to relationObject3.id,
                    Relations.RELATION_KEY to relationObject3.key,
                    Relations.TYPE to ObjectTypeIds.RELATION
                )
            )
        )
    )

    fun detailsSetByRelation(relationSetBy: ObjectWrapper.Relation) = Block.Details(
        details = mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.ID to root,
                    Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
                    Relations.SET_OF to relationSetBy.key
                )
            ),
            relationSetBy.key to Block.Fields(
                mapOf(
                    Relations.ID to relationSetBy.id,
                    Relations.RELATION_KEY to relationSetBy.key,
                    Relations.TYPE to ObjectTypeIds.RELATION
                )
            )
        )
    )
}