package com.anytypeio.anytype.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.ui.auth.account.CreateAccountFragment.Companion.ARGS_CODE
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.RemoteStorageFragment
import com.anytypeio.anytype.ui.templates.TemplateSelectFragment
import com.anytypeio.anytype.ui.widgets.collection.CollectionFragment

class Navigator : AppNavigation {

    private var navController: NavController? = null

    override fun startSplashFromDesktop() {
        navController?.navigate(R.id.action_profileScreen_to_splashFragment)
    }

    override fun startDesktopFromSplash() {
        navController?.navigate(R.id.action_splashScreen_to_homeScreen)
    }

    override fun startDesktopFromLogin() {
        navController?.navigate(R.id.action_global_desktopScreen)
    }

    override fun startDesktopFromSignUp() {
        val bundle = bundleOf(HomeScreenFragment.SHOW_MNEMONIC_KEY to true)
        navController?.navigate(R.id.action_global_desktopScreen, bundle)
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
        navController?.navigate(R.id.openInviteCodeScreen)
    }

    override fun aboutAnalyticsScreen() {
        navController?.navigate(R.id.openAboutAnalyticsScreen)
    }

    override fun exitToInvitationCodeScreen() {
        navController?.navigate(R.id.action_setupNewAccountScreen_to_invitationFragment)
    }

    override fun enterKeychain() {
        navController?.navigate(R.id.action_open_sign_in)
    }

    override fun chooseAccount() {
        navController?.navigate(R.id.action_select_account)
    }

    override fun workspace() {}

    override fun openSettings() {
        navController?.navigate(R.id.action_open_settings)
    }

    override fun openDocument(id: String) {
        navController?.navigate(
            R.id.objectNavigation,
            Bundle().apply {
                putString(EditorFragment.ID_KEY, id)
            }
        )
    }

    override fun launchDocument(id: String) {
        navController?.navigate(
            R.id.objectNavigation,
            bundleOf(EditorFragment.ID_KEY to id),
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.pageSearchFragment) {
                    inclusive = true
                }
            }
        )
    }

    override fun launchCollections(subscription: Subscription) {
        navController?.navigate(
            R.id.homeScreenWidgets,
            bundleOf(CollectionFragment.SUBSCRIPTION_KEY to subscription.id)
        )
    }

    override fun launchObjectSet(id: Id) {
        navController?.navigate(
            R.id.dataViewNavigation,
            bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to id),
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.pageSearchFragment) {
                    inclusive = true
                }
            }
        )
    }

    override fun launchObjectFromSplash(id: Id) {
        navController?.navigate(
            R.id.action_splashScreen_to_objectScreen,
            bundleOf(EditorFragment.ID_KEY to id),
        )
    }

    override fun launchObjectSetFromSplash(id: Id) {
        navController?.navigate(
            R.id.action_splashScreen_to_objectSetScreen,
            bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to id),
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
        val popped = navController?.popBackStack()
        if (popped == false) {
            navController?.navigate(R.id.homeScreen)
        }
    }

    override fun exitToDesktop() {
        val popped = navController?.popBackStack(R.id.homeScreen, false)
        if (popped == false) {
            navController?.navigate(R.id.homeScreen)
        }
    }

    override fun openDebugSettings() {
        navController?.navigate(R.id.action_profileScreen_to_debugSettingsFragment)
    }

    override fun openPageSearch() {
        navController?.navigate(R.id.pageSearchFragment)
    }

    override fun exitToDesktopAndOpenPage(pageId: String) {
        navController?.navigate(
            R.id.homeScreen,
            bundleOf(EditorFragment.ID_KEY to pageId),
            navOptions {
                launchSingleTop = true
            }
        )
    }

    override fun openObjectSet(target: String, isPopUpToDashboard: Boolean) {
        if (isPopUpToDashboard) {
            navController?.navigate(
                R.id.dataViewNavigation,
                bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to target),
                navOptions {
                    popUpTo(R.id.main_navigation) { inclusive = true }
                }
            )
        } else {
            navController?.navigate(
                R.id.dataViewNavigation,
                bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to target)
            )
        }
    }

    override fun openUserSettingsScreen() {
        navController?.navigate(R.id.action_profileScreen_to_userSettingsFragment)
    }

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        navController = null
    }

    override fun openUpdateAppScreen() {
        navController?.navigate(R.id.alertUpdateAppFragment)
    }

    override fun deletedAccountScreen(deadline: Long) {
        navController?.navigate(
            R.id.deletedAccountNavigation,
            bundleOf(DeletedAccountFragment.DEADLINE_KEY to deadline),
            navOptions {
                popUpTo = R.id.main_navigation
            }
        )
    }

    override fun logout() {
        navController?.navigate(R.id.actionLogout)
    }

    override fun migrationErrorScreen() {
        navController?.navigate(R.id.migrationNeededScreen)
    }

    override fun openTemplates(
        ctx: Id,
        type: String,
        templates: List<Id>
    ) {
        navController?.navigate(
            R.id.templateSelectScreen,
            bundleOf(
                TemplateSelectFragment.CTX_KEY to ctx,
                TemplateSelectFragment.TEMPLATE_IDS_KEY to templates,
                TemplateSelectFragment.OBJECT_TYPE_KEY to type
            )
        )
    }

    override fun openLibrary() {
        navController?.navigate(R.id.libraryFragment)
    }

    override fun openRemoteStorageScreen(subscription: Id) {
        navController?.navigate(R.id.remoteStorageFragment,
            bundleOf(RemoteStorageFragment.SUBSCRIPTION_KEY to subscription))
    }
}
