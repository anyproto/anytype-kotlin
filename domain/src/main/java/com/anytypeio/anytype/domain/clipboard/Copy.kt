package com.anytypeio.anytype.domain.clipboard

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id

class Copy(
    private val repo: BlockRepository,
    private val clipboard: Clipboard
) : BaseUseCase<Unit, Copy.Params>() {

    override suspend fun run(params: Params) = safe {
        val result = repo.copy(
            command = Command.Copy(
                context = params.context,
                range = params.range,
                blocks = params.blocks
            )
        )
        clipboard.put(
            text = result.text,
            html = result.html,
            blocks = result.blocks
        )
    }

    /**
     * Params for clipboard paste operation.
     * @param context id of the context
     * @param range selected text range
     * @param blocks associated blocks
     */
    data class Params(
        val context: Id,
        val range: IntRange?,
        val blocks: List<Block>
    )

    /**
     * @param text plain text
     * @param html optional html
     * @param blocks anytype clipboard slot
     */
    class Response(
        val text: String,
        val html: String?,
        val blocks: List<Block>
    )
}