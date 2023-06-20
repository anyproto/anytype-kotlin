package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.relationsFilteredByHiddenAndDescription
import com.anytypeio.anytype.presentation.sets.model.Viewer

suspend fun DVViewer.buildListViews(
    objects: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    objectOrderIds: List<Id>
): List<Viewer.ListView.Item> {
    val items = objects.mapNotNull { id ->
        val obj = store.get(id)
        if (obj != null) {
            when (obj.layout) {
                ObjectType.Layout.BASIC,
                ObjectType.Layout.SET,
                ObjectType.Layout.COLLECTION,
                ObjectType.Layout.OBJECT_TYPE,
                ObjectType.Layout.RELATION,
                ObjectType.Layout.FILE,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.BOOKMARK -> {
                    val description = if (relations.any { it.key == Relations.DESCRIPTION }) {
                        obj.description
                    } else {
                        null
                    }
                    Viewer.ListView.Item.Default(
                        objectId = obj.id,
                        relations = obj.relationsFilteredByHiddenAndDescription(
                            relations = relations,
                            urlBuilder = urlBuilder,
                            details = details,
                            settings = viewerRelations,
                            storeOfObjects = store
                        ),
                        name = obj.getProperName(),
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        ),
                        description = description,
                        hideIcon = hideIcon
                    )
                }
                ObjectType.Layout.PROFILE -> {
                    val description = if (relations.any { it.key == Relations.DESCRIPTION }) {
                        obj.description
                    } else {
                        null
                    }
                    Viewer.ListView.Item.Profile(
                        objectId = obj.id,
                        relations = obj.relationsFilteredByHiddenAndDescription(
                            relations = relations,
                            urlBuilder = urlBuilder,
                            details = details,
                            settings = viewerRelations,
                            storeOfObjects = store
                        ),
                        name = obj.getProperName(),
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        ),
                        description = description,
                        hideIcon = hideIcon
                    )
                }
                ObjectType.Layout.TODO -> {
                    val description = if (relations.any { it.key == Relations.DESCRIPTION }) {
                        obj.description
                    } else {
                        null
                    }
                    Viewer.ListView.Item.Task(
                        objectId = obj.id,
                        relations = obj.relationsFilteredByHiddenAndDescription(
                            relations = relations,
                            urlBuilder = urlBuilder,
                            details = details,
                            settings = viewerRelations,
                            storeOfObjects = store
                        ),
                        name = obj.getProperName(),
                        done = obj.done ?: false,
                        description = description
                    )
                }
                else -> null
            }
        } else {
            null
        }
    }
    return if (objectOrderIds.isNotEmpty()) {
        items.sortObjects(objectOrderIds)
    } else {
        items
    }
}

private fun List<Viewer.ListView.Item>.sortObjects(objectOrderIds: List<Id>): List<Viewer.ListView.Item> {
    val orderMap = objectOrderIds.mapIndexed { index, id -> id to index }.toMap()
    return sortedBy { orderMap[it.objectId] }
}