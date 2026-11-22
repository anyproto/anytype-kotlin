package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

sealed class ObjectState {

    abstract val isInitialized: Boolean

    sealed class DataView : ObjectState() {

        abstract val root: Id
        abstract val blocks: List<Block>
        abstract val details: ObjectViewDetails
        abstract val objectRestrictions: List<ObjectRestriction>
        abstract val dataViewRestrictions: List<DataViewRestrictions>

        abstract val hasObjectLayoutConflict: Boolean

        /**
         * The id of an inline query block when the screen shows that block of a page, else null.
         * The page is then [root]: the block holds the views, and its target holds the source.
         */
        abstract val inlineBlockId: Id?

        val isInline: Boolean get() = inlineBlockId != null

        /**
         * The object that defines the records: the set, the collection, or the type itself.
         * For an inline query, the target of the block.
         */
        val sourceObjectId: Id
            get() = if (inlineBlockId != null) dataViewContent.targetObjectId else root

        override val isInitialized: Boolean
            get() = blocks.any { it.isScreenDataView() }
        val dataViewBlock: Block get() = blocks.first { it.isScreenDataView() }
        val dataViewContent: DV get() = dataViewBlock.content as DV
        val viewers: List<DVViewer> get() = dataViewContent.viewers

        private fun Block.isScreenDataView(): Boolean =
            content is DV && (inlineBlockId == null || id == inlineBlockId)

        data class Set(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
            override val inlineBlockId: Id? = null,
        ) : DataView()

        data class Collection(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
            override val inlineBlockId: Id? = null,
        ) : DataView()

        data class TypeSet(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
            override val inlineBlockId: Id? = null,
        ) : DataView()
    }

    object Init : ObjectState() {
        override val isInitialized: Boolean
            get() = false
    }

    object ErrorLayout : ObjectState() {
        override val isInitialized: Boolean
            get() = false
    }

    companion object {
        const val VIEW_DEFAULT_OBJECT_TYPE = ObjectTypeIds.PAGE
        val VIEW_TYPE_UNSUPPORTED = DVViewerType.BOARD
    }
}
