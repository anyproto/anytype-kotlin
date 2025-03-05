package com.anytypeio.anytype.ui.primitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
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
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeMainScreen
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeViewModel
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState
import com.anytypeio.anytype.feature_object_type.fields.ui.FieldsMainScreen
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.editor.EditorModalFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.relations.RelationAddToObjectFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.templates.EditorTemplateFragment.Companion.TYPE_TEMPLATE_EDIT
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_PICK_EMOJI
import com.anytypeio.anytype.ui.types.picker.REQUEST_KEY_REMOVE_EMOJI
import com.anytypeio.anytype.ui.types.picker.RESULT_EMOJI_UNICODE
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY_PICK_EMOJI) { _, bundle ->
            val res = requireNotNull(bundle.getString(RESULT_EMOJI_UNICODE))
            vm.updateIcon(res)
        }
        setFragmentResultListener(REQUEST_KEY_REMOVE_EMOJI) { _, _ ->
            vm.removeIcon()
        }
    }

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
                ObjectTypeCommand.OpenEmojiPicker -> {
                    runCatching {
                        findNavController().navigate(R.id.openEmojiPicker)
                    }.onFailure {
                        Timber.w("Error while opening emoji picker")
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

                ObjectTypeCommand.OpenFieldsScreen -> {
                    navComposeController.navigate(OBJ_TYPE_FIELDS)
                }

                is ObjectTypeCommand.OpenAddFieldScreen -> {
                    RelationAddToObjectFragment.new(
                        ctx = command.typeId,
                        space = command.space,
                        isSetOrCollection = command.isSet
                    ).showChildFragment()
                }
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
                    uiDeleteAlertState = vm.uiAlertState.collectAsStateWithLifecycle().value,
                    uiEditButtonState = vm.uiEditButtonState.collectAsStateWithLifecycle().value,
                    uiLayoutTypeState = vm.uiTypeLayoutsState.collectAsStateWithLifecycle().value,
                    onTypeEvent = vm::onTypeEvent
                )
            }
            composable(route = OBJ_TYPE_FIELDS) {
                FieldsMainScreen(
                    uiFieldsListState = vm.uiFieldsListState.collectAsStateWithLifecycle().value,
                    uiTitleState = vm.uiTitleState.collectAsStateWithLifecycle().value,
                    uiIconState = vm.uiIconState.collectAsStateWithLifecycle().value,
                    uiFieldEditOrNewState = vm.uiFieldEditOrNewState.collectAsStateWithLifecycle().value,
                    uiFieldLocalInfoState = vm.uiFieldLocalInfoState.collectAsStateWithLifecycle().value,
                    uiAddFieldsScreenState = vm.uiAddFieldsState.collectAsStateWithLifecycle().value,
                    fieldEvent = vm::onFieldEvent
                )
            }
        }
        LaunchedEffect(Unit) {
            vm.navigation.collect { nav ->
                when (nav) {
                    is OpenObjectNavigation.OpenEditor -> {
                        findNavController().navigate(
                            R.id.objectNavigation,
                            EditorFragment.args(
                                ctx = nav.target,
                                space = nav.space
                            )
                        )
                    }

                    is OpenObjectNavigation.OpenDataView -> {
                        findNavController().navigate(
                            R.id.dataViewNavigation,
                            ObjectSetFragment.args(
                                ctx = nav.target,
                                space = nav.space
                            )
                        )
                    }

                    is OpenObjectNavigation.OpenParticipant -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.participantScreen,
                                ParticipantFragment.args(
                                    objectId = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening participant screen")
                        }
                    }

                    is OpenObjectNavigation.OpenChat -> {
                        findNavController().navigate(
                            R.id.chatScreen,
                            ChatFragment.args(
                                ctx = nav.target,
                                space = nav.space
                            )
                        )
                    }

                    OpenObjectNavigation.NonValidObject -> {
                        toast(getString(R.string.error_non_valid_object))
                    }

                    is OpenObjectNavigation.OpenDateObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.dateObjectScreen,
                                DateObjectFragment.args(
                                    objectId = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.e(it, "Failed to navigate to date object screen")
                        }
                    }

                    is OpenObjectNavigation.UnexpectedLayoutError -> {
                        toast(getString(R.string.error_unexpected_layout))
                    }

                    else -> {
                        // Do nothing.
                    }
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
            }
        }
        when (val state = errorStateScreen) {
            UiErrorState.Hidden -> {

            }

            is UiErrorState.Show -> {

            }
        }
    }

    override fun injectDependencies() {
        val params = ObjectTypeVmParams(
            spaceId = SpaceId(space),
            objectId = objectId,
            showHiddenFields = true
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
        private const val OBJ_TYPE_FIELDS = "obj_fields"
        const val ARG_SPACE = "arg.object.type.space"
        const val ARG_OBJECT_ID = "arg.object.type.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}