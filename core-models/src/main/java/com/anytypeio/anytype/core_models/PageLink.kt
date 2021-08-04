package com.anytypeio.anytype.core_models

/**
 * @property id document id
 * @property fields document fields
 * @property snippet text from first child block of the document
 * @property hasInboundLinks does this page has inbound pages
 */
data class DocumentInfo(
    val id: String,
    val obj: ObjectWrapper.Basic,
    val snippet: String?,
    val hasInboundLinks: Boolean,
    val smartBlockType: SmartBlockType
)

data class ObjectLinks(val inbound: List<DocumentInfo>, val outbound: List<DocumentInfo>)

data class ObjectInfoWithLinks(
    val id: String,
    val documentInfo: DocumentInfo,
    val links: ObjectLinks
)