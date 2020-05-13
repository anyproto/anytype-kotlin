package com.agileburo.anytype.domain.dashboard.interactor

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.ext.content

fun List<Block>.toHomeDashboard(
    id: String,
    details: Block.Details = Block.Details(emptyMap())
): HomeDashboard =
    first { it.id == id }.let { root ->
    HomeDashboard(
        id = root.id,
        blocks = root.children.mapNotNull { child ->
            find { it.id == child }
        },
        children = root.children,
        fields = root.fields,
        type = root.content<Block.Content.Smart>().type,
        details = details
    )
}