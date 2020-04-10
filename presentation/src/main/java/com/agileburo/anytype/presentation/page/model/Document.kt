package com.agileburo.anytype.presentation.page.model

import com.agileburo.anytype.domain.block.model.Block

class Document(
    var blocks: List<Block>,
    var details: Block.Details
)