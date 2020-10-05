package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

class CreateNewDocument(
    private val repo: BlockRepository,
    private val documentEmojiProvider: DocumentEmojiIconProvider
) : BaseUseCase<CreateNewDocument.Result, CreateNewDocument.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Result> = safe {
        val emoji = documentEmojiProvider.random()
        val id = repo.createNewDocument(
            command = Command.CreateNewDocument(
                name = params.name,
                emoji = emoji
            )
        )
        Result(
            id = id,
            name = params.name,
            emoji = emoji
        )
    }

    data class Params(
        val name: String
    )

    data class Result(
        val id: String,
        val name: String,
        val emoji: String
    )
}