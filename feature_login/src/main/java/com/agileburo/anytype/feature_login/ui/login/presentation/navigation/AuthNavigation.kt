package com.agileburo.anytype.feature_login.ui.login.presentation.navigation

interface AuthNavigation {
    fun createProfile()
    fun enterKeychain()
    fun choosePinCode()
    fun confirmPinCode(pin: String)
    fun setupNewAccount()
    fun setupSelectedAccount(id: String)
    fun congratulation()
    fun chooseProfile()
    fun workspace()
}