package com.agileburo.anytype.core_utils.ext

/**
 * This method removes alpha from color.
 * @return Transparent color, when input is 0
 */

const val TRANSPARENT_COLOR = "#00000000"

fun Int.hexColorCode(): String =
    if (this == 0) {
        TRANSPARENT_COLOR
    } else {
        String.format("#%06X", 0xFFFFFF and this)
    }