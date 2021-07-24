package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig

/**
 * @property [viewerDb] maps viewer to its records by viewer's id
 */
data class ObjectSet(
    val blocks: List<Block> = emptyList(),
    val viewerDb: Map<Id, ViewerData> = emptyMap(),
    val details: Map<Id, Block.Fields> = emptyMap(),
    val objectTypes: List<ObjectType> = emptyList(),
    val relations: List<Relation> = emptyList(),
    val objectRestrictions: List<ObjectRestriction> = emptyList(),
    val restrictions: List<DataViewRestrictions> = emptyList()
) {

    val dataview: Block get() = blocks.first { it.content is DV }

    val viewers: List<DVViewer> get() = dataview.content<DV>().viewers

    val isInitialized: Boolean = blocks.any { it.content is DV }

    fun getRecord(viewer: Id, target: Id): DVRecord {
        val records = viewerDb[viewer]?.records ?: emptyList()
        return records.first { record -> record[ObjectSetConfig.ID_KEY] == target }
    }

    companion object {
        fun init() = ObjectSet()
    }

    /**
     * Set of raw-data records that a specific viewer contains.
     */
    data class ViewerData(
        val records: List<DVRecord>,
        val total: Int
    )
}