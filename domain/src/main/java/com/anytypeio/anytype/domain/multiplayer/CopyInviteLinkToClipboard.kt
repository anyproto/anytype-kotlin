package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.clipboard.Clipboard
import javax.inject.Inject

/**
 * Use case for copying invite link to clipboard
 */
class CopyInviteLinkToClipboard @Inject constructor(
    private val clipboard: Clipboard
) : BaseUseCase<Unit, CopyInviteLinkToClipboard.Params>() {

    override suspend fun run(params: Params) = safe {
        clipboard.put(
            text = params.link,
            html = null,
            blocks = emptyList<Block>()
        )
    }

    data class Params(
        val link: String
    )
}