package com.agileburo.anytype.navigation

import android.os.Bundle
import androidx.navigation.NavController
import com.agileburo.anytype.R
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigation
import com.agileburo.anytype.feature_login.ui.login.presentation.common.Keys
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.AuthNavigation

class Navigator : AuthNavigation, DesktopNavigation {

    private var navController: NavController? = null

    override fun createProfile() {
        navController?.navigate(R.id.createProfileScreen)
    }

    override fun enterKeychain() {
        navController?.navigate(R.id.keychainLoginScreen)
    }

    override fun choosePinCode() {
        navController?.navigate(R.id.choosePinCodeScreen)
    }

    override fun confirmPinCode(pin: String) {
        navController?.navigate(
            R.id.confirmPinCodeScreen,
            Bundle().apply { putString(Keys.PIN_CODE_KEY, pin) }
        )
    }

    override fun congratulation() {
        navController?.navigate(R.id.congratulationScreen)
    }

    override fun chooseProfile() {
        navController?.navigate(R.id.chooseProfileScreen)
    }

    override fun workspace() {
        navController?.navigate(R.id.desktopScreen)
    }

    override fun openDocument(id: String) {
        navController?.navigate(R.id.documentScreen)
    }

    override fun setupNewAccount() {
        navController?.navigate(R.id.setupNewAccountScreen)
    }

    override fun setupSelectedAccount(id: String) {
        navController?.navigate(
            R.id.setupSelectedAccountScreen,
            Bundle().apply { putString(Keys.SELECTED_ACCOUNT_ID_KEY, id) }
        )
    }

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        navController = null
    }
}