package com.agileburo.anytype.presentation.desktop

sealed class DesktopView {
    data class Document(val id: String, val title: String) : DesktopView()
}