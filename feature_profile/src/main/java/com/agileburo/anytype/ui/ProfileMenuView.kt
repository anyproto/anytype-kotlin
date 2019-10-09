package com.agileburo.anytype.ui

sealed class ProfileMenuView {
    object Phrase : ProfileMenuView()
    object Pin : ProfileMenuView()
}