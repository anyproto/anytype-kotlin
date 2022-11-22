package com.anytypeio.anytype.core_models

/**
 * [BUNDLED_IMAGE] preset image, or image bundled to Anytype
 * [UPLOADED_IMAGE] image uploaded by user and accessible by its hash.
 */
enum class CoverType(val code: Int) {
    NONE(0),
    UPLOADED_IMAGE(1),
    COLOR(2),
    GRADIENT(3),
    BUNDLED_IMAGE(4),
    UNSPLASH_IMAGE(5)
}