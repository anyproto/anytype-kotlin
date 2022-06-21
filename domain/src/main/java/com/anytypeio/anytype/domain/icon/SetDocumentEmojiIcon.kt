package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for setting emoji icon.
 */
class SetDocumentEmojiIcon(
    private val repo: BlockRepository
) : SetEmojiIcon<Id>() {

    override suspend fun run(params: SetEmojiIcon.Params<Id>) = safe {
        repo.setDocumentEmojiIcon(
            command = Command.SetDocumentEmojiIcon(
                context = params.target,
                emoji = params.emoji
            )
        )
    }
}