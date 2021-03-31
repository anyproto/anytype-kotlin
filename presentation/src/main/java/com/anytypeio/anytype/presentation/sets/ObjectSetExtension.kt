package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.ID_KEY
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