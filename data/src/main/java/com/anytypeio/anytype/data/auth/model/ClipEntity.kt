package com.anytypeio.anytype.data.auth.model

import com.anytypeio.anytype.domain.clipboard.Clip

/**
 * @see Clip
 */
class ClipEntity(
    override val text: String,
    override val html: String?,
    override val uri: String?
) : Clip