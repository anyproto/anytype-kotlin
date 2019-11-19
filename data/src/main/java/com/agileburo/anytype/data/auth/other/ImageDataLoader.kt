package com.agileburo.anytype.data.auth.other

import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.image.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageDataLoader(
    private val remote: ImageLoaderRemote
) : ImageLoader {

    override suspend fun load(
        id: String, size: Image.Size
    ): ByteArray = withContext(Dispatchers.IO) {
        remote.load(
            id = id,
            size = size.toEntity()
        )
    }
}