package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.valuesFilteredByHidden
import com.anytypeio.anytype.presentation.sets.model.Viewer

fun DVViewer.buildListViews(
    objects: List<ObjectWrapper.Basic>,
    relations: List<Relation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder
): List<Viewer.ListView.Item> = objects.mapNotNull { obj ->
    when (obj.layout) {
        ObjectType.Layout.BASIC,
        ObjectType.Layout.SET,
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.RELATION,
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.NOTE -> {
            Viewer.ListView.Item.Default(
                objectId = obj.id,
                relations = obj.valuesFilteredByHidden(
                    relations = relations,
                    urlBuilder = urlBuilder,
                    details = details,
                    settings = viewerRelations
                ),
                name = obj.getProperName(),
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.layout,
                    builder = urlBuilder
                ),
                description = obj.description
            )
        }
        ObjectType.Layout.PROFILE -> {
            Viewer.ListView.Item.Profile(
                objectId = obj.id,
                relations = obj.valuesFilteredByHidden(
                    relations = relations,
                    urlBuilder = urlBuilder,
                    details = details,
                    settings = viewerRelations
                ),
                name = obj.getProperName(),
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.layout,
                    builder = urlBuilder
                ),
                description = obj.description
            )
        }
        ObjectType.Layout.TODO -> {
            Viewer.ListView.Item.Task(
                objectId = obj.id,
                relations = obj.valuesFilteredByHidden(
                    relations = relations,
                    urlBuilder = urlBuilder,
                    details = details,
                    settings = viewerRelations
                ),
                name = obj.getProperName(),
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.layout,
                    builder = urlBuilder
                ),
                description = obj.description
            )
        }
        else -> null
    }
}