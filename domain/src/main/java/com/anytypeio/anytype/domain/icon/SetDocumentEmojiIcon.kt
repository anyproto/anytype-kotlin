package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for setting emoji icon.
 */
class SetDocumentEmojiIcon(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetDocumentEmojiIcon.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setDocumentEmojiIcon(
            command = Command.SetDocumentEmojiIcon(
                context = params.context,
                target = params.target,
                emoji = params.emoji
            )
        )
    }

    /**
     * Params for for setting document's emoji icon
     * @property emoji emoji's unicode
     * @property target id of the target block (icon)
     * @property context id of the context for this operation
     */
    data class Params(
        val emoji: String,
        val target: Id,
        val context: Id
    )
}