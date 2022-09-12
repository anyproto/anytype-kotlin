package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.ID_KEY
import com.anytypeio.anytype.presentation.relations.view
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

fun ObjectSet.updateRecord(
    viewer: Id,
    update: List<DVRecord>
): ObjectSet {
    val current = viewerDb[viewer]
    return if (current != null) {
        val new = current.records.update(update)
        val updatedRecords = viewerDb.toMutableMap().apply {
            put(viewer, current.copy(records = new))
        }
        this.copy(viewerDb = updatedRecords)
    } else {
        this.copy()
    }
}

fun ObjectSet.deleteRecords(
    viewer: Id,
    recordIds: List<String>
): ObjectSet {
    val current = viewerDb[viewer]
    return if (current != null) {
        val new = current.records.mapNotNull { rec ->
            val id = rec[ID_KEY]
            if (id != null && !recordIds.contains(id)) {
                rec
            } else {
                null
            }
        }
        val updatedRecords = viewerDb.toMutableMap().apply {
            put(viewer, current.copy(records = new))
        }
        this.copy(viewerDb = updatedRecords)
    } else {
        this.copy()
    }
}

fun ObjectSet.featuredRelations(
    ctx: Id,
    urlBuilder: UrlBuilder
): BlockView.FeaturedRelation? {
    val block = blocks.find { it.content is Block.Content.FeaturedRelations }
    if (block != null) {
        val views = mutableListOf<DocumentRelationView>()
        val ids = details[ctx]?.featuredRelations ?: emptyList()
        views.addAll(
            mapFeaturedRelations(
                ctx = ctx,
                ids = ids,
                details = Block.Details(details),
                relations = relations,
                urlBuilder = urlBuilder
            )
        )
        return BlockView.FeaturedRelation(
            id = block.id,
            relations = views
        )
    } else {
        return null
    }
}

private fun mapFeaturedRelations(
    ctx: Id,
    ids: List<String>,
    details: Block.Details,
    relations: List<Relation>,
    urlBuilder: UrlBuilder
): List<DocumentRelationView> = ids.mapNotNull { id ->
    when (id) {
        Relations.DESCRIPTION -> null
        Relations.TYPE -> {
            val objectTypeId = details.details[ctx]?.type?.firstOrNull()
            if (objectTypeId != null) {
                DocumentRelationView.ObjectType(
                    relationId = id,
                    name = details.details[objectTypeId]?.name.orEmpty(),
                    isFeatured = true,
                    type = objectTypeId
                )
            } else {
                null
            }
        }
        Relations.SET_OF -> {
            val objectSet = ObjectWrapper.Basic(details.details[ctx]?.map.orEmpty())
            val sources = mutableListOf<ObjectView>()
            objectSet.setOf.forEach { objectTypeId ->
                val wrapper = ObjectWrapper.Basic(details.details[objectTypeId]?.map.orEmpty())
                if (!wrapper.isEmpty()) {
                    sources.add(wrapper.toObjectView(urlBuilder = urlBuilder))
                }
            }
            DocumentRelationView.Source(
                relationId = id,
                name = Relations.RELATION_NAME_EMPTY,
                isFeatured = true,
                sources = sources
            )
        }
        else -> {
            val relation = relations.firstOrNull { it.key == id }
            relation?.view(
                details = details,
                values = details.details[ctx]?.map ?: emptyMap(),
                urlBuilder = urlBuilder,
                isFeatured = true
            )
        }
    }
}

fun List<DVRecord>.update(new: List<DVRecord>): List<DVRecord> {
    val update = new.associateBy { rec -> rec[ID_KEY] as String }
    val result = mutableListOf<DVRecord>()
    forEach { rec ->
        val id = rec[ID_KEY]
        if (update.containsKey(id)) {
            val updated = update[id]
            if (updated != null)
                result.add(updated)
            else
                result.add(rec)
        } else {
            result.add(rec)
        }
    }
    // TODO remove this part when middleware is fixed
    update.forEach { (id, record) ->
        val isAlreadyPresent = result.any { r -> r[ID_KEY] == id }
        if (!isAlreadyPresent) result.add(record)
    }
    return result
}

fun ObjectSet.viewerById(currentViewerId: String?): Block.Content.DataView.Viewer {
    val block = dataview.content
    val dv = block as DV
    return dv.viewers.find { it.id == currentViewerId } ?: dv.viewers.first()
}

fun List<SimpleRelationView>.filterHiddenRelations(): List<SimpleRelationView> =
    filter { !it.isHidden }

fun DV.getRelation(relationKey: Id): Relation? = relations.firstOrNull { it.key == relationKey }

fun DV.isRelationReadOnly(relationKey: Id): Boolean {
    val relation = getRelation(relationKey)
    return relation != null && relation.isReadOnly
}

fun ObjectWrapper.Basic.toObjectView(urlBuilder: UrlBuilder): ObjectView = when (isDeleted) {
    true -> ObjectView.Deleted(id)
    else -> ObjectView.Default(
        id = id,
        name = getProperName(),
        icon = ObjectIcon.from(
            obj = this,
            layout = layout,
            builder = urlBuilder
        ),
        types = type
    )
}