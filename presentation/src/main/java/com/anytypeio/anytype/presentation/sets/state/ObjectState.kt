package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.extension.getObject
import timber.log.Timber

sealed class ObjectState {

    abstract val isInitialized: Boolean

    sealed class DataView : ObjectState() {

        abstract val root: Id
        abstract val blocks: List<Block>
        abstract val details: ObjectViewDetails
        abstract val objectRestrictions: List<ObjectRestriction>
        abstract val dataViewRestrictions: List<DataViewRestrictions>

        abstract val dataViewContent: DV
        abstract val dataViewBlock: Block
        abstract val viewers: List<DVViewer>

        abstract val hasObjectLayoutConflict: Boolean

        /** Whether the object is a type showing the objects of that type. */
        val isTypeSet: Boolean
            get() = details.getObject(root)?.layout == ObjectType.Layout.OBJECT_TYPE


        /**
         * Whether the given data view holds its own objects instead of querying
         * sources. Falls back to the block when the layout has not caught up with
         * a conversion.
         */
        fun isCollection(blockId: Id): Boolean {
            val rootLayout = details.getObject(root)?.layout
            if (rootLayout == ObjectType.Layout.COLLECTION) return true
            val content = blocks.firstOrNull { it.id == blockId }?.content ?: return false
            if (content !is DV) {
                Timber.e("Block $blockId of object $root is not a data view block")
                return false
            }
            if (rootLayout !in DATA_VIEW_LAYOUTS) {
                val target = details.getObject(content.targetObjectId)
                if (target != null) return target.layout == ObjectType.Layout.COLLECTION
            }
            return content.isCollection
        }

        data class Set(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
        ) : DataView() {

            override val isInitialized get() = blocks.any { it.content is DV }
            override val dataViewBlock get() = blocks.first { it.content is DV }
            override val dataViewContent get() = dataViewBlock.content as DV
            override val viewers get() = dataViewContent.viewers
        }

        data class Collection(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
        ) : DataView() {

            override val isInitialized get() = blocks.any { it.content is DV }
            override val dataViewBlock get() = blocks.first { it.content is DV }
            override val dataViewContent get() = dataViewBlock.content as DV
            override val viewers get() = dataViewContent.viewers
        }

        data class TypeSet(
            override val root: Id,
            override val blocks: List<Block> = emptyList(),
            override val details: ObjectViewDetails = ObjectViewDetails.EMPTY,
            override val objectRestrictions: List<ObjectRestriction> = emptyList(),
            override val dataViewRestrictions: List<DataViewRestrictions> = emptyList(),
            override val hasObjectLayoutConflict: Boolean = false,
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

    companion object {
        /**
         * Layouts of the objects that own a data view. Anything else hosts it
         * inline.
         */
        private val DATA_VIEW_LAYOUTS = setOf(
            ObjectType.Layout.SET,
            ObjectType.Layout.COLLECTION,
            ObjectType.Layout.OBJECT_TYPE
        )

        const val VIEW_DEFAULT_OBJECT_TYPE = ObjectTypeIds.PAGE
        val VIEW_TYPE_UNSUPPORTED = DVViewerType.BOARD
    }
}
