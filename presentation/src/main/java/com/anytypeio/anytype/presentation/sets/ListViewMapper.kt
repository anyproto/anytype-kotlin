package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.objects.relationsFilteredByHiddenAndDescription
import com.anytypeio.anytype.presentation.objects.setTypeRelationIconsAsNone
import com.anytypeio.anytype.presentation.sets.model.Viewer

suspend fun DVViewer.buildListViews(
    objects: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    objectOrderIds: List<Id>,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): List<Viewer.ListView.Item> {
    val items = objects.mapNotNull { id ->
        val obj = store.get(id)
        if (obj != null && obj.isValid) {
            when (obj.layout) {
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
                            settings = viewerRelations,
                            storeOfObjects = store,
                            fieldParser = fieldParser,
                            storeOfObjectTypes = storeOfObjectTypes
                        ).setTypeRelationIconsAsNone(),
                        name = fieldParser.getObjectName(obj),
                        icon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(obj)
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
                            settings = viewerRelations,
                            storeOfObjects = store,
                            fieldParser = fieldParser,
                            storeOfObjectTypes = storeOfObjectTypes
                        ).setTypeRelationIconsAsNone(),
                        name = fieldParser.getObjectName(obj),
                        done = obj.done == true,
                        description = description
                    )
                }
                else -> {
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
                            settings = viewerRelations,
                            storeOfObjects = store,
                            fieldParser = fieldParser,
                            storeOfObjectTypes = storeOfObjectTypes
                        ).setTypeRelationIconsAsNone(),
                        name = fieldParser.getObjectName(obj),
                        icon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(obj)
                        ),
                        description = description,
                        hideIcon = hideIcon
                    )
                }
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