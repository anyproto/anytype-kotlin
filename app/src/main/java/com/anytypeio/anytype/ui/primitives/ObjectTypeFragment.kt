package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeMainScreen
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeViewModel
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.viewmodel.UiErrorState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject
import kotlin.getValue
import timber.log.Timber

class ObjectTypeFragment : BaseComposeFragment() {
    @Inject
    lateinit var factory: ObjectTypeVMFactory

    private val vm by viewModels<ObjectTypeViewModel> { factory }
    private lateinit var navComposeController: NavHostController

    private val space get() = argString(ARG_SPACE)
    private val objectId get() = argString(ARG_OBJECT_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            ObjectTypeScreen()
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
                is ObjectTypeCommand.SendToast.Error -> TODO()
                is ObjectTypeCommand.SendToast.UnexpectedLayout -> TODO()
            }
        }
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
                ObjectTypeMainScreen(
                    uiSyncStatusBadgeState = vm.uiSyncStatusBadgeState.collectAsStateWithLifecycle().value,
                    uiSyncStatusState = vm.uiSyncStatusWidgetState.collectAsStateWithLifecycle().value,
                    uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                    uiIconState = vm.uiIconState.collectAsStateWithLifecycle().value,
                    uiFieldsButtonState = vm.uiFieldsButtonState.collectAsStateWithLifecycle().value,
                    uiLayoutButtonState = vm.uiLayoutButtonState.collectAsStateWithLifecycle().value,
                    uiTemplatesHeaderState = vm.uiTemplatesHeaderState.collectAsStateWithLifecycle().value,
                    uiTemplatesAddIconState = vm.uiTemplatesAddIconState.collectAsStateWithLifecycle().value,
                    uiTemplatesListState = vm.uiTemplatesListState.collectAsStateWithLifecycle().value,
                    uiObjectsHeaderState = vm.uiObjectsHeaderState.collectAsStateWithLifecycle().value,
                    uiObjectsAddIconState = vm.uiObjectsAddIconState.collectAsStateWithLifecycle().value,
                    uiObjectsSettingsIconState = vm.uiObjectsSettingsIconState.collectAsStateWithLifecycle().value,
                    uiObjectsListState = vm.uiObjectsListState.collectAsStateWithLifecycle().value,
                    uiContentState = vm.uiContentState.collectAsStateWithLifecycle().value,
                    onTypeEvent = {}
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle().value
        when (val state = errorStateScreen) {
            UiErrorState.Hidden -> {

            }
            is UiErrorState.Show -> {
                val message = when (val r = state.reason) {
                    is UiErrorState.Reason.ErrorGettingObjects -> r.msg
                    is UiErrorState.Reason.Other -> r.msg
                }
                BaseAlertDialog(
                    dialogText = message,
                    buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                    onButtonClick = vm::closeObject,
                    onDismissRequest = vm::closeObject
                )
            }
        }
    }

    override fun injectDependencies() {
        val params = ObjectTypeVmParams(
            spaceId = SpaceId(space),
            objectId = objectId
        )
        componentManager().objectTypeComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do nothing. TODO add ime padding.
    }

    companion object DateLayoutNavigation {
        private const val OBJ_TYPE_MAIN = "obj_type_main"
        const val ARG_SPACE = "arg.object.type.space"
        const val ARG_OBJECT_ID = "arg.object.type.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}