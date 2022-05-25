package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

class CreateObjectType(
    private val repo: BlockRepository,
    private val documentEmojiProvider: DocumentEmojiIconProvider
) : BaseUseCase<ObjectType, CreateObjectType.Params>() {

    override suspend fun run(
        params: Params
    ): Either<Throwable, ObjectType> = safe {
        val type = ObjectType.Prototype(
            name = params.name,
            layout = getLayout(params.layout),
            emoji = documentEmojiProvider.random()
        )
        repo.createObjectType(type)
    }

    private fun getLayout(layout: Int): ObjectType.Layout = when (layout) {
        ObjectType.Layout.BASIC.ordinal -> ObjectType.Layout.BASIC
        ObjectType.Layout.PROFILE.ordinal -> ObjectType.Layout.PROFILE
        ObjectType.Layout.TODO.ordinal -> ObjectType.Layout.TODO
        ObjectType.Layout.SET.ordinal -> ObjectType.Layout.SET
        else -> throw UnsupportedOperationException("Wrong Layout type:$layout")
    }

    data class Params(
        val name: String,
        val layout: Int
    )

    data class Response(val objectType: ObjectType)
}