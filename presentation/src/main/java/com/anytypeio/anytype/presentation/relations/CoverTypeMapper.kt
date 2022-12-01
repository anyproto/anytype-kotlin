package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider

fun CoverWrapper.getCover(
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider
): CoverContainer {
    val type = coverType

    var coverColor: CoverColor? = null
    var coverImage: Url? = null
    var coverGradient: String? = null

    when (type) {
        CoverType.UPLOADED_IMAGE,
        CoverType.UNSPLASH_IMAGE -> {
            coverImage = coverId?.let { id ->
                urlBuilder.image(id)
            }
        }
        CoverType.BUNDLED_IMAGE -> {
            val hash = coverId?.let { id ->
                coverImageHashProvider.provide(id)
            }
            if (hash != null) coverImage = urlBuilder.image(hash)
        }
        CoverType.COLOR -> {
            coverColor = coverId?.let { id ->
                CoverColor.values().find { it.code == id }
            }
        }
        CoverType.GRADIENT -> {
            coverGradient = coverId
        }
        CoverType.NONE -> {}
    }
    return CoverContainer(
        coverColor = coverColor,
        coverImage = coverImage,
        coverGradient = coverGradient
    )
}

class CoverContainer(
    val coverColor: CoverColor? = null,
    val coverImage: Url? = null,
    val coverGradient: String? = null
)

interface CoverWrapper {
    val coverType: CoverType
    val coverId: String?
}

class BlockFieldsCoverWrapper(val fields: Block.Fields?) : CoverWrapper {

    override val coverType: CoverType
        get() {
            val value = fields?.coverType?.toInt() ?: return CoverType.NONE
            return CoverType.values().find { type ->
                type.code == value
            } ?: CoverType.NONE
        }

    override val coverId = fields?.coverId
}

class BasicObjectCoverWrapper(val obj: ObjectWrapper.Basic?) : CoverWrapper {
    override val coverType = obj?.coverType ?: CoverType.NONE
    override val coverId = obj?.coverId
}