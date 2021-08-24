package com.anytypeio.anytype.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.settings.EditorSettings
import com.anytypeio.anytype.ui.archive.ArchiveFragment
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.ui.auth.account.CreateAccountFragment.Companion.ARGS_CODE
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.navigation.PageNavigationFragment
import com.anytypeio.anytype.ui.sets.CreateObjectSetFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment

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

    override fun createProfile(invitationCode: String) {
        val bundle = bundleOf(ARGS_CODE to invitationCode)
        navController?.navigate(R.id.action_invitationFragment_to_createAccountScreen, bundle)
    }

    override fun setupNewAccount() {
        navController?.navigate(R.id.action_setup_new_account)
    }

    override fun enterInvitationCode() {
        navController?.navigate(R.id.action_startLoginScreen_to_invitationFragment)
    }

    override fun exitToInvitationCodeScreen() {
        navController?.navigate(R.id.action_setupNewAccountScreen_to_invitationFragment)
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

    override fun openDocument(id: String, editorSettings: EditorSettings?) {
        navController?.navigate(
            R.id.objectNavigation,
            Bundle().apply {
                putString(EditorFragment.ID_KEY, id)
                editorSettings?.let {
                    putParcelable(EditorFragment.DEBUG_SETTINGS, it)
                }
            }
        )
    }

    override fun launchDocument(id: String) {
        navController?.navigate(
            R.id.objectNavigation,
            bundleOf(EditorFragment.ID_KEY to id),
            navOptions {
                popUpTo = R.id.desktopScreen
                launchSingleTop = true
            }
        )
    }

    override fun launchObjectSet(id: Id) {
        navController?.navigate(
            R.id.dataViewNavigation,
            bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to id),
            navOptions {
                popUpTo = R.id.desktopScreen
                launchSingleTop = true
            }
        )
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

    override fun openGoals() {
        navController?.navigate(R.id.action_desktopScreen_to_goalsFragment)
    }

    override fun openCustomizeDisplayView() {
    }

    override fun openSwitchDisplayView() {
    }

    override fun openDatabaseViewAddView() {
    }

    override fun openContacts() {
    }

    override fun openEditDatabase() {
    }

    override fun exit() {
        navController?.popBackStack()
    }

    override fun exitToDesktop() {
        navController?.popBackStack(R.id.desktopScreen, false)
    }

    override fun openDebugSettings() {
        navController?.navigate(R.id.action_profileScreen_to_debugSettingsFragment)
    }

    override fun openPageNavigation(target: String) {
        val bundle = bundleOf(PageNavigationFragment.TARGET_ID_KEY to target)
        navController?.navigate(R.id.pageNavigationFragment, bundle)
    }

    override fun openPageSearch() {
        navController?.navigate(R.id.pageSearchFragment)
    }

    override fun exitToDesktopAndOpenPage(pageId: String) {
        navController?.navigate(
            R.id.desktopScreen,
            bundleOf(EditorFragment.ID_KEY to pageId),
            navOptions {
                popUpTo = R.id.desktopScreen
                launchSingleTop = true
            }
        )
    }

    override fun openArchive(target: String) {
        navController?.navigate(
            R.id.archiveFragment,
            bundleOf(ArchiveFragment.ID_KEY to target)
        )
    }

    override fun openObjectSet(target: String) {
        navController?.navigate(
            R.id.dataViewNavigation,
            bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to target)
        )
    }

    override fun openCreateSetScreen(ctx: Id) {
        navController?.navigate(
            R.id.from_desktop_to_create_sets,
            bundleOf(CreateObjectSetFragment.CONTEXT_ID_KEY to ctx)
        )
    }

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        navController = null
    }
}
