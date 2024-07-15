package com.anytypeio.anytype.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.editor.EditorModalFragment
import com.anytypeio.anytype.ui.library.LibraryFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.RemoteFilesManageFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.TYPE_TEMPLATE_EDIT
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.TYPE_TEMPLATE_SELECT
import com.anytypeio.anytype.ui.templates.TemplateSelectFragment
import com.anytypeio.anytype.ui.widgets.collection.CollectionFragment
import timber.log.Timber

class Navigator : AppNavigation {

    private var navController: NavController? = null

    override fun openSpaceSettings() {
        try {
            navController?.navigate(R.id.action_open_space_settings)
        } catch (e: Exception) {
            Timber.e(e, "Error while opening settings")
        }
    }

    override fun openDocument(target: Id, space: Id) {
        navController?.navigate(
            R.id.objectNavigation,
            EditorFragment.args(
                ctx = target,
                space = space
            )
        )
    }

    override fun openModalTemplateSelect(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key,
        space: Id,
    ) {
        navController?.navigate(
            R.id.nav_editor_modal,
            bundleOf(
                EditorModalFragment.ARG_TEMPLATE_ID to template,
                EditorModalFragment.ARG_TEMPLATE_TYPE_ID to templateTypeId,
                EditorModalFragment.ARG_TEMPLATE_TYPE_KEY to templateTypeKey,
                EditorModalFragment.ARG_SCREEN_TYPE to TYPE_TEMPLATE_SELECT,
                EditorModalFragment.ARG_SPACE_ID to space
            )
        )
    }

    override fun openModalTemplateEdit(
        template: Id,
        templateTypeId: Id,
        templateTypeKey: Key,
        space: Id,
    ) {
        navController?.navigate(
            R.id.nav_editor_modal,
            bundleOf(
                EditorModalFragment.ARG_TEMPLATE_ID to template,
                EditorModalFragment.ARG_TEMPLATE_TYPE_ID to templateTypeId,
                EditorModalFragment.ARG_TEMPLATE_TYPE_KEY to templateTypeKey,
                EditorModalFragment.ARG_SCREEN_TYPE to TYPE_TEMPLATE_EDIT,
                EditorModalFragment.ARG_SPACE_ID to space
            )
        )
    }

    override fun launchDocument(target: String, space: Id) {
        navController?.navigate(
            R.id.objectNavigation,
            EditorFragment.args(
                ctx = target,
                space = space
            ),
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.pageSearchFragment) {
                    inclusive = true
                }
            }
        )
    }

    override fun launchCollections(subscription: Subscription, space: Id) {
        navController?.navigate(
            R.id.homeScreenWidgets,
            CollectionFragment.args(
                subscription = subscription.id,
                space = space
            )
        )
    }

    override fun launchObjectSet(target: Id, space: Id) {
        navController?.navigate(
            R.id.dataViewNavigation,
            ObjectSetFragment.args(
                ctx = target,
                space = space
            ),
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.pageSearchFragment) {
                    inclusive = true
                }
            }
        )
    }

    override fun openKeychainScreen() {
        navController?.navigate(R.id.action_open_keychain)
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
        // Old search
//        navController?.navigate(R.id.pageSearchFragment)
        // Uncomment to use new search
        navController?.navigate(R.id.globalSearchScreen)
    }

    override fun openObjectSet(
        target: Id,
        space: Id,
        view: Id?,
        isPopUpToDashboard: Boolean
    ) {
        if (isPopUpToDashboard) {
            navController?.navigate(
                R.id.dataViewNavigation,
                ObjectSetFragment.args(
                    ctx = target,
                    space = space,
                    view = view
                ),
                navOptions {
                    popUpTo(R.id.main_navigation) { inclusive = true }
                }
            )
        } else {
            navController?.navigate(
                R.id.dataViewNavigation,
                ObjectSetFragment.args(
                    ctx = target,
                    space = space,
                    view = view
                )
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

    override fun exitFromMigrationScreen() {
        navController?.navigate(R.id.onboarding_nav, null, navOptions {
            popUpTo(R.id.migrationNeededScreen) {
                inclusive = true
            }
        })
    }

    override fun openLibrary(space: Id) {
        navController?.navigate(
            R.id.libraryFragment,
            LibraryFragment.args(space)
        )
    }

    override fun openRemoteFilesManageScreen(subscription: Id) {
        navController?.navigate(R.id.remoteStorageFragment,
            bundleOf(RemoteFilesManageFragment.SUBSCRIPTION_KEY to subscription))
    }

    override fun openTemplatesModal(typeId: Id) {
        navController?.navigate(
            R.id.nav_templates_modal,
            bundleOf(
                TemplateSelectFragment.ARG_TYPE_ID to typeId
            )
        )
    }
}
