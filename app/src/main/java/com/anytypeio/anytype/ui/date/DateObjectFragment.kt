package com.anytypeio.anytype.ui.date

import android.os.Build
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
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
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_date.viewmodel.UiErrorState
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectViewModel
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectVMFactory
import com.anytypeio.anytype.feature_date.ui.DateMainScreen
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectCommand
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectVmParams
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.search.GlobalSearchFragment
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import javax.inject.Inject
import timber.log.Timber

class DateObjectFragment : BaseComposeFragment(), ObjectTypeSelectionListener {
    @Inject
    lateinit var factory: DateObjectVMFactory

    private val vm by viewModels<DateObjectViewModel> { factory }
    private lateinit var navComposeController: NavHostController

    private val space get() = argString(ARG_SPACE)
    private val objectId get() = argString(ARG_OBJECT_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            DateLayoutScreenWrapper()
            ErrorScreen()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(vm.effects) { effect ->
            Timber.d("Received date effect: $effect")
            when (effect) {
                DateObjectCommand.Back -> {
                    runCatching {
                        findNavController().popBackStack()
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting back from all content")
                    }
                }
                DateObjectCommand.ExitToVault -> {
                    runCatching {
                        findNavController().navigate(R.id.actionOpenVault)
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting to vault from all content")
                    }
                }
                is DateObjectCommand.NavigateToEditor -> {
                    runCatching {
                        navigation().openDocument(
                            target = effect.id,
                            space = effect.space.id
                        )
                    }.onFailure {
                        toast("Failed to open document")
                        Timber.e(it, "Failed to open document from all content")
                    }
                }
                is DateObjectCommand.NavigateToSetOrCollection -> {
                    runCatching {
                        navigation().openObjectSet(
                            target = effect.id,
                            space = effect.space.id
                        )
                    }.onFailure {
                        toast("Failed to open object set")
                        Timber.e(it, "Failed to open object set from all content")
                    }
                }
                is DateObjectCommand.OpenChat -> {
                    runCatching {
                        navigation().openChat(
                            target = effect.target,
                            space = effect.space.id
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to open a chat from all content")
                    }
                }
                is DateObjectCommand.OpenType -> {
                    runCatching {
                        navigation().openObjectType(
                            objectId = effect.target,
                            space = effect.space.id
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to open type object from data object")
                    }
                }
                DateObjectCommand.OpenGlobalSearch -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.globalSearchScreen,
                            args = GlobalSearchFragment.args(
                                space = space
                            )
                        )
                    }.onFailure { e ->
                        Timber.e(e, "Error while opening global search screen from all content")
                    }
                }
                is DateObjectCommand.NavigateToDateObject -> {
                    runCatching {
                        findNavController().navigate(
                            resId = R.id.dateObjectScreen,
                            args = args(
                                objectId = effect.objectId,
                                space = effect.space.id
                            ),
                            navOptions = navOptions {
                                launchSingleTop = true
                            }
                        )
                    }.onFailure {
                        Timber.e(it, "Failed to navigate to date object")
                    }
                }

                DateObjectCommand.ExitToHomeOrChat -> {
                    runCatching {
                        val result = findNavController().popBackStack(R.id.chatScreen, false)
                        if (!result) {
                            findNavController().popBackStack(R.id.homeScreen, false)
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Error while exiting to vault from all content")
                    }
                }
                is DateObjectCommand.SendToast.UnexpectedLayout -> {
                    toast("Unexpected layout")
                }
                is DateObjectCommand.SendToast.Error -> {
                    toast(effect.message)
                }
                DateObjectCommand.TypeSelectionScreen -> {
                    val dialog = ObjectTypeSelectionFragment.new(space = space)
                    dialog.show(childFragmentManager, null)
                }
                is DateObjectCommand.NavigateToParticipant -> {
                    runCatching {
                        findNavController().navigate(
                            R.id.participantScreen,
                            ParticipantFragment.args(
                                objectId = effect.objectId,
                                space = effect.space.id
                            )
                        )
                    }.onFailure {
                        Timber.w("Error while opening participant screen")
                    }
                }

                is DateObjectCommand.OpenUrl -> {
                    try {
                        ActivityCustomTabsHelper.openUrl(
                            activity = requireActivity(),
                            url = effect.url
                        )
                    } catch (e: Throwable) {
                        Timber.e(e, "Error opening bookmark URL: ${effect.url}")
                        toast("Failed to open URL")
                    }
                }
            }
        }
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onCreateObjectOfTypeClicked(objType = objType)
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
    fun DateLayoutScreenWrapper() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        navComposeController = rememberNavController(bottomSheetNavigator)
        NavHost(
            navController = navComposeController,
            startDestination = DATE_MAIN
        ) {
            composable(route = DATE_MAIN) {
                DateMainScreen(
                    uiCalendarIconState = vm.uiCalendarIconState.collectAsStateWithLifecycle().value,
                    uiSyncStatusBadgeState = vm.uiSyncStatusBadgeState.collectAsStateWithLifecycle().value,
                    uiHeaderState = vm.uiHeaderState.collectAsStateWithLifecycle().value,
                    uiFieldsState = vm.uiFieldsState.collectAsStateWithLifecycle().value,
                    uiObjectsListState = vm.uiObjectsListState.collectAsStateWithLifecycle().value,
                    uiNavigationWidget = vm.uiNavigationWidget.collectAsStateWithLifecycle().value,
                    uiFieldsSheetState = vm.uiFieldsSheetState.collectAsStateWithLifecycle().value,
                    uiContentState = vm.uiContentState.collectAsStateWithLifecycle().value,
                    canPaginate = vm.canPaginate.collectAsStateWithLifecycle().value,
                    uiCalendarState = vm.uiCalendarState.collectAsStateWithLifecycle().value,
                    uiSyncStatusState = vm.uiSyncStatusWidgetState.collectAsStateWithLifecycle().value,
                    uiSnackbarState = vm.uiSnackbarState.collectAsStateWithLifecycle().value,
                    onDateEvent = vm::onDateEvent
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle()
        when (val state = errorStateScreen.value) {
            UiErrorState.Hidden -> {
                // Do nothing
            }
            is UiErrorState.Show -> {
                val message = when (val r = state.reason) {
                    is UiErrorState.Reason.YearOutOfRange ->
                        stringResource(
                            id = R.string.date_layout_alert_date_out_of_range,
                            r.min,
                            r.max
                        )
                    is UiErrorState.Reason.ErrorGettingFields -> r.msg
                    is UiErrorState.Reason.ErrorGettingObjects -> r.msg
                    is UiErrorState.Reason.Other -> r.msg
                }
                BaseAlertDialog(
                    dialogText = message,
                    buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                    onButtonClick = vm::hideError,
                    onDismissRequest = vm::hideError
                )
            }
        }
    }

    override fun injectDependencies() {
        val params = DateObjectVmParams(
            spaceId = SpaceId(space),
            objectId = objectId
        )
        componentManager().dateObjectComponent.get(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().dateObjectComponent.release()
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object DateLayoutNavigation {
        private const val DATE_MAIN = "date_main"
        const val ARG_SPACE = "arg.date.object.space"
        const val ARG_OBJECT_ID = "arg.date.object.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}