package com.anytypeio.anytype.presentation.editor.editor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlockDimensions(
    val left: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0,
    val right: Int = 0,
    val width: Int = 0,
    val height: Int = 0
) : Parcelable