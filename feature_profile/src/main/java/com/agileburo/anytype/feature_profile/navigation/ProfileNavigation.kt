package com.agileburo.anytype.feature_profile.navigation

interface ProfileNavigation {

    fun openPinCodeScreen()
    fun openKeychainScreen()

    sealed class Command {
        object OpenPinCodeScreen : Command()
        object OpenKeychainScreen : Command()
    }
}