package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.domain.block.model.Block

/**
 * @property id document id
 * @property fields document fields
 * @property snippet text from first child block of the document
 * @property hasInboundLinks does this page has inbound pages
 */
data class DocumentInfo(
    val id: String,
    val fields: Block.Fields,
    val snippet: String?,
    val hasInboundLinks: Boolean,
    val type: Type
) {
    enum class Type { PAGE, HOME, PROFILE_PAGE, ARCHIVE, SET }
}

data class PageLinks(val inbound: List<DocumentInfo>, val outbound: List<DocumentInfo>)

data class PageInfoWithLinks(
    val id: String,
    val documentInfo: DocumentInfo,
    val links: PageLinks
)