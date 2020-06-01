package com.agileburo.anytype.data.auth.model

import com.agileburo.anytype.domain.clipboard.Clip

/**
 * @see Clip
 */
class ClipEntity(
    override val text: String,
    override val html: String?,
    override val uri: String?
) : Clip