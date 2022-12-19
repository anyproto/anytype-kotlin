package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

data class ObjectSet(
    val blocks: List<Block> = emptyList(),
    val details: Map<Id, Block.Fields> = emptyMap(),
    val objectRestrictions: List<ObjectRestriction> = emptyList(),
    val restrictions: List<DataViewRestrictions> = emptyList()
) {

    val dataview: Block @Throws(NoSuchElementException::class)
    get() = blocks.first { it.content is DV }

    val dv: DV @Throws(NoSuchElementException::class)
    get() = blocks.first { it.content is DV }.content as DV

    val viewers: List<DVViewer> get() = dataview.content<DV>().viewers

    val isInitialized: Boolean = blocks.any { it.content is DV }

    companion object {
        fun init() = ObjectSet()
        fun reset() = ObjectSet()
    }
}