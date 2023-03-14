package com.anytypeio.anytype.presentation.collections

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.DVViewerType
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
import net.bytebuddy.asm.Advice.OffsetMapping.Sort
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

    // VIEW RELATIONS
    val dvViewerRelation1 =
        StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
    val dvViewerRelation2 =
        StubDataViewViewRelation(key = relationObject2.key, isVisible = true)
    val dvViewerRelation3 =
        StubDataViewViewRelation(key = relationObject3.key, isVisible = true)

    // RELATION LINKS
    val relationLink1 = StubRelationLink(relationObject1.key)
    val relationLink2 = StubRelationLink(relationObject2.key)
    val relationLink3 = StubRelationLink(relationObject3.key)

    // SEARCH OBJECTS COMMAND, RELATION KEYS
    val dvKeys = listOf(
        relationObject1.key,
        relationObject2.key,
        relationObject3.key
    )

    // SORTS
    val sort1 = DVSort(
        id = "sortId-${RandomString.make()}",
        relationKey = relationObject1.key,
        type = DVSortType.ASC
    )
    val sorts = listOf(sort1)

    val viewer =
        StubDataViewView(
            id = "dvViewer-${RandomString.make()}",
            viewerRelations = listOf(dvViewerRelation1, dvViewerRelation2, dvViewerRelation3),
            type = DVViewerType.LIST,
            sorts = sorts
        )
    val dataView = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewer),
        relationLinks = listOf(relationLink1, relationLink2, relationLink3)
    )
    val workspaceId = "workspace-${RandomString.make()}"
    val obj1 = StubObject(id = "object1-${RandomString.make()}")
    val obj2 = StubObject(id = "object2-${RandomString.make()}")
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