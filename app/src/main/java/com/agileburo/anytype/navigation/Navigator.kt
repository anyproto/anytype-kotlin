package com.agileburo.anytype.navigation

import android.os.Bundle
import androidx.navigation.NavController
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.ui.auth.Keys

class Navigator : AppNavigation {

    private var navController: NavController? = null

    override fun startSplashFromDesktop() {
        navController?.navigate(R.id.action_profileScreen_to_splashFragment)
    }

    override fun startDesktopFromSplash() {
        navController?.navigate(R.id.action_splashScreen_to_desktopScreen)
    }

    override fun startDesktopFromLogin() {
        navController?.navigate(R.id.action_global_desktopScreen)
    }

    override fun startLogin() {
        navController?.navigate(R.id.action_splashFragment_to_login_nav)
    }

    override fun createProfile() {
        navController?.navigate(R.id.action_create_profile)
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

    override fun chooseAccount() {
        navController?.navigate(R.id.action_select_account)
    }

    override fun workspace() {}

    override fun openProfile() {
        navController?.navigate(R.id.action_open_profile)
    }

    override fun openDocument(id: String) {
        navController?.navigate(R.id.pageScreen)
    }

    override fun openKeychainScreen() {
        navController?.navigate(R.id.action_open_keychain)
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

    override fun confirmPinCode(pin: String) {
        /*
        navController?.navigate(
            R.id.confirmPinCodeScreen,
            Bundle().apply { putString(Keys.PIN_CODE_KEY, pin) }
        )
        */
    }

    override fun openContacts() {
        navController?.navigate(R.id.action_desktopScreen_to_contactsFragment)
    }

    override fun exit() {
        navController?.popBackStack()
    }

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        navController = null
    }
}