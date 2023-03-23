package com.anytypeio.anytype.presentation.collections

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
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
import net.bytebuddy.utility.RandomString

class MockCollection(context: String) {
    val root = context
    val title = StubTitle(id = "title-${RandomString.make()}", text = "title-name-${RandomString.make()}")
    val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))

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
        key = "relationStatus-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.STATUS
    )
    val relationObject5 = StubRelationObject(
        key = "relationObject-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.OBJECT
    )
    val relationObject6 = StubRelationObject(
        key = "relationNumber-${RandomString.make()}",
        isReadOnlyValue = false,
        format = Relation.Format.NUMBER
    )

    // VIEW RELATIONS
    val dvViewerRelation1 =
        StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
    val dvViewerRelation2 =
        StubDataViewViewRelation(key = relationObject2.key, isVisible = true)
    val dvViewerRelation3 =
        StubDataViewViewRelation(key = relationObject3.key, isVisible = true)
    val dvViewerRelation4 =
        StubDataViewViewRelation(key = relationObject4.key, isVisible = true)
    val dvViewerRelation5 =
        StubDataViewViewRelation(key = relationObject5.key, isVisible = true)
    val dvViewerRelation6 =
        StubDataViewViewRelation(key = relationObject6.key, isVisible = true)

    // RELATION LINKS
    val relationLink1 = StubRelationLink(relationObject1.key)
    val relationLink2 = StubRelationLink(relationObject2.key)
    val relationLink3 = StubRelationLink(relationObject3.key)
    val relationLink4 = StubRelationLink(relationObject4.key)
    val relationLink5 = StubRelationLink(relationObject5.key)
    val relationLink6 = StubRelationLink(relationObject6.key)

    // SEARCH OBJECTS COMMAND, RELATION KEYS
    val dvKeys = listOf(
        relationObject1.key,
        relationObject2.key,
        relationObject3.key,
        relationObject4.key,
        relationObject5.key,
        relationObject6.key
    )

    // SORTS
    val sort1 = DVSort(
        id = "sortId-${RandomString.make()}",
        relationKey = relationObject1.key,
        type = DVSortType.ASC
    )
    val sortGrid = DVSort(
        id = "sortId-${RandomString.make()}",
        relationKey = relationObject5.key,
        type = DVSortType.DESC
    )
    val sortGallery = DVSort(
        id = "sortId-${RandomString.make()}",
        relationKey = relationObject6.key,
        type = DVSortType.DESC
    )
    val sorts = listOf(sort1)

    val viewerList = StubDataViewView(
        id = "dvViewerList-${RandomString.make()}",
        viewerRelations = listOf(dvViewerRelation1, dvViewerRelation2, dvViewerRelation3),
        type = DVViewerType.LIST,
        sorts = sorts
    )
    val viewerGrid = StubDataViewView(
        id = "dvViewerGrid-${RandomString.make()}",
        viewerRelations = listOf(dvViewerRelation6, dvViewerRelation5, dvViewerRelation3),
        type = DVViewerType.GRID,
        sorts = listOf(sortGrid)
    )
    val viewerGallery = StubDataViewView(
        id = "dvViewerGallery-${RandomString.make()}",
        viewerRelations = listOf(
            dvViewerRelation1,
            dvViewerRelation2,
            dvViewerRelation3,
            dvViewerRelation4,
            dvViewerRelation5,
            dvViewerRelation6
        ),
        type = DVViewerType.GALLERY,
        sorts = listOf(sortGallery)
    )

    val obj1 = StubObject(id = "object1-${RandomString.make()}")
    val obj2 = StubObject(id = "object2-${RandomString.make()}")
    val obj3 = StubObject(id = "object3-${RandomString.make()}")
    val obj4 = StubObject(id = "object4-${RandomString.make()}")
    val obj5 = StubObject(id = "object5-${RandomString.make()}")

    // 3, 2, 4, 5, 1
    val objectOrderList =
        ObjectOrder(
            view = viewerList.id,
            group = "",
            ids = listOf(obj3.id, obj2.id, obj4.id, obj5.id, obj1.id)
        )

    // 1, 2, 4, 3, 5
    val objectOrderGrid =
        ObjectOrder(
            view = viewerGrid.id,
            group = "",
            ids = listOf(obj1.id, obj2.id, obj4.id, obj3.id, obj5.id)
        )

    // 5, 4, 3, 2, 1
    val objectOrderGallery =
        ObjectOrder(
            view = viewerGallery.id,
            group = "",
            ids = listOf(obj5.id, obj4.id, obj3.id, obj2.id, obj1.id)
        )
    val dataView = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewerList),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5, relationLink6),
        isCollection = true,
        objectOrder = emptyList()
    )

    val dataViewGrid = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewerGrid),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5, relationLink6),
        isCollection = true,
        objectOrder = emptyList()
    )

    val dataViewGallery = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewerGallery),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5, relationLink6),
        isCollection = true,
        objectOrder = emptyList()
    )

    val dataViewWithObjectOrder = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewerList, viewerGrid, viewerGallery),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3, relationLink4, relationLink5, relationLink6),
        objectOrder = listOf(objectOrderList, objectOrderGrid, objectOrderGallery),
        isCollection = true
    )
    val workspaceId = "workspace-${RandomString.make()}"

    val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(context)

    val details = Block.Details(
        details = mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.ID to root,
                    Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
                )
            )
        )
    )

    val dataViewEmpty = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(),
        relationLinks = listOf()
    )
}