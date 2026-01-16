package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.fields.ui.FieldsMainScreen
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.create.SetTypeTitlesAndIconScreen
import com.anytypeio.anytype.feature_object_type.ui.icons.ChangeIconScreen
import com.anytypeio.anytype.feature_object_type.ui.menu.ObjectTypeMenu
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeViewModel
import com.anytypeio.anytype.ui.editor.EditorModalFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.TYPE_TEMPLATE_EDIT
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject
import timber.log.Timber

class ObjectTypeFragment : BaseComposeFragment() {
    @Inject
    lateinit var factory: ObjectTypeVMFactory
    private val vm by viewModels<ObjectTypeViewModel> { factory }
    private lateinit var navComposeController: NavHostController

    private val space get() = argString(ARG_SPACE)
    private val objectId get() = argString(ARG_OBJECT_ID)
    private val view: Id? get() = argOrNull<Id>(ARG_VIEW_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            ObjectTypeScreen()
            IconAndTitleUpdateScreen()
            IconsPickerScreen()
            ErrorScreen()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.commands) { command ->
            Timber.d("Received command: $command")
            when (command) {
                ObjectTypeCommand.Back -> {
                    runCatching {
                        findNavController().popBackStack()
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting back from object type screen")
                    }
                }

                is ObjectTypeCommand.OpenTemplate -> {
                    findNavController().navigate(
                        R.id.nav_editor_modal,
                        bundleOf(
                            EditorModalFragment.ARG_TEMPLATE_ID to command.templateId,
                            EditorModalFragment.ARG_TEMPLATE_TYPE_ID to command.typeId,
                            EditorModalFragment.ARG_TEMPLATE_TYPE_KEY to command.typeKey,
                            EditorModalFragment.ARG_SCREEN_TYPE to TYPE_TEMPLATE_EDIT,
                            EditorModalFragment.ARG_SPACE_ID to command.spaceId
                        )
                    )
                }

                is ObjectTypeCommand.OpenAddNewPropertyScreen -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.editTypePropertiesScreen,
                            EditTypePropertiesFragment.args(
                                objectId = command.typeId,
                                space = command.space
                            )
                        )
                    }.onFailure {
                        Timber.e(it, "Error while opening edit object type properties screen")
                    }
                }

                is ObjectTypeCommand.ShowToast -> {
                    toast(command.msg)
                }
            }
        }
        vm.sendAnalyticsScreenObjectType()
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    fun ObjectTypeScreen() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navComposeController = rememberNavController(bottomSheetNavigator)
        NavHost(
            navController = navComposeController,
            startDestination = OBJ_TYPE_MAIN
        ) {
            composable(route = OBJ_TYPE_MAIN) {
                val showPropertiesScreen = vm.showPropertiesScreen.collectAsStateWithLifecycle().value
                WithSetScreen(
                    uiSyncStatusBadgeState = vm.uiSyncStatusBadgeState.collectAsStateWithLifecycle().value,
                    uiIconState = vm.uiIconState.collectAsStateWithLifecycle().value,
                    uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                    uiDescriptionState = vm.uiDescriptionState.collectAsStateWithLifecycle().value,
                    uiHorizontalButtonsState = vm.uiHorizontalButtonsState.collectAsStateWithLifecycle().value,
                    uiTemplatesModalListState = vm.uiTemplatesModalListState.collectAsStateWithLifecycle().value,
                    uiLayoutTypeState = vm.uiTypeLayoutsState.collectAsStateWithLifecycle().value,
                    uiSyncStatusState = vm.uiSyncStatusWidgetState.collectAsStateWithLifecycle().value,
                    uiDeleteAlertState = vm.uiAlertState.collectAsStateWithLifecycle().value,
                    uiDeleteTypeAlertState = vm.uiDeleteTypeAlertState.collectAsStateWithLifecycle().value,
                    objectId = objectId,
                    space = space,
                    view = view,
                    onTypeEvent = vm::onTypeEvent
                )
                if (showPropertiesScreen) {
                    FieldsMainScreen(
                        uiFieldsListState = vm.uiTypePropertiesListState.collectAsStateWithLifecycle().value,
                        uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                        uiIconState = vm.uiIconState.collectAsStateWithLifecycle().value,
                        uiEditPropertyState = vm.uiEditPropertyScreen.collectAsStateWithLifecycle().value,
                        uiFieldLocalInfoState = vm.uiFieldLocalInfoState.collectAsStateWithLifecycle().value,
                        fieldEvent = vm::onFieldEvent
                    )
                }

                val menuState = vm.uiMenuState.collectAsStateWithLifecycle().value
                if (menuState.isVisible) {
                    ObjectTypeMenu(
                        isPinned = menuState.isPinned,
                        canDelete = menuState.canDelete,
                        isDescriptionFeatured = menuState.isDescriptionFeatured,
                        canEditDetails = menuState.canEditDetails,
                        onEvent = vm::onMenuEvent
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle().value
        if (errorStateScreen is UiErrorState.Show) {
            when (val r = errorStateScreen.reason) {
                is UiErrorState.Reason.ErrorGettingObjects -> {
                    BaseAlertDialog(
                        dialogText = "${stringResource(R.string.object_type_open_type_error)}:\n${r.msg}",
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::closeObject,
                        onDismissRequest = vm::closeObject
                    )
                }
                is UiErrorState.Reason.Other -> {
                    BaseAlertDialog(
                        dialogText = r.msg,
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
                }
                UiErrorState.Reason.ErrorEditingTypeDetails -> {
                    BaseAlertDialog(
                        dialogText = stringResource(R.string.object_type_edit_type_details_error),
                        buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                        onButtonClick = vm::hideError,
                        onDismissRequest = vm::hideError
                    )
                }
            }
        }
    }

    @Composable
    private fun IconsPickerScreen() {
        val uiState = vm.uiIconsPickerScreen.collectAsStateWithLifecycle().value
        if (uiState is UiIconsPickerState.Visible) {
            ChangeIconScreen(
                modifier = Modifier.fillMaxWidth(),
                onDismissRequest = {
                    vm.onTypeEvent(TypeEvent.OnIconPickerDismiss)
                },
                onIconClicked = { name, color ->
                    vm.onTypeEvent(
                        TypeEvent.OnIconPickerItemClick(
                            iconName = name,
                            color = color
                        )
                    )
                },
                onRemoveIconClicked = {
                    vm.onTypeEvent(TypeEvent.OnIconPickerRemovedClick)
                }
            )
        }
    }

    @Composable
    private fun IconAndTitleUpdateScreen() {
        SetTypeTitlesAndIconScreen(
            uiState = vm.uiTitleAndIconUpdateState.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissTitleAndIconScreen,
            onIconClicked = vm::onIconClickedTitleAndIconScreen,
            onButtonClicked = vm::onButtonClickedTitleAndIconScreen
        )
    }

    override fun injectDependencies() {
        val params = ObjectTypeVmParams(
            spaceId = SpaceId(space),
            objectId = objectId,
            showHiddenFields = true,
            initialViewId = view
        )
        componentManager().objectTypeComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Skipping this, since window insets will be applied by compose code.
    }

    companion object {
        private const val OBJ_TYPE_MAIN = "obj_type_main"
        const val ARG_SPACE = "arg.object.type.space"
        const val ARG_OBJECT_ID = "arg.object.type.object_id"
        const val ARG_VIEW_ID = "arg.object.type.view_id"

        fun args(space: Id, objectId: Id, view: Id? = null) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId,
            ARG_VIEW_ID to view
        )
    }
}