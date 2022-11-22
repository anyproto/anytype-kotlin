package com.anytypeio.anytype.presentation.editor.cover

sealed class CoverView {
    data class Color(val coverColor: CoverColor) : CoverView()
    data class Gradient(val gradient: String) : CoverView()
    data class Image(val url: String) : CoverView()
}