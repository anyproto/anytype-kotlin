package com.agileburo.anytype.domain.page.navigation

import com.agileburo.anytype.domain.block.model.Block

/**
 * @property id page id
 * @property fields page fields
 * @property snippet text from first child block of the page
 * @property lastOpened last time opened
 * @property hasInboundLinks does this page has inbound pages
 */
data class PageInfo(
    val id: String,
    val fields: Block.Fields,
    val snippet: String?,
    val lastOpened: Long,
    val hasInboundLinks: Boolean
)

data class PageLinks(val inbound: List<PageInfo>, val outbound: List<PageInfo>)

data class PageInfoWithLinks(
    val id: String,
    val pageInfo: PageInfo,
    val links: PageLinks
)

