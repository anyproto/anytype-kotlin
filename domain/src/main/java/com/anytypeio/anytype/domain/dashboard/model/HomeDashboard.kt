package com.anytypeio.anytype.domain.dashboard.model

import com.anytypeio.anytype.core_models.Block

data class HomeDashboard(
    val id: String,
    val blocks: List<Block>,
    val children: List<String>,
    val fields: Block.Fields,
    val type: Block.Content.Smart.Type,
    val details: Block.Details = Block.Details(emptyMap())
)