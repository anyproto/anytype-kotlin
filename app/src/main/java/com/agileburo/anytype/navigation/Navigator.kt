package com.agileburo.anytype.navigation

import android.os.Bundle
import androidx.navigation.NavController
import com.agileburo.anytype.R
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigation
import com.agileburo.anytype.feature_login.ui.login.presentation.common.Keys
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.AuthNavigation
import com.agileburo.anytype.feature_profile.navigation.ProfileNavigation
import timber.log.Timber

class Navigator : AuthNavigation, DesktopNavigation, ProfileNavigation {

    private var navController: NavController? = null

    override fun createProfile() {
        navController?.navigate(R.id.action_open_sign_up)
    }

    override fun setupNewAccount() {
        navController?.navigate(R.id.action_setup_new_account)
    }

    override fun enterKeychain() {
        navController?.navigate(R.id.action_open_sign_in)
    }

    override fun congratulation() {
        navController?.navigate(R.id.action_open_congratulation_screen)
    }

    override fun chooseProfile() {
        navController?.navigate(R.id.action_select_account)
    }

    override fun workspace() {
        navController?.navigate(R.id.action_open_desktop_screen)
    }

    override fun openDocument(id: String) {
        navController?.navigate(R.id.action_open_document)
    }

    override fun setupSelectedAccount(id: String) {
        navController?.navigate(
            R.id.action_setup_selected_account,
            Bundle().apply { putString(Keys.SELECTED_ACCOUNT_ID_KEY, id) }
        )
    }

    override fun choosePinCode() {
        navController?.navigate(R.id.choosePinCodeScreen)
    }

    override fun openProfile() {
        navController?.navigate(R.id.action_open_profile)
    }

    override fun confirmPinCode(pin: String) {
        navController?.navigate(
            R.id.confirmPinCodeScreen,
            Bundle().apply { putString(Keys.PIN_CODE_KEY, pin) }
        )
    }

    override fun openPinCodeScreen() {
        Timber.d("OpenPinCodeScreen called")
    }

    override fun openKeychainScreen() {
        Timber.d("OpenKeychainScreen called")
    }

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        navController = null
    }
}