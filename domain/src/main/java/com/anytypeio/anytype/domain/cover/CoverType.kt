package com.anytypeio.anytype.domain.cover

/**
 * [BUNDLED_IMAGE] preset image, or image bundled to Anytype
 * [UPLOADED_IMAGE] image uploaded by user and accessible by its hash.
 */
enum class CoverType {
    NONE,
    BUNDLED_IMAGE,
    UPLOADED_IMAGE,
    COLOR,
    GRADIENT,
}