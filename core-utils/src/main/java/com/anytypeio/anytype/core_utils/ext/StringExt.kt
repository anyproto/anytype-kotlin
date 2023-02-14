package com.anytypeio.anytype.core_utils.ext

fun String?.orNull(): String? = this?.ifEmpty { null }