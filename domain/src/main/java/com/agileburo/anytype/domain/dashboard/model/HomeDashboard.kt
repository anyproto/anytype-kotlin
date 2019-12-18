package com.agileburo.anytype.domain.dashboard.model

import com.agileburo.anytype.domain.block.model.Block

data class HomeDashboard(
    val id: String,
    val blocks: List<Block>,
    val children: List<String>,
    val fields: Block.Fields,
    val type: Block.Content.Dashboard.Type
)