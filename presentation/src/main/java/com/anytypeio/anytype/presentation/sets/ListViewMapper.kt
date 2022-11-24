package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.valuesFilteredByHidden
import com.anytypeio.anytype.presentation.sets.model.Viewer

suspend fun DVViewer.buildListViews(
    objects: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
): List<Viewer.ListView.Item> = objects.mapNotNull { id ->
    val obj = store.get(id)
    if (obj != null) {
        when (obj.layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.SET,
            ObjectType.Layout.OBJECT_TYPE,
            ObjectType.Layout.RELATION,
            ObjectType.Layout.FILE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.BOOKMARK -> {
                Viewer.ListView.Item.Default(
                    objectId = obj.id,
                    relations = obj.valuesFilteredByHidden(
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
                    description = obj.description,
                    hideIcon = hideIcon
                )
            }
            ObjectType.Layout.PROFILE -> {
                Viewer.ListView.Item.Profile(
                    objectId = obj.id,
                    relations = obj.valuesFilteredByHidden(
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
                    description = obj.description,
                    hideIcon = hideIcon
                )
            }
            ObjectType.Layout.TODO -> {
                Viewer.ListView.Item.Task(
                    objectId = obj.id,
                    relations = obj.valuesFilteredByHidden(
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
                    description = obj.description,
                    hideIcon = hideIcon
                )
            }
            else -> null
        }
    } else {
        null
    }
}