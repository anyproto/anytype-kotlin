package com.anytypeio.anytype.presentation.editor.cover

sealed class CoverView {
    data class Color(val color: String) : CoverView()
    data class Gradient(val gradient: String) : CoverView()
    data class Image(val url: String) : CoverView()
}