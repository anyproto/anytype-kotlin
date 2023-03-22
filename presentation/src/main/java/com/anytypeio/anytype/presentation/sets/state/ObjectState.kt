package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

sealed class ObjectState {

    abstract val isInitialized: Boolean

    sealed class DataView : ObjectState() {

        abstract val root: Id
        abstract val blocks: List<Block>
        abstract val details: Map<Id, Block.Fields>
        abstract val objectRestrictions: List<ObjectRestriction>
        abstract val dataViewRestrictions: List<DataViewRestrictions>

        abstract val objectRelationLinks: List<RelationLink>

        abstract val dataViewContent: DV
        abstract val dataViewBlock: Block
        abstract val viewers: List<DVViewer>

        data class Set(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: Map<Id, Block.Fields> = emptyMap(),
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val objectRelationLinks: List<RelationLink> = emptyList()
        ) : DataView() {

            override val isInitialized get() = blocks.any { it.content is DV }
            override val dataViewBlock get() = blocks.first { it.content is DV }
            override val dataViewContent get() = dataViewBlock.content as DV
            override val viewers get() = dataViewContent.viewers
        }

        data class Collection(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: Map<Id, Block.Fields> = emptyMap(),
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val objectRelationLinks: List<RelationLink> = emptyList()
        ) : DataView() {

            override val isInitialized get() = blocks.any { it.content is DV }
            override val dataViewBlock get() = blocks.first { it.content is DV }
            override val dataViewContent get() = dataViewBlock.content as DV
            override val viewers get() = dataViewContent.viewers
        }
    }

    object Init : ObjectState() {
        override val isInitialized: Boolean
            get() = false
    }

    object ErrorLayout : ObjectState() {
        override val isInitialized: Boolean
            get() = false
    }
}
