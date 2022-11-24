package com.anytypeio.anytype.presentation.relations.model

import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat

sealed class RelationView {

    abstract val format: RelationFormat

    data class Existing(
        val key: Key,
        val name: String,
        override val format: RelationFormat
    ) : RelationView()

    data class CreateFromScratch(
        override val format: RelationFormat,
        val isSelected: Boolean = false
    ) : RelationView()
}