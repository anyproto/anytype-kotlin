package com.anytypeio.anytype.data.auth.model

data class DocumentInfoEntity(
    val id: String,
    val fields: BlockEntity.Fields,
    val snippet: String?,
    val hasInboundLinks: Boolean,
    val type: Type
) {
    enum class Type { PAGE, HOME, PROFILE_PAGE, ARCHIVE, SET }
}

data class PageLinksEntity(
    val inbound: List<DocumentInfoEntity>,
    val outbound: List<DocumentInfoEntity>
)

data class PageInfoWithLinksEntity(
    val id: String,
    val docInfo: DocumentInfoEntity,
    val links: PageLinksEntity
)

