package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Relation

data class RelationFormatView(
    val format: Relation.Format,
    val isSelected: Boolean = false
)