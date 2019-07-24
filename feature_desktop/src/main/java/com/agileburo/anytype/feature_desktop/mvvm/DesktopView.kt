package com.agileburo.anytype.feature_desktop.mvvm

sealed class DesktopView {
    data class Document(val id : String, val title : String) : DesktopView()
    object NewDocument : DesktopView()
}