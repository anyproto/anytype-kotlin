package com.agileburo.anytype.domain.clipboard

/**
 * A clip on the clipboard.
 * @property text plain text
 * @property html html representation
 * @property uri uri for the copied content (Anytype URI or an external app URI)
 */
interface Clip {
    val text: String
    val html: String?
    val uri: String?
}