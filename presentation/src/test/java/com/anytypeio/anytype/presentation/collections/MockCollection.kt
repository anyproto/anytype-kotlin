package com.anytypeio.anytype.presentation.collections

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import net.bytebuddy.utility.RandomString

class MockCollection(context: String) {
    val root = context
    val title = StubTitle(id = "title-${RandomString.make()}", text = "title-name-${RandomString.make()}")
    val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))
    val relationKey1 = "relation-${RandomString.make()}"
    val relationKey2 = "relation-${RandomString.make()}"
    val dvViewerRelation1 = StubDataViewViewRelation(key = relationKey1, isVisible = true)
    val dvViewerRelation2 = StubDataViewViewRelation(key = relationKey2, isVisible = true)
    val relationLink1 = StubRelationLink(relationKey1)
    val relationLink2 = StubRelationLink(relationKey2)
    val viewer =
        StubDataViewView(
            id = "dvViewer-${RandomString.make()}",
            viewerRelations = listOf(dvViewerRelation1, dvViewerRelation2),
            type = DVViewerType.LIST
        )
    val dataView = StubDataView(
        id = "dv-${RandomString.make()}",
        views = listOf(viewer),
        relationLinks = listOf(relationLink1, relationLink2)
    )
    val workspaceId = "workspace-${RandomString.make()}"
    val obj1 = StubObject(id = "object-${RandomString.make()}")
    val obj2 = StubObject(id = "object-${RandomString.make()}")
    val dvKeys = listOf(relationKey1, relationKey2)
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