package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.presentation.editor.Editor
import kotlinx.coroutines.flow.Flow

interface RelationListProvider {

    val links: Flow<List<RelationLink>>
    val details: Flow<Block.Details>

    fun getLinks() : List<RelationLink>
    fun getDetails(): Block.Details

    class EditorRelationListProvider(
        private val storage: Editor.Storage
    ) : RelationListProvider {
        override val links: Flow<List<RelationLink>>
            get() = storage.relationLinks.stream()
        override val details: Flow<Block.Details>
            get() = storage.details.stream()

        override fun getLinks() = storage.relationLinks.current()
        override fun getDetails() = storage.details.current()
    }
}