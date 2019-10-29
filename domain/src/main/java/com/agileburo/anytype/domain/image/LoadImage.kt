package com.agileburo.anytype.domain.image

import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

class LoadImage(private val loader: ImageLoader) : BaseUseCase<ByteArray, LoadImage.Param>() {

    override suspend fun run(params: Param) = try {
        loader.load(
            id = params.id,
            size = params.size
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Param(val id: String, val size: Image.Size = Image.Size.SMALL)

}