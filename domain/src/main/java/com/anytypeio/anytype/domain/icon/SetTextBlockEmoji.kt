package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetTextBlockEmoji(
    private val repo: BlockRepository
) : SetEmojiIcon<TextBlockTarget>() {

    override suspend fun run(
        params: Params<TextBlockTarget>
    ) = safe {
        repo.setTextIcon(
            command = Command.SetTextIcon(
                context = params.target.context,
                blockId = params.target.blockId,
                icon = Command.SetTextIcon.Icon.Emoji(params.emoji)
            )
        )
    }
}