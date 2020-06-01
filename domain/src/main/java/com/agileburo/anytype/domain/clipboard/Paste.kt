package com.agileburo.anytype.domain.clipboard

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.model.Payload

/**
 * Use-case for pasting to Anytype clipboard.
 */
class Paste(
    private val repo: BlockRepository,
    private val clipboard: Clipboard,
    private val matcher: Clipboard.UriMatcher
) : BaseUseCase<Paste.Response, Paste.Params>() {

    override suspend fun run(params: Params) = safe {
        val clip = clipboard.clip()

        if (clip != null) {

            val uri = clip.uri

            val blocks = if (uri != null && matcher.isAnytypeUri(uri))
                clipboard.blocks()
            else
                emptyList()

            repo.paste(
                command = Command.Paste(
                    context = params.context,
                    focus = params.focus,
                    selected = emptyList(),
                    range = params.range,
                    text = clip.text,
                    html = clip.html,
                    blocks = blocks
                )
            )
        } else {
            throw IllegalStateException("Empty clip!")
        }
    }

    /**
     * Params for pasting to Anytype clipboard
     * @property context id of the context
     * @property focus id of the focused/target block
     * @property range selected text range
     */
    data class Params(
        val context: Id,
        val focus: Id,
        val range: IntRange
    )

    /**
     * Response for the use-case.
     * @param cursor caret position
     * @param blocks ids of the new blocks
     * @param payload response payload
     */
    data class Response(
        val cursor: Int,
        val blocks: List<Id>,
        val payload: Payload
    )
}