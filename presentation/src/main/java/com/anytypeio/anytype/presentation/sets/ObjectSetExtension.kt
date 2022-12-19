package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.ID_KEY
import com.anytypeio.anytype.presentation.relations.objectTypeRelation
import com.anytypeio.anytype.presentation.relations.view
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

fun ObjectSet.featuredRelations(
    ctx: Id,
    urlBuilder: UrlBuilder,
    relations: List<ObjectWrapper.Relation>
): BlockView.FeaturedRelation? {
    val block = blocks.find { it.content is Block.Content.FeaturedRelations }
    if (block != null) {
        val views = mutableListOf<DocumentRelationView>()
        val ids = details[ctx]?.featuredRelations ?: emptyList()
        views.addAll(
            mapFeaturedRelations(
                ctx = ctx,
                keys = ids,
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
    keys: List<String>,
    details: Block.Details,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder
): List<DocumentRelationView> = keys.mapNotNull { key ->
    when (key) {
        Relations.DESCRIPTION -> null
        Relations.TYPE -> {
            val objectTypeId = details.details[ctx]?.type?.firstOrNull()
            if (objectTypeId != null) {
                details.objectTypeRelation(
                    relationKey = key,
                    isFeatured = true,
                    objectTypeId = objectTypeId
                )
            } else {
                null
            }
        }
        Relations.SET_OF -> {
            val objectSet = ObjectWrapper.Basic(details.details[ctx]?.map.orEmpty())
            val sources = mutableListOf<ObjectView>()
            val source = objectSet.setOf.firstOrNull()
            if (source != null) {
                val wrapper = ObjectWrapper.Basic(details.details[source]?.map.orEmpty())
                if (!wrapper.isEmpty()) {
                    if (wrapper.isDeleted == true) {
                        DocumentRelationView.Source.Deleted(
                            relationId = details.details[ctx]?.id.orEmpty(),
                            relationKey = key,
                            name = Relations.RELATION_NAME_EMPTY,
                            isFeatured = true
                        )
                    } else {
                        sources.add(wrapper.toObjectView(urlBuilder = urlBuilder))
                        DocumentRelationView.Source.Base(
                            relationId = details.details[ctx]?.id.orEmpty(),
                            relationKey = key,
                            name = Relations.RELATION_NAME_EMPTY,
                            isFeatured = true,
                            sources = sources
                        )
                    }
                } else {
                    DocumentRelationView.Source.Base(
                        relationId = details.details[ctx]?.id.orEmpty(),
                        relationKey = key,
                        name = Relations.RELATION_NAME_EMPTY,
                        isFeatured = true,
                        sources = sources
                    )
                }
            } else {
                DocumentRelationView.Source.Base(
                    relationId = details.details[ctx]?.id.orEmpty(),
                    relationKey = key,
                    name = Relations.RELATION_NAME_EMPTY,
                    isFeatured = true,
                    sources = sources
                )
            }
        }
        else -> {
            val relation = relations.firstOrNull { it.key == key }
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
        types = type,
        isRelation = type.contains(ObjectTypeIds.RELATION)
    )
}