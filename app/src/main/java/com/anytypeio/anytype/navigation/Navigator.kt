package com.anytypeio.anytype.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.ui.allcontent.AllContentFragment
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.editor.EditorModalFragment
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.primitives.CreateTypeFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFieldsFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectFragment
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
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

    override fun openChat(target: Id, space: Id, popUpToVault: Boolean) {
        navController?.navigate(
            R.id.chatScreen,
            ChatFragment.args(
                ctx = target,
                space = space,
                popUpToVault = popUpToVault
            )
        )
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
                popUpTo(R.id.globalSearchScreen) {
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
                popUpTo(R.id.globalSearchScreen) {
                    inclusive = true
                }
            }
        )
    }

    override fun exit(space: Id) {
        val popped = navController?.popBackStack()
        if (popped == false) {
            navController?.navigate(
                R.id.homeScreen,
                WidgetsScreenFragment.args(space = space)
            )
        }
    }

    override fun exitToDesktop(space: Id) {
        val popped = navController?.popBackStack(R
            .id.homeScreen,
            false
        )
        if (popped == false) {
            navController?.navigate(
                R.id.homeScreen,
                WidgetsScreenFragment.args(space = space)
            )
        }
    }

    override fun exitToVault() {
        val popped = navController?.popBackStack(R.id.vaultScreen, true)
        if (popped == false) {
            navController?.navigate(R.id.vaultScreen)
        }
    }

    override fun exitToSpaceHome() {
        runCatching {
            navController?.let { controller ->
                val isPopped = controller.popBackStack(R.id.chatScreen, inclusive = false)
                if (!isPopped) {
                    controller.popBackStack(R.id.homeScreen, inclusive = false)
                }
            }
        }.onFailure {
            Timber.e(it, "Error while exiting to space home")
        }
    }

    override fun openGlobalSearch(space: Id) {
        navController?.navigate(
            resId = R.id.globalSearchScreen,
            args = GlobalSearchFragment.args(
                space = space
            )
        )
    }

    override fun openShareScreen(space: SpaceId) {
        navController?.navigate(
            R.id.shareSpaceScreen,
            args = ShareSpaceFragment.args(space)
        )
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
                    popUpTo(R.id.homeScreen) {
                        inclusive = false
                    }
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

    override fun openRemoteFilesManageScreen(subscription: Id, space: Id) {
        navController?.navigate(
            resId = R.id.remoteStorageFragment,
            args = RemoteFilesManageFragment.args(
                subscription = subscription,
                space = space
            )
        )
    }

    override fun openTemplatesModal(typeId: Id) {
        navController?.navigate(
            R.id.nav_templates_modal,
            bundleOf(
                TemplateSelectFragment.ARG_TYPE_ID to typeId
            )
        )
    }

    override fun openAllContent(space: Id) {
        navController?.navigate(
            resId = R.id.action_open_all_content,
            args = AllContentFragment.args(space)
        )
    }

    override fun openRelationCreationScreen(id: Id, name: String, space: Id) {
        navController?.navigate(
            resId = R.id.openRelationCreationScreen,
            args = RelationCreateFromScratchForObjectFragment.args(
                ctx = id,
                query = name,
                space = space
            )
        )
    }

    override fun openCreateObjectTypeScreen(spaceId: Id) {
        val args = CreateTypeFragment.args(
            spaceId = spaceId
        )
        navController?.navigate(
            resId = R.id.createObjectTypeScreen,
            args = args
        )
    }

    override fun openDateObject(
        objectId: Id,
        space: Id
    ) {
        navController?.navigate(
            resId = R.id.dateObjectScreen,
            args = DateObjectFragment.args(
                objectId = objectId,
                space = space)
        )
    }

    override fun openParticipantObject(
        objectId: Id,
        space: Id
    ) {
        navController?.navigate(
            resId = R.id.participantScreen,
            args = ParticipantFragment.args(
                objectId = objectId,
                space = space
            )
        )
    }

    override fun openObjectType(
        objectId: Id,
        space: Id
    ) {
        navController?.navigate(
            resId = R.id.objectTypeNavigation,
            args = ObjectTypeFragment.args(
                objectId = objectId,
                space = space
            )
        )
    }

    override fun openCurrentObjectTypeFields(
        objectId: Id,
        space: Id
    ) {
        navController?.navigate(
            resId = R.id.objectTypeFieldsScreen,
            args = ObjectTypeFieldsFragment.args(
                objectId = objectId,
                space = space
            )
        )
    }
}
