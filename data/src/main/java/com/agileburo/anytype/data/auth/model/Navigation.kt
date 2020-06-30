package com.agileburo.anytype.data.auth.model

data class PageInfoEntity(
    val id: String,
    val fields: BlockEntity.Fields,
    val snippet: String?,
    val lastOpened: Long,
    val hasInboundLinks: Boolean
)

data class PageLinksEntity(
    val inbound: List<PageInfoEntity>,
    val outbound: List<PageInfoEntity>
)

data class PageInfoWithLinksEntity(
    val id: String,
    val pageInfo: PageInfoEntity,
    val links: PageLinksEntity
)

