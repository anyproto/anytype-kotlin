package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.model.Payload

interface Clipboard {

    /**
     * Use-case for pasting to Anytype clipboard.
     */
    class Paste(
        private val repo: BlockRepository
    ) : BaseUseCase<Paste.Response, Paste.Params>(), Clipboard {

        override suspend fun run(params: Params) = safe {
            repo.paste(
                command = Command.Paste(
                    context = params.context,
                    focus = params.focus,
                    selected = params.selected,
                    range = params.range,
                    text = params.text,
                    html = params.html,
                    blocks = params.blocks
                )
            )
        }

        /**
         * Params for pasting to Anytype clipboard
         * @property context id of the context
         * @property focus id of the focused/target block
         * @property selected id of currently selected blocks
         * @property range selected text range
         * @property text plain text to paste
         * @property html optional html to paste
         * @property blocks blocks currently contained in clipboard
         */
        data class Params(
            val context: Id,
            val focus: Id,
            val selected: List<Id>,
            val range: IntRange,
            val text: String,
            val html: String?,
            val blocks: List<Block>
        )

        /**
         * Response for [Clipboard.Paste] use-case.
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
}