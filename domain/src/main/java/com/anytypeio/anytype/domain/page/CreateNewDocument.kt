package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

class CreateNewDocument(
    private val repo: BlockRepository,
    private val documentEmojiProvider: DocumentEmojiIconProvider
) : BaseUseCase<CreateNewDocument.Result, CreateNewDocument.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Result> = safe {
        val emoji = null
        val id = repo.createNewDocument(
            command = Command.CreateNewDocument(
                name = params.name,
                emoji = emoji,
                type = params.type
            )
        )
        Result(
            id = id,
            name = params.name,
            emoji = emoji
        )
    }

    /**
     * [name] name for new object
     * [type] type for new object
     */
    data class Params(
        val name: String,
        val type: String?
    )

    data class Result(
        val id: String,
        val name: String,
        val emoji: String?
    )
}