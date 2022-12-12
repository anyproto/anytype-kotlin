package com.anytypeio.anytype.core_models

data class CreateBlockLinkWithObjectResult(
    val blockId: Id,
    val objectId: Id,
    val event: Payload
)

