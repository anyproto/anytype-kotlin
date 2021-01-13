package com.anytypeio.anytype.presentation.page.cover

sealed class DocCaverGalleryView {
    data class Header(val title: String) : DocCaverGalleryView()
    data class Color(val color: CoverColor) : DocCaverGalleryView()
}