package com.agileburo.anytype.core_utils.ext

fun Int.hexColorCode(): String = String.format("#%06X", 0xFFFFFF and this)